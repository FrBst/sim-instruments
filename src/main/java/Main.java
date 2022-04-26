import com.fasterxml.jackson.databind.ObjectMapper;
import org.matsim.contrib.minibus.PMain;
import org.matsim.pt2matsim.run.CreateDefaultPTMapperConfig;
import org.matsim.pt2matsim.run.PublicTransitMapper;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
//        new CemdapGenerator().main();

//        OSMData data = new OSMData(10, 10);
//        data.init();
//
//        new MatsimGenerator().generatePlans();
//        PtGenerator.readFromGtfs();
//        CreateDefaultPTMapperConfig.main(new String[]{"PTMapperConfig.xml"});
//        PublicTransitMapper.main(new String[]{"PTMapperConfig.xml"});
//        RunMatsim.main(new String[]{});
        PMain.main(new String[] {"scenarios\\plymouth\\config-minibus.xml"});
    }
}
