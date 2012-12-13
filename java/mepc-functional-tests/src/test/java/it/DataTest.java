package it;

import static com.jayway.restassured.RestAssured.expect;
import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_NOT_ACCEPTABLE;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import net.gageot.test.rules.ServiceRule;

import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
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
	public void should_not_hire_stalonne_twice() throws Exception {
		String hireURL = format("http://localhost:%d/data/hire/stalonne", serviceRule.getPort());
		expect().statusCode(SC_NO_CONTENT).when().put(hireURL);
		expect().statusCode(SC_FORBIDDEN).body(is("Sorry, stalonne is already hired...")).when().put(hireURL);
	}
}
