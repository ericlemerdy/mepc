package it;

import static com.jayway.restassured.RestAssured.expect;
import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.net.URL;
import java.util.List;

import net.gageot.test.rules.ServiceRule;

import org.junit.Rule;
import org.junit.Test;

import server.SoldierServer;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.Module;

public class DataTest {

	@Rule
	public ServiceRule<SoldierServer> serviceRule = ServiceRule.startWithRandomPort(SoldierServer.class, (Module) null);

	@Test
	public void should_get_soldiers() throws Exception {
		expect().body("soldiers.soldiers.id", contains( //
				contains((Object) "stallone", "statham", "li"), //
				contains((Object) "lundgren", "norris", "van-damme"), //
				contains((Object) "willis", "schwarzenegger") //
				)).when().get(format("http://localhost:%d/data/soldiers.json", serviceRule.getPort()));
	}

	@Test
	public void should_get_soldiers_as_jsonp() throws Exception {
		final List<String> lines = Resources.readLines(
				new URL(format("http://localhost:%d/data/soldiers.jsonp?callback=foo", serviceRule.getPort())), Charsets.UTF_8);
		assertThat(lines).hasSize(1);
		assertThat(lines.get(0)).startsWith("foo({").contains("stallone").endsWith("})");
	}

	@Test
	public void should_not_hire_stalonne_twice() throws Exception {
		final String hireURL = format("http://localhost:%d/data/hire/stalonne", serviceRule.getPort());
		expect().statusCode(SC_NO_CONTENT).when().put(hireURL);
		expect().statusCode(SC_FORBIDDEN).body(is("Sorry, stalonne is already hired...")).when().put(hireURL);
	}
}
