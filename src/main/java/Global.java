import java.util.LinkedList;
import java.util.List;

import org.locationtech.proj4j.ProjCoordinate;
import org.matsim.api.core.v01.Coord;

import model.Building;

public class Global {
	private static List<Coord> transitPortCoords;
	public static final String scenario = "plymouth";
	
	static {
		initParkingCoords();
	}
	
	public static List<Coord> getTransitPorts() {
		return List.copyOf(transitPortCoords);
	}
	
	private static void initParkingCoords() {
		transitPortCoords = new LinkedList<>();
		ProjCoordinate p = MatsimGenerator.transformFromWGS84(50.4151, -4.2497);
    	p = MatsimGenerator.transformFromWGS84(50.4453, -4.1088);
    	transitPortCoords.add(new Coord(p.x, p.y));
    	p = MatsimGenerator.transformFromWGS84(50.3809, -4.0010);
    	transitPortCoords.add(new Coord(p.x, p.y));
    	p = MatsimGenerator.transformFromWGS84(50.3545, -4.0510);
    	transitPortCoords.add(new Coord(p.x, p.y));
	}
}
