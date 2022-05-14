import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

import org.locationtech.proj4j.ProjCoordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;

import model.LinkHourlyCount;
import osm.Osm;

public class CountsGenerator {
    private static Map<String, LinkHourlyCount> counts = new HashMap<>();
    private static Map<String, LinkHourlyCount> base = new HashMap<>();
    private static Map<String, List<Link>> mapping = new HashMap<>();
    private static Map<String, osm.Node> refs = new HashMap<>();
    private static Osm osm;
    private static int coef = 10; // Population scale.
    private static Network net = ScenarioUtils.loadScenario(ConfigUtils.loadConfig( "scenarios/plymouth/output_network_config.xml")).getNetwork();

    public static void gunzip(Path fin) throws IOException {

        try(final InputStream in = new GZIPInputStream(Files.newInputStream(fin));
            BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                String type = searchAttribute(line, "type");
                if (type.equals("vehicle enters traffic")
                        || type.equals("entered link")) {
                    double time = Double.parseDouble(searchAttribute(line, "time"));
                    String link = searchAttribute(line, "link");
                    counts.merge(link, LinkHourlyCount.linkInit((int) ((time-1) / 3600.0)), (v1, v2) -> v1.inc((int) ((time-1) / 3600.0)));
                }
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("postproc/hourly_counts.csv"))) {
            writer.write("link;01:00:00;02:00:00;03:00:00;04:00:00;05:00:00;06:00:00;07:00:00;08:00:00;09:00:00;10:00:00;11:00:00;12:00:00;13:00:00;14:00:00;15:00:00;16:00:00;17:00:00;18:00:00;19:00:00;20:00:00;21:00:00;22:00:00;23:00:00;24:00:00;25:00:00;26:00:00;27:00:00;28:00:00;29:00:00;30:00:00");
//            writer.write(IntStream.rangeClosed(0, LinkHourlyCount.maxHour-1).mapToObj(String::valueOf).collect(Collectors.joining(";")));
            writer.write("\n");

            for (var lhc : counts.entrySet()) {
                writer.write(lhc.getKey() + ";");
                writer.write(Arrays.stream(lhc.getValue().getCounts()).map(x -> x * coef).mapToObj(String::valueOf).collect(Collectors.joining(";")));
                writer.write("\n");
            }
        }
    }

    private String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        return resultStringBuilder.toString();
    }

    private static String searchAttribute(String line, String attribute) {
        if (!line.contains(attribute)) { return ""; }
        int left = line.indexOf(attribute + "=\"") + attribute.length() + 2;
        int right = line.indexOf("\"", left);
        return line.substring(left, right);
    }

    public static void addToBase(String point_id, String dir, int hour, double lat, double lon, int cnt) throws IOException {

        ProjCoordinate station = MatsimGenerator.transformFromWGS84(lat, lon);
        Coord c = new Coord(station.x, station.y);

        double dist = Double.MAX_VALUE;
        Link closest = null;
        List<Link> links = deduceLinkId(point_id, lat, lon);
        for (Link l : links) {
            Coord first = l.getFromNode().getCoord();
            Coord last = l.getToNode().getCoord();
            double dX = last.getX() - first.getX();
            double dY = last.getY() - first.getY();
            if ((dir.equals("N") || dir.equals("n")) && dY > 0
                    || (dir.equals("S") || dir.equals("s")) && dY <= 0
                    || (dir.equals("E") || dir.equals("e")) && dX > 0
                    || (dir.equals("W") || dir.equals("w")) && dX <= 0) {
            	double newdist = NetworkUtils.getEuclideanDistance(c, NetworkUtils.findNearestPointOnLink(c, l));
                if (newdist < dist) {
                	dist = newdist;
                	closest = l;
                }
            }
        }

        if (closest == null) {
        	System.err.println("Could not find link near " + c.toString());
        	return;
        }
        base.merge(closest.getId().toString(), LinkHourlyCount.linkInit(hour, cnt), (v1, v2) -> v1.set(hour, cnt));
    }
    
    public static List<Link> deduceLinkId(String count_point, double lat, double lon) throws IOException {

        if (mapping.containsKey(count_point)) {
            return mapping.get(count_point);
        }
        
        mapping.put(count_point, getBestWays(lat, lon));

        return mapping.get(count_point);
    }

    private static List<Link> getBestWays(double lat, double lon) {
//        Map<Way, Double> distSq = new HashMap<>();
        

        ProjCoordinate station = MatsimGenerator.transformFromWGS84(lat, lon);
        Coord c = new Coord(station.x, station.y);
        
        List<Link> links = NetworkUtils.getNearestNodes(net, c, 40.0).stream()
        		.map(n -> NetworkUtils.getIncidentLinks(n).values())
        		.flatMap(s -> s.stream())
        		.collect(Collectors.toList());
        return links;
//        for (Way l : )) {
//            List<Node> nodes = w.getNds().stream()
//                    .map(nd -> refs.get(nd.getRef())).collect(Collectors.toList());
//
//            double minD = Integer.MAX_VALUE;
//            int minI = 0;
//            Node n;
//            for (int i = 0; i < nodes.size(); i++) {
//                n = nodes.get(i);
//                double d = (n.lat - lat) * (n.lat - lat) + (n.lon - lon) * (n.lon - lon);
//                if (d < minD) {
//                    minD = d;
//                    minI = i;
//                }
//            }
//
//            Node clos1, clos2;
//            if (minI == 0) {
//                clos1 = nodes.get(minI);
//                clos2 = nodes.get(minI+1);
//            } else if (minI == nodes.size()-1) {
//                clos1 = nodes.get(minI-1);
//                clos2 = nodes.get(minI);
//            } else {
//                Node n1 = nodes.get(minI-1);
//                Node n2 = nodes.get(minI+1);
//                if (((n1.lat - lat) * (n1.lat - lat) + (n1.lon - lon) * (n1.lon - lon)) > ((n2.lat - lat) * (n2.lat - lat) + (n2.lon - lon) * (n2.lon - lon))) {
//                    clos1 = nodes.get(minI);
//                    clos2 = n2;
//                } else {
//                    clos1 = n1;
//                    clos2 = nodes.get(minI);
//                }
//            }
//
//            distSq.put(new Way(null, new ArrayList<>(Arrays.asList(new Nd(clos1.getId()), new Nd(clos2.getId()))), null), minD);
//
//            double min = Double.MAX_VALUE;
//            for (Nd nd : w.getNds()) {
//                Node n = refs.get(nd.getRef());
//                double temp = (n.lat - lat) * (n.lat - lat) + (n.lon - lon) * (n.lon - lon);
//                if (temp < min) { min = temp; }
//            }
//
//            distSq.put(w, min);
//        }
//
//        return distSq.entrySet().stream()
//                .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
//                .limit(2)
//                .map(Map.Entry::getKey)
//                .collect(Collectors.toList());
    }

    public static Map<String, LinkHourlyCount> readData(String countsFile) throws IOException {
        double minX = 50.3578;
        double minY = -4.2047;
        double maxX = 50.4372;
        double maxY = -4.0830;
        int year = 2019;

        BufferedReader br = new BufferedReader(new FileReader(countsFile));
        try {
            br.readLine();
            String line = br.readLine().replace(", ", "");

            while (line != null && !line.isEmpty()) {
                String[] tokens = line.split(",", 34);

                int yr = Integer.parseInt(tokens[3]);
                double lat = Double.parseDouble(tokens[15]);
                double lon = Double.parseDouble(tokens[16]);
                if ( yr == year && lat > minX && lat < maxX && lon > minY && lon < maxY) {
                    String point_id = tokens[1];
                    String dir = tokens[2];
                    int cnt = Integer.parseInt(tokens[22]) + Integer.parseInt(tokens[23]) + Integer.parseInt(tokens[24]);
                    int hour = Integer.parseInt(tokens[5]);
                    System.out.println("Adding " + point_id + ", hour " + hour);

                    int tries = 1;
                    while (tries > 0) {
                        try {
                            addToBase(point_id, dir, hour, lat, lon, cnt);
                            tries = 0;
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                            tries--;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException r) {
                                System.err.println(r.getMessage());
                            }
                        }
                    }
                }
                line = br.readLine();
                if (line != null) {
                    line = line.replace(", ", "");
                }
            }
        } finally {
            br.close();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("postproc/count_osm_mapped.csv"))) {
            writer.write("link;01:00:00;02:00:00;03:00:00;04:00:00;05:00:00;06:00:00;07:00:00;08:00:00;09:00:00;10:00:00;11:00:00;12:00:00;13:00:00;14:00:00;15:00:00;16:00:00;17:00:00;18:00:00;19:00:00;20:00:00;21:00:00;22:00:00;23:00:00;24:00:00;25:00:00;26:00:00;27:00:00;28:00:00;29:00:00;30:00:00");
            writer.write("\n");

            for (var lhc : base.entrySet()) {
                writer.write(lhc.getKey() + ";");
                writer.write(Arrays.stream(lhc.getValue().getCounts()).mapToObj(String::valueOf).collect(Collectors.joining(";")));
                writer.write("\n");
            }
        }

        return base;
    }

    public static void mapToNetwork() throws IOException {
//        ArrayList<String> from = base.keySet().stream().map(s -> s.split(";")[0]).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> from = new ArrayList<>();
        ArrayList<String> to = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("scenarios/plymouth/count_osm.csv"))) {
            br.readLine();
            String line = br.readLine();

            while (line != null && !line.isEmpty()) {
                String[] tokens = line.split(";");

                from.add(tokens[0]);
                to.add(tokens[1]);
                values.add(line.substring(tokens[0].length() + tokens[1].length() + 2));

                line = br.readLine();
            }
        }

        List<String> res = new LinkedList<>();

        for (Link l : net.getLinks().values()) {
            org.matsim.api.core.v01.network.Node fromNode = l.getFromNode();
            org.matsim.api.core.v01.network.Node toNode = l.getToNode();

            for (int i = 0; i < from.size(); i++) {
                if (fromNode.getId().toString().equals(from.get(i))
                    && toNode.getId().toString().equals(to.get(i))) {
                    res.add(l.getId().toString() + ";" + values.get(i));

                    from.remove(i);
                    to.remove(i);
                    values.remove(i);
                    break;
                }
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("postproc/count_osm_mapped.csv"))) {
            writer.write("link;01:00:00;02:00:00;03:00:00;04:00:00;05:00:00;06:00:00;07:00:00;08:00:00;09:00:00;10:00:00;11:00:00;12:00:00;13:00:00;14:00:00;15:00:00;16:00:00;17:00:00;18:00:00;19:00:00;20:00:00;21:00:00;22:00:00;23:00:00;24:00:00;25:00:00;26:00:00;27:00:00;28:00:00;29:00:00;30:00:00");
            writer.write("\n");

            for (var line : res) {
                writer.write(line);
                writer.write("\n");
            }
        }
    }

    public static double calculateError() throws IOException {
        int start = 8;
        int end = 19;
        Map<String, LinkHourlyCount> stations = base;

        double[] MAE = new double[30];
        double[] RMSE = new double[30];
        int[] total_stations = new int[30];
        int[] total_sim = new int[30];

        try (BufferedReader br = new BufferedReader(new FileReader("postproc/hourly_counts.csv"))) {
            br.readLine();
            String line = br.readLine();


            while (line != null && !line.isEmpty()) {
                String[] tokens = line.split(";");

                if (!stations.containsKey(tokens[0])) {
                    line = br.readLine();
                    continue;
                }

                int[] cnt = stations.get(tokens[0]).getCounts();

                for (int i = start; i <= end; i++) {
                    int current = Integer.parseInt(tokens[i+1]);
                    total_sim[i] += current;
                    total_stations[i] += cnt[i-1];
                    MAE[i] += Math.abs(current - cnt[i-1]);
                    RMSE[i] += Math.pow(current - cnt[i-1], 2);
                }

                line = br.readLine();
            }
        }

        for (int i = start; i <= end; i++) {
            MAE[i] /= stations.size();
            RMSE[i] = Math.sqrt(RMSE[i] / stations.size());
        }

        double avgMAE = Arrays.stream(Arrays.copyOfRange(MAE, start, end+1)).average().getAsDouble();
        double avgRMSE = Arrays.stream(Arrays.copyOfRange(RMSE, start, end+1)).average().getAsDouble();
        int totalStations = Arrays.stream(total_stations).sum();
        int totalSim = Arrays.stream(total_sim).sum();
        return avgRMSE;
    }
    
    public static Map<String, LinkHourlyCount> readSpecifiedStations(String countsFile, List<Integer> stations) throws IOException {
        int year = 2019;
        Map<String, LinkHourlyCount> temp = new HashMap<>();
        
        BufferedReader br = new BufferedReader(new FileReader(countsFile));
        try {
            br.readLine();
            String line = br.readLine().replace(", ", "");

            while (line != null && !line.isEmpty()) {
                String[] tokens = line.split(",", 34);

                double lat = Double.parseDouble(tokens[15]);
                double lon = Double.parseDouble(tokens[16]);
                int yr = Integer.parseInt(tokens[3]);
                if ( yr == year) {
                    String point_id = tokens[1];
                    if (!stations.contains(Integer.parseInt(point_id))) {
                        line = br.readLine();
                        if (line != null) {
                            line = line.replace(", ", "");
                        }
                    	continue;
                    }
                    String dir = tokens[2];
                    int cnt = Integer.parseInt(tokens[33]);
                    int hour = Integer.parseInt(tokens[5]);
                    System.out.println("Adding " + point_id + ", hour " + hour);

                    int tries = 1;
                    temp.merge(lat + ";" + lon + ";" + dir, LinkHourlyCount.linkInit(hour, cnt), (v1, v2) -> v1.set(hour, cnt));
                }
                line = br.readLine();
                if (line != null) {
                    line = line.replace(", ", "");
                }
            }
        } finally {
            br.close();
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("postproc/count_boundaries.csv"))) {
            writer.write("lat;lon;dir;01:00:00;02:00:00;03:00:00;04:00:00;05:00:00;06:00:00;07:00:00;08:00:00;09:00:00;10:00:00;11:00:00;12:00:00;13:00:00;14:00:00;15:00:00;16:00:00;17:00:00;18:00:00;19:00:00;20:00:00;21:00:00;22:00:00;23:00:00;24:00:00;25:00:00;26:00:00;27:00:00;28:00:00;29:00:00;30:00:00");
            writer.write("\n");

            for (var lhc : temp.entrySet()) {
                writer.write(lhc.getKey() + ";");
                writer.write(Arrays.stream(lhc.getValue().getCounts()).mapToObj(String::valueOf).collect(Collectors.joining(";")));
                writer.write("\n");
            }
        }
        
        return temp;
    }
}
