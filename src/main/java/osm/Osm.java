package osm;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@XmlRootElement(name = "osm")
@XmlAccessorType(XmlAccessType.FIELD)
public class Osm {
    @XmlElement(name = "way")
    public List<Way> ways;
    @XmlElement(name = "node")
    public List<Node> nodes;
}
