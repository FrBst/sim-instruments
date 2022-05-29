import java.util.LinkedList;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.contrib.multimodal.router.util.WalkTravelTime;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.events.handler.EventHandler;
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

//		ParkingScore parkingScoreManager = new ParkingScoreManager(new WalkTravelTime(controler.getConfig().plansCalcRoute()), scenario);
//		parkingScoreManager.setParkingScoreScalingFactor(1);
//		parkingScoreManager.setParkingBetas(new ParkingBetaExample());
		
//		ParkingInfrastructure parkingInfrastructureManager = new ParkingInfrastructureManager(parkingScoreManager, controler.getEvents());
//		{
//			LinkedList<PublicParking> publicParkings = new LinkedList<PublicParking>();
//			int id = 0;
//			for (Coord c : Global.getTransitPorts()) {
//				publicParkings.add(new PublicParking(Id.create("transitParking" + id, PC2Parking.class), 100000, c,
//						new ParkingCostCalculatorExample(0), "transitParking"));
//			}
//			parkingInfrastructureManager.setPublicParkings(publicParkings);
//		}
//
//		GeneralParkingModule generalParkingModule = new GeneralParkingModule(controler);
//		generalParkingModule.setParkingScoreManager(parkingScoreManager);
//		generalParkingModule.setParkingInfrastructurManager(parkingInfrastructureManager);
		
        controler.run();
    }
    
    private static EventsManager getCustomEventManager(Controler controler) {
    	return new EventsManager() {
			@Override
			public void processEvent(Event event) {
				controler.getInjector().getInstance(EventsManager.class).processEvent(event);
			}

			@Override
			public void addHandler(final EventHandler handler) {
				controler.addOverridingModule(new AbstractModule() {
					@Override
					public void install() {
						addEventHandlerBinding().toInstance(handler);
					}
				});
			}

			@Override
			public void removeHandler(EventHandler handler) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void resetHandlers(int iteration) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void initProcessing() {
				controler.getInjector().getInstance(EventsManager.class).initProcessing();
			}

			@Override
			public void afterSimStep(double time) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void finishProcessing() {
				controler.getInjector().getInstance(EventsManager.class).finishProcessing();
			}
		};
    }
}
