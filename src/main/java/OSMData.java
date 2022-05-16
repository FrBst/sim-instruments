import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.matsim.api.core.v01.Id;
import org.matsim.pt2matsim.osm.lib.Osm;
import org.matsim.pt2matsim.osm.lib.Osm.Node;
import org.matsim.pt2matsim.osm.lib.Osm.Way;
import org.matsim.pt2matsim.osm.lib.OsmData;
import org.matsim.pt2matsim.osm.lib.OsmDataImpl;
import org.matsim.pt2matsim.osm.lib.OsmFileReader;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.fasterxml.jackson.databind.JsonNode;

import model.ActivityType;
import model.Building;

public class OSMData {
	MathTransform trans;
	GeometryBuilder gb = new GeometryBuilder(JTSFactoryFinder.getGeometryFactory(null));
	int buildingScale = 100;
	int areaScale = 200;
	
    Random r = new Random(123);
    private int zonesX;
    private int zonesY;

    private double minX = 50.3406;
    private double minY = -4.2507;
    private double maxX = 50.4485;
    private double maxY = -3.9966;
    Map<Long, List<Double>> nodes = new HashMap<>();

    Map<ActivityType, ArrayList<ArrayList<Building>>> activities = new HashMap<ActivityType, ArrayList<ArrayList<Building>>>();

    public OSMData(int zonesX, int zonesY) {
        this.zonesX = zonesX;
        this.zonesY = zonesY;
        
        try {
        	CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
        	CoordinateReferenceSystem destCRS = CRS.decode("EPSG:27700");
        	trans = CRS.findMathTransform(sourceCRS, destCRS);
        	Geometry min = JTS.transform(gb.point(minX, minY), trans);
        	minX = min.getCoordinate().getX();
        	minY = min.getCoordinate().getY();
        	Geometry max = JTS.transform(gb.point(maxX, maxY), trans);
        	maxX = max.getCoordinate().getX();
        	maxY = max.getCoordinate().getY();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MismatchedDimensionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//        for (int i = 0; i < zonesX * zonesY; i++) {
//            activities.put(ActivityType.HOME, new ArrayList<>());
//            work.add(new ArrayList<>());
//        }
    }

    public Building getRandomBuilding(ActivityType activity, int zone) {
        int size = activities.get(activity).get(zone).size();
        if (size == 0) {
//        	throw new RuntimeException("this should not happen");
        	double dx = (maxX - minX) / zonesX;
        	double dy = (maxY - minY) / zonesY;
        	int x = zone % zonesX;
        	int y = zone / zonesX;
        	return new Building(dx * (x + r.nextDouble()), dy * (y + r.nextDouble()), "placeholder", zone);
//            return getRandomResidential(44);
        }
        return activities.get(activity).get(zone).get(r.nextInt(size));
    }

    public void init() {
        OsmData res = new OsmDataImpl();
        new OsmFileReader(res).readFile("original-input-data/plymouth/osm/home.xml");
        activities.put(ActivityType.HOME, bucketBuildings(res, true));
        OsmData work = new OsmDataImpl();
        new OsmFileReader(work).readFile("original-input-data/plymouth/osm/work.xml");
        activities.put(ActivityType.WORK, bucketBuildings(work, false));
        OsmData shop = new OsmDataImpl();
        new OsmFileReader(shop).readFile("original-input-data/plymouth/osm/shop.xml");
        activities.put(ActivityType.SHOP, bucketBuildings(shop, false));
        OsmData leisure = new OsmDataImpl();
        new OsmFileReader(leisure).readFile("original-input-data/plymouth/osm/leisure.xml");
        activities.put(ActivityType.LEISURE, bucketBuildings(leisure, false));
        OsmData studyC = new OsmDataImpl();
        new OsmFileReader(studyC).readFile("original-input-data/plymouth/osm/study1.xml");
        activities.put(ActivityType.STUDY_CHILD, bucketBuildings(studyC, false));
        OsmData studyA = new OsmDataImpl();
        new OsmFileReader(studyA).readFile("original-input-data/plymouth/osm/study2.xml");
        activities.put(ActivityType.STUDY_ADULT, bucketBuildings(studyA, false));
    }

    // filterRes -- to filter out buildings with "building=yes", but also other tags.
    // (used to mark homes more accurately).
    private ArrayList<ArrayList<Building>> bucketBuildings(OsmData data, boolean filterRes) {
        var buildings = new ArrayList<ArrayList<Building>>();
        List<Id<Node>> toDelete = new ArrayList<>();
        
        for (int i = 0; i < zonesX * zonesY; i++) {
            buildings.add(new ArrayList<>());
        }

        for (Way w : data.getWays().values()) {
        	if (filterRes && w.getTags() != null && w.getTags().keySet().size() > 1 
        			&& w.getTags().containsKey("buildings") && w.getTags().get("building").equals("yes")) {
    			continue;
        	}
        	
        	boolean isLand = w.getTags().containsKey("landuse") && !w.getTags().containsKey("building");
        	if (isLand) {
        		String huy = "fdsfsdf";
        	}
        	
        	List<Node> ns = w.getNodes();
        	if (ns.size() <= 2) {
        		continue;
        	}
            List<Double> coords = new LinkedList<>();
            for (Osm.Node n : ns) {
            	coords.add(n.getCoord().getY());
            	coords.add(n.getCoord().getX());
            	// TODO: Повтор?
            }
            
            Polygon poly = gb.polygon(coords.stream().mapToDouble(Double::doubleValue).toArray());
            try {
				Geometry transformed = JTS.transform(poly, trans);
				int magnitude = (int) Math.ceil(transformed.getArea() / (isLand ? areaScale : buildingScale));
				Envelope box = transformed.getEnvelopeInternal();
				double dx = box.getMaxX() - box.getMinX();
				double dy = box.getMaxY() - box.getMinY();
				int shot = 1;
				while (shot <= magnitude) {
					Point p = gb.point(box.getMinX() + r.nextDouble() * dx, box.getMinY() + r.nextDouble() * dy);
					if (transformed.contains(p)) {
						shot++;
		                int x = (int) ((p.getX() - minX) / (maxX - minX) * zonesX);
		                int y = (int) ((p.getY() - minY) / (maxY - minY) * zonesY);
		                if (x < 0 || x >= zonesX || y < 0 || y >= zonesY) {
		                	continue;
		                }
						buildings.get(y * zonesX + x).add(new Building(p.getX(), p.getY(), "none", y * zonesX + x));
					}
				}
			} catch (MismatchedDimensionException | TransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        for (Node n : data.getNodes().values()) {
        	if (!n.getTags().containsKey("amenity")) {
        		continue;
        	}
        	
			Coordinate transformed = null;
			try {
				transformed = JTS.transform(gb.point(n.getCoord().getX(), n.getCoord().getY()), trans).getCoordinate();
			} catch (MismatchedDimensionException | TransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            int x = (int) ((transformed.getX() - minX) / (maxX - minX) * zonesX);
            int y = (int) ((transformed.getY() - minY) / (maxY - minY) * zonesY);
            if (x < 0 || x >= zonesX || y < 0 || y >= zonesY) {
            	continue;
            }
			buildings.get(y * zonesX + x).add(new Building(transformed.getX(), transformed.getY(), "none", y * zonesX + x));
        }

        return buildings;
    }

    public int getRandomZone(ActivityType activity) {
        int total = (int) activities.get(activity).stream().mapToInt(Collection::size).sum();
        return activities.get(activity).stream().flatMap(List::stream).skip(r.nextInt(total)).findFirst().get().getZone();
    }
    
    public void printMatrix(ActivityType activity) {
    	for (int i = 0; i < zonesY; i++) {
    		for (int j = 0; j < zonesX; j++) {
    			System.out.print(activities.get(activity).get(i * zonesX + j).size() + "\t");
    		}
    		System.out.println();
    	}
    }
    
    public List<Integer> getTop(ActivityType activity) {
    	return activities.get(activity).stream()
    			.sorted(Comparator.comparing(List<Building>::size).reversed())
				.limit((long)(zonesX * zonesY * 0.05))
				.map(l -> l.get(0).getZone())
				.collect(Collectors.toList());
    }
    
    public List<Integer> getCount(ActivityType activity) {
    	return activities.get(activity).stream()
    			.map(List<Building>::size)
    			.collect(Collectors.toList());
    }
}