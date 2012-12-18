package it;

import static com.google.common.io.Resources.readLines;
import static com.jayway.restassured.RestAssured.expect;
import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
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
import com.google.inject.Module;

public class DataTest {

	@Rule
	public ServiceRule<SoldierServer> appRule = ServiceRule.startWithRandomPort(SoldierServer.class, (Module) null);

	private String getAppHost() {
		return System.getProperty("fr.valtech.appHost", format("localhost:%d", appRule.getPort()));
	}

	@Test
	public void should_get_soldiers() throws Exception {
		expect().body("soldiers.soldiers.id", contains( //
				contains((Object) "stallone", "statham", "li"), //
				contains((Object) "lundgren", "norris", "van-damme"), //
				contains((Object) "willis", "schwarzenegger") //
				)).when().get(format("http://%s/data/soldiers.json", getAppHost()));
	}

	@Test
	public void should_get_soldiers_as_jsonp() throws Exception {
		final List<String> lines = readLines(new URL(format("http://%s/data/soldiers.jsonp?callback=foo", getAppHost())), Charsets.UTF_8);
		assertThat(lines).hasSize(1);
		assertThat(lines.get(0)).startsWith("foo({").contains("stallone").endsWith("})");
	}

	@Test
	public void should_not_hire_stallone_twice() throws Exception {
		final String hireURL = format("http://%s/data/hire/stallone", getAppHost());
		expect().statusCode(SC_OK).when().post(hireURL);
		expect().statusCode(SC_FORBIDDEN).body(is("Sorry, stallone is already hired...")).when().post(hireURL);
	}

	@Test
	public void should_not_hire_norris() throws Exception {
		final String hireURL = format("http://%s/data/hire/norris", getAppHost());
		expect().statusCode(SC_FORBIDDEN).body(is("You can never hire chuck norris because chuck norris hired you...")).when().post(hireURL);
	}
}
