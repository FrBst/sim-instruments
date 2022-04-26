import generated.CountType;
import generated.CountsType;
import org.matsim.counts.Count;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CountsGenerator {
    File measures;
    CountsType counts = new CountsType();

    public CountsGenerator(String countsPath) {
        measures = new File(countsPath);
        counts.setName("plymouth");
        counts.setYear(2022);


    }

    private readData() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(measures));

        Map<String, CountType> stations = new HashMap<>();

        int last_count_point = 0;
        String line = br.readLine();
        while (line != null) {
            String[] tokens = line.split(",");
            if (!stations.containsKey(tokens[1])) {
                stations.put(tokens[1], new CountType());
                stations.get(tokens[1]).set
            }
        }
    }
}
