import model.Activity;
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
        CRSFactory factory = new CRSFactory();
        CoordinateReferenceSystem srcCrs = factory.createFromName("EPSG:4326");
        CoordinateReferenceSystem dstCrs = factory.createFromName("EPSG:27700");
        BasicCoordinateTransform transform = new BasicCoordinateTransform(srcCrs, dstCrs);
        // Note these are x, y so lng, lat
        ProjCoordinate srcCoord;
        ProjCoordinate dstCoord = new ProjCoordinate();

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

                    Building b;
                    if (a.getOType().equals("home")) {
                        b = osm.getRandomResidential(a.getOZone());
                    } else if (a.getOType().equals("work")) {
                        b = osm.getRandomWorkplace(a.getOZone());
                    } else {
                        b = osm.getRandomWorkplace(a.getOZone());
                    }
                    srcCoord = new ProjCoordinate(b.getLongitude(), b.getLatitude());
                    transform.transform(srcCoord, dstCoord);

                    writer.write("\t\t\t<activity type=\"" + a.getOType() + "\" ");
                    writer.write("x=\"" + dstCoord.x + "\" y=\"" + dstCoord.y + "\" ");
                    writer.write("end_time=\"" + (180 + a.getOEndTime()) / 60 % 24 + ":" + a.getOEndTime() % 60 + ":00\"/>\n");

                    if (i == p.getActivities().size() - 1) {
                        writer.write("\t\t\t<leg mode=\"" + a.getModeBefore() + "\"/>\n");
                        if (a.getDType().equals("home")) {
                            b = osm.getRandomResidential(a.getDZone());
                        } else if (a.getDType().equals("work")) {
                            b = osm.getRandomWorkplace(a.getDZone());
                        } else {
                            b = osm.getRandomWorkplace(a.getDZone());
                        }
                        srcCoord = new ProjCoordinate(b.getLongitude(), b.getLatitude());
                        transform.transform(srcCoord, dstCoord);
                        writer.write("\t\t\t<activity type=\"" + a.getDType() + "\" ");
                        writer.write("x=\"" + dstCoord.x + "\" y=\"" + dstCoord.y + "\"/>\n");
                    }
                }

                writer.write("\t\t</plan>\n");
                writer.write("\t</person>\n");
            }
            writer.write("</population>\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
