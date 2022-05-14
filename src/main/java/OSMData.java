import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Building;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class OSMData {

    Random r = new Random(123);
    private int zonesX;
    private int zonesY;

    private final double minX = 50.3578;
    private final double minY = -4.2047;
    private final double maxX = 50.4372;
    private final double maxY = -4.0830;
    Map<Long, List<Double>> nodes = new HashMap<>();

    ArrayList<ArrayList<Building>> residential = new ArrayList<ArrayList<Building>>();
    ArrayList<ArrayList<Building>> work = new ArrayList<ArrayList<Building>>();

    public OSMData(int zonesX, int zonesY) {
        this.zonesX = zonesX;
        this.zonesY = zonesY;

        for (int i = 0; i < zonesX * zonesY; i++) {
            residential.add(new ArrayList<>());
            work.add(new ArrayList<>());
        }
    }

    // TODO: УЖОС
    public Building getRandomResidential(int zone) {
        int size = residential.get(zone).size();
        if (size == 0) {
            return getRandomResidential(44);
        }
        return residential.get(zone).get(r.nextInt(size));
    }

    public Building getRandomWorkplace(int zone) {
        int size = work.get(zone).size();
        if (size == 0) {
            return getRandomWorkplace(44);
        }
        return work.get(zone).get(r.nextInt(size));
    }

    public void init() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            OverpassJson resData = mapper.readValue(new File("src/main/resources/residential.json"), OverpassJson.class);
            OverpassJson workData = mapper.readValue(new File("src/main/resources/work.json"), OverpassJson.class);

            nodes = getNodes(resData);
            nodes.putAll(getNodes(workData));

            residential = bucketBuildings(resData);
            work = bucketBuildings(workData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Map<Long, List<Double>> getNodes(OverpassJson data) {
        HashMap<Long, List<Double>> res = new HashMap<>();
        for (Iterator<JsonNode> it = data.getElements().elements(); it.hasNext(); ) {
            JsonNode address = it.next();
            if (!address.get("type").toString().equals("\"node\"")) { continue; }
            res.put(address.get("id").asLong(), Arrays.asList(address.get("lat").asDouble(), address.get("lon").asDouble()));
        }
        return res;
    }

    private ArrayList<ArrayList<Building>> bucketBuildings(OverpassJson data) {
        var res = new ArrayList<ArrayList<Building>>();
        for (int i = 0; i < zonesX * zonesY; i++) {
            res.add(new ArrayList<>());
        }

        for (Iterator<JsonNode> it = data.getElements().elements(); it.hasNext(); ) {
            JsonNode address = it.next();
            int x, y;
            double lat, lon;
            if (address.get("nodes") != null) {
                List<Double> coords = nodes.get(address.get("nodes").elements().next().asLong());
                x = (int) ((coords.get(0) - minX) / (maxX - minX) * 10);
                y = (int) ((coords.get(1) - minY) / (maxY - minY) * 10);
                lat = coords.get(0);
                lon = coords.get(1);
            } else if (!address.get("type").toString().equals("\"relation\"") && address.get("tags") != null) {
                x = (int) ((address.get("lat").asDouble() - minX) / (maxX - minX) * 10);
                y = (int) ((address.get("lon").asDouble() - minY) / (maxY - minY) * 10);
                lat = address.get("lat").asDouble();
                lon =address.get("lon").asDouble();
            } else {
                continue;
            }

            if (x >= zonesX) { x--; }
            if (y >= zonesY) { y--; }

            Building b = new Building();
            if (address.has("tags") && address.get("tags").has("amenity")) {
                b.setType(address.get("tags").get("amenity").toString());
            } else if (address.has("tags") && address.get("tags").has("building")) {
                b.setType(address.get("tags").get("building").toString());
            } else { b.setType(""); }

            b.setLatitude(lat);
            b.setLongitude(lon);
            res.get(y * zonesX + x).add(b);
        }

        return res;
    }

    public int getRandomWorkZone() {
        int total = (int) work.stream().mapToInt(Collection::size).sum();
        int rand = r.nextInt(total);

        for (int i = 0; i < zonesX * zonesY; i++) {
            rand -= work.get(i).size();
            if (rand < 0) { return i; }
        }

        throw new RuntimeException("Should not happen");
    }

    public int getRandomResidentialZone() {
        int total = (int) residential.stream().mapToInt(Collection::size).sum();
        int rand = r.nextInt(total);

        for (int i = 0; i < zonesX * zonesY; i++) {
            rand -= residential.get(i).size();
            if (rand < 0) { return i; }
        }

        throw new RuntimeException("Should not happen");
    }
}
