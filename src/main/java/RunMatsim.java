import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;

public class RunMatsim {
    public static void main(String[] args) {
        Config config;
        if ( args==null || args.length==0 || args[0]==null ){
            config = ConfigUtils.loadConfig( "scenarios/plymouth/config.xml" );
        } else {
            config = ConfigUtils.loadConfig( args );
        }

        Scenario scenario = ScenarioUtils.loadScenario(config) ;
        Controler controler = new Controler( scenario ) ;
        controler.run();
    }
}
