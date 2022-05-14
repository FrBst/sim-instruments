package osm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Way {
    @XmlAttribute(name = "id")
    public String id;
    @XmlElement(name = "nd")
    public ArrayList<Nd> nds;
    @XmlElement(name = "tag")
    public List<Tag> tags;
}
