import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleUtils;
import org.matsim.pt.transitSchedule.TransitScheduleWriterV2;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import model.LinkHourlyCount;

public class Output2EPSG {
	
    private static CRSFactory factory = new CRSFactory();
    private static CoordinateReferenceSystem srcCrs = factory.createFromName("EPSG:27700");
    private static CoordinateReferenceSystem dstCrs = factory.createFromName("EPSG:4326");
    private static BasicCoordinateTransform transform = new BasicCoordinateTransform(srcCrs, dstCrs);
    private static ProjCoordinate srcCoord;
    private static ProjCoordinate dstCoord;
	
	public static void gen() {
		Scenario scen = ScenarioUtils.loadScenario(ConfigUtils.loadConfig("2epsg-config.xml"));
		
		for (Node n : scen.getNetwork().getNodes().values()) {
			transformReplace(n);
		}
		scen.getNetwork().getAttributes().putAttribute("coordinateReferenceSystem", "EPSG:4326");
		NetworkUtils.writeNetwork(scen.getNetwork(), "final/output_network.xml.gz");

		for (TransitStopFacility tsf : scen.getTransitSchedule().getFacilities().values()) {
			transformReplace(tsf);
		}
		scen.getTransitSchedule().getAttributes().putAttribute("coordinateReferenceSystem", "EPSG:4326");
		new TransitScheduleWriterV2(scen.getTransitSchedule()).write("final/transitSchedule.xml.gz");
		
	}
	
	private static void transformReplace(Node node) {
        srcCoord = new ProjCoordinate(node.getCoord().getX(), node.getCoord().getY());
        dstCoord = new ProjCoordinate();
        transform.transform(srcCoord, dstCoord);
        
        node.setCoord(new Coord(dstCoord.x, dstCoord.y));
	}
	
	private static void transformReplace(TransitStopFacility tsf) {
        srcCoord = new ProjCoordinate(tsf.getCoord().getX(), tsf.getCoord().getY());
        dstCoord = new ProjCoordinate();
        transform.transform(srcCoord, dstCoord);
        
        tsf.setCoord(new Coord(dstCoord.x, dstCoord.y));
	}
}
