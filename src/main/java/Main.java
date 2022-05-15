import com.fasterxml.jackson.databind.ObjectMapper;
import org.matsim.contrib.minibus.PMain;
import org.matsim.counts.Count;
import org.matsim.pt2matsim.config.OsmConverterConfigGroup;
import org.matsim.pt2matsim.run.CreateDefaultPTMapperConfig;
import org.matsim.pt2matsim.run.Osm2MultimodalNetwork;
import org.matsim.pt2matsim.run.PublicTransitMapper;
import org.matsim.run.CreateFullConfig;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

public class Main {
    public static void main(String[] args) {
//        CreateFullConfig.main(new String[] {"full_config.xml"});
//        Osm2MultimodalNetwork.run("/home/vit/eclipse-workspace/sim-instruments/original-input-data/plymouth/pt2matsim-network/config-fine.xml");
//        new CemdapGenerator().main();
    	new OSMData(24, 16).init();
    	
//
//        new MatsimGenerator().generatePlans();
//        PtGenerator.readFromGtfs();
//        PublicTransitMapper.main(new String[]{"PTMapperConfig.xml"});
//        RunMatsim.main(new String[]{});
//        PMain.main(new String[] {"scenarios/plymouth/config-minibus.xml"});
//        CountsGenerator.gunzip(Path.of("output/plymouth_0/plymouth_0.output_events.xml.gz"));

//        CountsGenerator.readData("original-input-data/counts.csv");
//        CountsGenerator.mapToNetwork();
//        CountsGenerator.calculateError();
//        CountsGenerator.readSpecifiedStations("original-input-data/counts.csv",
//        		Arrays.asList(16989,27910,57823,70077,81374,38687));
    }
}
