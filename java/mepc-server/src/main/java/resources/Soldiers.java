package resources;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.google.common.collect.ImmutableMap;

@Path("/data")
public class Soldiers {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public class Soldier {
		public String id;
		public String name;
		public String description;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public class ThreeSoldiers {
		public List<Soldier> soldiers;
	}

	@GET
	@Path("soldiers.json")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, List<ThreeSoldiers>> allSoldiers() {
		return ImmutableMap.<String, List<ThreeSoldiers>> of(
				"soldiers",
				newArrayList(
						new ThreeSoldiers(newArrayList(new Soldier("stallone", "Sylvester Stallone", "Lorem ipsum"), new Soldier("statham", "Jason Statham",
								"Lorem ipsum"), new Soldier("li", "Jet Li", "Lorem ipsum"))),
						new ThreeSoldiers(newArrayList(new Soldier("lundgren", "Dolph Lundgren", "Lorem ipsum"), new Soldier("norris", "Chuck Norris",
								"Lorem ipsum"), new Soldier("crews", "Terry Crews", "Lorem ipsum"))),
						new ThreeSoldiers(newArrayList(new Soldier("couture", "Randy Couture", "Lorem ipsum"), new Soldier("hemsworth", "Chris Hemsworth",
								"Lorem ipsum"), new Soldier("van-damme", "Jean-Claude Van Damme", "Lorem ipsum"))),
						new ThreeSoldiers(newArrayList(new Soldier("willis", "Bruce Willis", "Lorem ipsum"), new Soldier("schwarzenegger",
								"Arnold Schwarzenegger", "Lorem ipsum"), new Soldier("adkins", "Scott Adkins", "Lorem ipsum")))));
	}
}
