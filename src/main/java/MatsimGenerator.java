import model.Activity;
import model.ActivityType;
import model.Building;
import model.Person;

import org.locationtech.jts.index.bintree.Key;
import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MatsimGenerator {
	private static OSMData osm;
    private static CRSFactory factory = new CRSFactory();
    private static CoordinateReferenceSystem srcCrs = factory.createFromName("EPSG:4326");
    private static CoordinateReferenceSystem dstCrs = factory.createFromName("EPSG:27700");
    private static BasicCoordinateTransform transform = new BasicCoordinateTransform(srcCrs, dstCrs);
    private static ProjCoordinate srcCoord;
    private static ProjCoordinate dstCoord = new ProjCoordinate();

    public MatsimGenerator(int x, int y) {
    	osm = new OSMData(x, y);
    	osm.init();
    }

    public void generatePlans() throws IOException {
        Map<String, String> tours = new HashMap<>();
        for (String line : mergeFiles("Tours")) {
            String[] tokens = line.split("\t");
            String mode = tokens[4];
            tours.put(tokens[0] + "." + tokens[1] + "." + tokens[2], mode);
        }

        HashMap<String, Person> persons = new HashMap<>();
        for (String line : mergeFiles("Stops")) {
            String[] tokens = line.split("\t");
            String id = tokens[0] + "." + tokens[1] + "." + tokens[2];
            String personId = tokens[0] + "." + tokens[1];
            if (!persons.containsKey(personId)) {
                Person p = new Person();
                p.setHid(Integer.parseInt(tokens[0]));
                p.setPid(Integer.parseInt(tokens[1]));
                persons.put(personId, p);
            }

            Activity a = new Activity();
            a.setModeBefore(tours.get(id));
            a.setOType(tokens[11]);
            a.setDType(tokens[4]);
            a.setOZone(Integer.parseInt(tokens[9]));
            a.setDZone(Integer.parseInt(tokens[8]));
            a.setOEndTime(Integer.parseInt(tokens[5]));
            persons.get(personId).getActivities().add(a);
        }

        write("scenarios/plymouth/plans.xml", "scenarios/plymouth/personAttributes.xml", new LinkedList<>(persons.values()));

        String hsdfsdf = "fsdfsdf";
    }

    private List<String> mergeFiles(String name) throws IOException {
        List<String> res = new LinkedList<>();
        for (int i = 1; i <= 10; i++) {
            BufferedReader br = new BufferedReader(new FileReader("original-input-data/plymouth/cemdap/" + name + ".out" + i));
            try {
                String line = br.readLine();

                while (line != null && !line.isEmpty()) {
                    res.add(line);
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
        }

        return res;
    }

    public void write(String plansFile, String attributesFile, List<Person> persons) {

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(plansFile))) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE population SYSTEM \"http://www.matsim.org/files/dtd/population_v6.dtd\">\n" +
                    "\n" +
                    "<population>\n");

            for (Person p : persons) {
                writer.write("\t<person id=\"" + p.getPid() + "\">\n");
                writer.write("\t\t<attributes>\n");
                writer.write("\t\t\t<attribute name=\"subpopulation\" class=\"java.lang.String\">default</attribute>\n");
                writer.write("\t\t</attributes>\n");
                writer.write("\t\t<plan selected=\"yes\">\n");

                p.getActivities().sort(Comparator.comparingInt(Activity::getOEndTime));
                Building spawn = null;
                for (int i = 0; i < p.getActivities().size(); i++) {
                    Activity a = p.getActivities().get(i);
                    if (i > 0) {
                        writer.write("\t\t\t<leg mode=\"" + a.getModeBefore() + "\"/>\n");
                    }

                    Building b = osm.getRandomBuilding(a.getOType(), a.getOZone());

                    /// !!!!
                    if (i == 0) {
                        if (!a.getOType().equals(ActivityType.HOME)) {
                        	System.err.println("inconsistency in plan?");
                        }
                    	spawn = b;
                    }

                    writer.write("\t\t\t<activity type=\"" + a.getOType() + "\" ");
                    writer.write("x=\"" + b.getLatitude() + "\" y=\"" + b.getLongitude() + "\" ");
                    writer.write("end_time=\"" + (180 + a.getOEndTime()) / 60 % 24 + ":" + a.getOEndTime() % 60 + ":00\"/>\n");

                    if (i == p.getActivities().size() - 1) {
                        writer.write("\t\t\t<leg mode=\"" + a.getModeBefore() + "\"/>\n");
                        b = osm.getRandomBuilding(a.getDType(), a.getDZone());

                        /// !!!!
                    	if (!a.getDType().equals(ActivityType.HOME)) {
                        	System.out.println("inconsistency in plan? (end)");
                        }
                    	b = spawn;
                        
                        
                        writer.write("\t\t\t<activity type=\"" + a.getDType() + "\" ");
                        writer.write("x=\"" + b.getLatitude() + "\" y=\"" + b.getLongitude() + "\"/>\n");
                    }
                }

                writer.write("\t\t</plan>\n");
                writer.write("\t</person>\n");
            }
            
            writeRandomFreight(writer);
            
            writer.write("</population>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ProjCoordinate transformFromWGS84(double lat, double lon) {
        srcCoord = new ProjCoordinate(lon, lat);
        dstCoord = new ProjCoordinate();
        transform.transform(srcCoord, dstCoord);
        return dstCoord;
    }
    
    private static void writeRandomFreight(BufferedWriter writer) throws IOException  {
    	Map<Building, Integer> counts = new ConcurrentHashMap<>(new LinkedHashMap<>());
    	
    	// Цифры с счетных станций
    	ProjCoordinate p = transformFromWGS84(50.3807795,-4.000795);
    	counts.put(new Building(p.x, p.y, "Lnd", -99), (int) (27000 / 0.7427 / 10));
    	
    	p = transformFromWGS84(50.4183,-4.2589);
    	counts.put(new Building(p.x, p.y, "Sal", -99), (int) (17500 / 0.7427 / 10)); // 0.0765 = prob between 13 and 14 
    	
    	p = transformFromWGS84(50.4704,-4.0985);
    	counts.put(new Building(p.x, p.y, "Nor", -99), (int) (6800 / 0.7427 / 10)); // 0.7427 -- [8;20).
    	
    	p = transformFromWGS84(50.3481, -4.0202);
    	counts.put(new Building(p.x, p.y, "Sth", -99), (int) (1280 / 0.7427 / 10));
    	
    	Random r = new Random(12353);
    	
    	for (var entry : counts.entrySet()) {
    		for (int i = 0; i < entry.getValue(); i++) {
    			int sec = (int) ((r.nextGaussian() * 5.2 + 13) * 3600);
    			
    			if (sec < 14400 || sec >= 72000) { // [4, 20)
    				continue;
    			}
    			
    			Building dest = null;
    			boolean through = true;
    			
//    			do {
    			int transit = counts.entrySet().stream()
    					.filter(e -> !e.getKey().equals(entry.getKey()))
    					.map(e -> e.getValue())
    					.reduce(0, Integer::sum);
    			if (transit == 0) {
	    			dest = osm.getRandomBuilding(ActivityType.WORK, osm.getRandomZone(ActivityType.WORK));
	    			through = false;
	            	i += 1;
    			} else {
        			int rand = (int) (r.nextInt(transit));
        			for (var w : counts.entrySet().stream().filter(e -> !e.getKey().equals(entry.getKey())).collect(Collectors.toList())) {
        				rand -= w.getValue();
        				if (rand < 0) {
        					dest = w.getKey();
                			counts.merge(dest, -1, Integer::sum);
        					break;
        				}
    				}
    			}
//    			} while (entry.getKey().getType().equals("Sth") && dest.getType().equals("Lnd")
//    					|| entry.getKey().getType().equals("Lnd") && dest.getType().equals("Sth")
    			
    			int hour = sec / 60 / 60;
    			int minute = sec / 60 % 60;
    			int second = sec / 3600;
    			
    			Building b1, b2;
    			if (r.nextDouble() > 0.5) {
    				b1 = entry.getKey();
    				b2 = dest;
    			} else {
    				b2 = entry.getKey();
    				b1 = dest;
    			}
    			
    			if (through) {
        			writer.write("\t<person id=\"" + entry.getKey().getType() + i + "\">\n");
                    writer.write("\t\t<attributes>\n");
                    writer.write("\t\t\t<attribute name=\"subpopulation\" class=\"java.lang.String\">freight</attribute>\n");
                    writer.write("\t\t</attributes>\n");
                    writer.write("\t\t<plan selected=\"yes\">\n");
                    writer.write("\t\t\t<activity type=\"home\" ");
                    writer.write("x=\"" + b1.getLatitude() + "\" y=\"" + b1.getLongitude() + "\" ");
                    writer.write("end_time=\"" + (hour-3) + ":" + minute + ":" + second + "\"/>\n");
                    writer.write("\t\t\t<leg mode=\"freight\"/>\n");
                    writer.write("\t\t\t<activity type=\"warehouse\" ");
                    writer.write("x=\"" + b2.getLatitude() + "\" y=\"" + b2.getLongitude() + "\" ");
                    writer.write("end_time=\"" + hour + ":" + minute + ":" + second + "\"/>\n");
                    writer.write("\t\t\t<leg mode=\"freight\"/>\n");
                    writer.write("\t\t\t<activity type=\"home\" ");
                    writer.write("x=\"" + b1.getLatitude() + "\" y=\"" + b1.getLongitude() + "\"/>\n");
                    writer.write("\t\t</plan>\n");
                    writer.write("\t</person>\n");
    			} else {
        			writer.write("\t<person id=\"" + entry.getKey().getType() + i + "\">\n");
                    writer.write("\t\t<attributes>\n");
                    writer.write("\t\t\t<attribute name=\"subpopulation\" class=\"java.lang.String\">freight</attribute>\n");
                    writer.write("\t\t</attributes>\n");
                    writer.write("\t\t<plan selected=\"yes\">\n");
                    writer.write("\t\t\t<activity type=\"home\" ");
                    writer.write("x=\"" + b1.getLatitude() + "\" y=\"" + b1.getLongitude() + "\" ");
                    writer.write("end_time=\"" + (hour-3) + ":" + minute + ":" + second + "\"/>\n");
                    writer.write("\t\t\t<leg mode=\"freight\"/>\n");
                    writer.write("\t\t\t<activity type=\"warehouse\" ");
                    writer.write("x=\"" + b2.getLatitude() + "\" y=\"" + b2.getLongitude() + "\" ");
                    writer.write("end_time=\"" + hour + ":" + minute + ":" + second + "\"/>\n");
                    writer.write("\t\t\t<leg mode=\"freight\"/>\n");
                    writer.write("\t\t\t<activity type=\"home\" ");
                    writer.write("x=\"" + b1.getLatitude() + "\" y=\"" + b1.getLongitude() + "\"/>\n");
                    writer.write("\t\t</plan>\n");
                    writer.write("\t</person>\n");
    			}
    		}
			
			counts.put(entry.getKey(), 0);
    	}
    }
}
