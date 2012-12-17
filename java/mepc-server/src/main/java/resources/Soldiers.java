package resources;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.Response.status;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.json.JSONWithPadding;

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

	private static boolean stalonneHired = false;

	@GET
	@Path("soldiers.json")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, List<ThreeSoldiers>> allSoldiers() {
		return ImmutableMap
				.<String, List<ThreeSoldiers>> of(
						"soldiers",
						newArrayList(
								new ThreeSoldiers(newArrayList(new Soldier("stallone", "Sylvester Stallone",
										"This ex-boxer is a vietnâm veteran that really had a rough."), new Soldier("statham", "Jason Statham",
										"Kickboxing expert, body-to-body, it leaves no chance to your enemies."), new Soldier("li", "Jet Li",
										"Do not be fooled by its size, this man can send you to the mat quickly thanks to its speed."))),
								new ThreeSoldiers(newArrayList(new Soldier("lundgren", "Dolph Lundgren",
										"The Russian soldier has hardened his body to the point it looks like a robot."), new Soldier("norris",
										"Chuck Norris", "You are not hiring Chuck Norris, this is Chuck Norris that hires you."), new Soldier(
										"van-damme", "Jean-Claude Van Damme", "This warrior philosopher is aware."))),
								new ThreeSoldiers(
										newArrayList(
												new Soldier("willis", "Bruce Willis",
														"This formidable killer is unstoppable, he learned that the police in New York."),
												new Soldier(
														"schwarzenegger",
														"Arnold Schwarzenegger",
														"When you're born in the Austrian mountains and you carries the milk down to the valley, you are getting stronger... If you hire him, he will be back.")))));
	}

	@GET
	@Path("soldiers.jsonp")
	@Produces("application/ecmascript")
	public JSONWithPadding allSoldiers(@QueryParam("callback") final String callbackName) {
		final Map<String, List<ThreeSoldiers>> allSoldiers = allSoldiers();
		return new JSONWithPadding(allSoldiers, callbackName);
	}

	@PUT
	@Path("hire/stalonne")
	public void hireSoldier() {
		if (stalonneHired) {
			final Response error = status(Status.FORBIDDEN).entity("Sorry, stalonne is already hired...").build();
			throw new WebApplicationException(error);
		}
		stalonneHired = true;
	}
}
