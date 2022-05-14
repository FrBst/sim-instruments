package osm;

import lombok.Data;

import javax.xml.bind.annotation.*;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Node {
    @XmlAttribute(name = "id")
    public String id;
    @XmlAttribute(name = "lat")
    public double lat;
    @XmlAttribute(name = "lon")
    public double lon;
}