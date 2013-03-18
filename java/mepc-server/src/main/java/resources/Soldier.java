package resources;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Soldier {
	@NonNull
	public String id;
	@NonNull
	public String name;
	@NonNull
	public String description;
	public Boolean hired = Boolean.FALSE;
}