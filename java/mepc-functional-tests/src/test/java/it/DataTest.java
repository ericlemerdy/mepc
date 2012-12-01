package it;

import static com.jayway.restassured.RestAssured.expect;
import static java.lang.String.format;
import static org.hamcrest.Matchers.contains;
import net.gageot.test.rules.ServiceRule;

import org.junit.Rule;
import org.junit.Test;

import server.SoldierServer;

import com.google.inject.Module;

public class DataTest {

	@Rule
	public ServiceRule<SoldierServer> serviceRule = ServiceRule.startWithRandomPort(SoldierServer.class, (Module) null);

	@Test
	public void should_get_soldiers() throws Exception {
		expect().body(
				"soldiers.soldiers.id",
				contains(contains((Object) "stallone", "statham", "li"), contains((Object) "lundgren", "norris", "crews"),
						contains((Object) "couture", "hemsworth", "van-damme"), contains((Object) "willis", "schwarzenegger", "adkins"))).when()
				.get(format("http://localhost:%d/data/soldiers.json", serviceRule.getPort()));
	}

}
