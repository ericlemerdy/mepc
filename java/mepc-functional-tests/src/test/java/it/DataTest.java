package it;

import static com.jayway.restassured.RestAssured.expect;
import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.hamcrest.Matchers.contains;
import net.gageot.test.rules.ServiceRule;

import org.apache.http.HttpStatus;
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
				contains(contains((Object) "stallone", "statham", "li"), contains((Object) "lundgren", "norris", "van-damme"),
						contains((Object) "willis", "schwarzenegger"))).when().get(format("http://localhost:%d/data/soldiers.json", serviceRule.getPort()));
	}

	@Test
	public void should_hire_soldier() throws Exception {
		expect().statusCode(SC_NO_CONTENT).when().put(format("http://localhost:%d/data/hire/stalonne", serviceRule.getPort()));
	}
}
