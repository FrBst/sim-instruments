import com.fasterxml.jackson.databind.ObjectMapper;
import org.matsim.contrib.minibus.PMain;
import org.matsim.core.controler.PrepareForSimUtils;
import org.matsim.core.router.speedy.SpeedyALT;
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
    public static void main(String[] args) throws IOException {
//        CreateFullConfig.main(new String[] {"full_config.xml"});
    	//TODO: Osm2MultimodalNetwork.run - изменения.
        Osm2MultimodalNetwork.run("/home/vit/eclipse-workspace/sim-instruments/original-input-data/" + Global.scenario + "/pt2matsim-network/config-fine.xml");
//        new OSMData(24, 16).init();
//        new CemdapGenerator().main();
    	
//
//        new MatsimGenerator(24, 16).generatePlans();
//        PtGenerator.readFromGtfs();
//        PublicTransitMapper.main(new String[]{"PTMapperConfig.xml"});
//        CountsGenerator.readData("original-input-data/counts.csv");
//        RunMatsim.main(new String[]{});
//        PMain.main(new String[] {"scenarios/plymouth/config-minibus.xml"});
//    	CountsGenerator.gunzip(Path.of("output/plymouth_0/plymouth_0.output_events.xml.gz"));
//        CountsGenerator.readCountsCompare(Path.of("output/plymouth_0/ITERS/it.100/plymouth_0.100.countscompare.txt"));
//
    	CountsGenerator.stats();
//        CountsGenerator.calculateError();
//        CountsGenerator.readSpecifiedStations("original-input-data/counts.csv",
////        		Arrays.asList(16989,27910,57823,70077,81374,38687));
     
        Output2EPSG.gen();
    }
}
