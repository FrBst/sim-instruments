import org.matsim.pt2matsim.run.Gtfs2TransitSchedule;

public class PtGenerator {

    public static void readFromGtfs() {
        String[] args = new String[5];
        args[0] = "C:\\Users\\Maksim\\Desktop\\gbPT";
        args[1] = "dayWithMostTrips";
        args[2] = "EPSG:27700";
        args[3] = "transitSchedule.xml";
        args[4] = "transitVehicles.xml";
        Gtfs2TransitSchedule.main(args);
    }

}
