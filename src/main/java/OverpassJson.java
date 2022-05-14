import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.Getter;

@Data
public class OverpassJson {
    private String version;
    private String generator;
    private JsonNode osm3s;
    private JsonNode elements;
}
