import model.Activity;
import model.ActivityType;
import model.Building;
import model.Person;
import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MatsimGenerator {
    OSMData osm = new OSMData(10, 10);
    private static CRSFactory factory = new CRSFactory();
    private static CoordinateReferenceSystem srcCrs = factory.createFromName("EPSG:4326");
    private static CoordinateReferenceSystem dstCrs = factory.createFromName("EPSG:27700");
    private static BasicCoordinateTransform transform = new BasicCoordinateTransform(srcCrs, dstCrs);
    private static ProjCoordinate srcCoord;
    private static ProjCoordinate dstCoord = new ProjCoordinate();

    public MatsimGenerator() {
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

        write("scenarios/plymouth/plans.xml", new LinkedList<>(persons.values()));

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

    public void write(String filename, List<Person> persons) {

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE population SYSTEM \"http://www.matsim.org/files/dtd/population_v6.dtd\">\n" +
                    "\n" +
                    "<population>\n");

            for (Person p : persons) {
                writer.write("\t<person id=\"" + p.getPid() + "\">\n");
                writer.write("\t\t<plan selected=\"yes\">\n");

                p.getActivities().sort(Comparator.comparingInt(Activity::getOEndTime));
                for (int i = 0; i < p.getActivities().size(); i++) {
                    Activity a = p.getActivities().get(i);
                    if (i > 0) {
                        writer.write("\t\t\t<leg mode=\"" + a.getModeBefore() + "\"/>\n");
                    }

                    Building b = osm.getRandomBuilding(a.getOType(), a.getOZone());
                    srcCoord = new ProjCoordinate(b.getLongitude(), b.getLatitude());
                    transform.transform(srcCoord, dstCoord);

                    writer.write("\t\t\t<activity type=\"" + a.getOType() + "\" ");
                    writer.write("x=\"" + dstCoord.x + "\" y=\"" + dstCoord.y + "\" ");
                    writer.write("end_time=\"" + (180 + a.getOEndTime()) / 60 % 24 + ":" + a.getOEndTime() % 60 + ":00\"/>\n");

                    if (i == p.getActivities().size() - 1) {
                        writer.write("\t\t\t<leg mode=\"" + a.getModeBefore() + "\"/>\n");
                        b = osm.getRandomBuilding(a.getDType(), a.getDZone());
                        srcCoord = new ProjCoordinate(b.getLongitude(), b.getLatitude());
                        transform.transform(srcCoord, dstCoord);
                        writer.write("\t\t\t<activity type=\"" + a.getDType() + "\" ");
                        writer.write("x=\"" + dstCoord.x + "\" y=\"" + dstCoord.y + "\"/>\n");
                    }
                }

                writer.write("\t\t</plan>\n");
                writer.write("\t</person>\n");
            }
            
            writeRandomFreight(writer);
            
            writer.write("</population>\n");
            writer.close();
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
    
    private static void writeRandomFreight(BufferedWriter writer) throws IOException {
    	Map<Building, Integer> counts = new HashMap<>();
    	Map<ProjCoordinate, Integer> plausibility = new HashMap<>();
    	
    	ProjCoordinate p = transformFromWGS84(50.4151, -4.2497);
    	counts.put(new Building(p.x, p.y, "Saltash", -99), 3000);
    	
    	p = transformFromWGS84(50.4453, -4.1088);
    	counts.put(new Building(p.x, p.y, "North", -99), 1200);
    	
    	p = transformFromWGS84(50.3807, -4.0010);
    	counts.put(new Building(p.x, p.y, "London", -99), 3700);
    	
    	p = transformFromWGS84(50.3545, -4.0510);
    	counts.put(new Building(p.x, p.y, "South", -99), 1500);
    	
    	Random r = new Random(12353);
    	
    	for (var entry : counts.entrySet()) {
    		for (int i = 0; i < entry.getValue(); i++) {
    			int sec = r.nextInt(108000);
    			if (sec < 21600 || sec > 93600) {
    				continue;
    			}
    			
    			Building dest = null;
    			int dice = r.nextInt(counts.values().stream().reduce(0, Integer::sum) - entry.getValue());
    			for (var cand : counts.entrySet()) {
    				if (cand.getKey().equals(entry.getKey())) {
    					continue;
    				}
    				dice -= cand.getValue();
    				if (dice < 0) {
    					dest = cand.getKey();
    				}
    			}
    			
    			int hour = sec / 60 / 60;
    			int minute = sec / 60 % 60;
    			int second = sec / 3600;
    			
    			writer.write("\t<person id=\"" + entry.getKey().getType() + i + "\">\n");
                writer.write("\t\t<plan selected=\"yes\">\n");
                writer.write("\t\t\t<activity type=\"home\" ");
                writer.write("x=\"" + entry.getKey().getLatitude() + "\" y=\"" + entry.getKey().getLongitude() + "\" ");
                writer.write("end_time=\"" + hour + ":" + minute + ":" + second + "\"/>\n");
                writer.write("\t\t\t<leg mode=\"car\"/>\n");
                writer.write("\t\t\t<activity type=\"work\" ");
                writer.write("x=\"" + dest.getLatitude() + "\" y=\"" + dest.getLongitude() + "\" ");
                writer.write("\t\t</plan>\n");
                writer.write("\t</person>\n");
    		}
    	}
    }
}
