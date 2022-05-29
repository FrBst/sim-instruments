package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Building {
	
	@EqualsAndHashCode.Include
    double latitude;
	
	@EqualsAndHashCode.Include
    double longitude;
	
    String type;
    int zone;
}
