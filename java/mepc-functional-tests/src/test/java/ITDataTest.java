import static com.google.common.io.Resources.readLines;
import static com.jayway.restassured.RestAssured.expect;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assume.assumeTrue;

import java.net.URL;
import java.util.List;

import org.hsqldb.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Charsets;

public class ITDataTest {

	private static Server server;

	private String getAppHost() {
		return getProperty("fr.valtech.appHost");
	}

	@BeforeClass
	public static void startHsqlDB() {
		server = new Server();
		server.setAddress("localhost");
		server.setDatabaseName(0, "");
		server.setDatabasePath(0, "file:target/test");
		server.start();
	}

	@AfterClass
	public static void stopHsqlDB() {
		server.stop();
	}

	@BeforeClass
	public static void conf() {
		System.out.println(format("Data test configuration { apphost: '%s', dev: '%s' }", //
				getProperty("fr.valtech.appHost"), //
				getProperty("fr.valtech.dev")));
	}

	@Test
	public void should_get_soldiers() throws Exception {
		expect().content("soldiers.soldiers.id", contains( //
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
	public void soldier_should_have_some_attributes() throws Exception {
		expect().root("soldiers.soldiers[0][0]")//
				.content("id", equalTo("stallone")).and() //
				.content("name", equalTo("Sylvester Stallone")) //
				.content("description", equalTo("This ex-boxer is a vietnâm veteran that really had a rough.")).and() //
				.content("hired", notNullValue()).and() //
				.content("codeName", nullValue()).and() //
				.when().get(format("http://%s/data/soldiers.json", getAppHost()));
	}

	@Test
	public void should_not_hire_stallone_twice() throws Exception {
		assumeTrue(parseBoolean(getProperty("fr.valtech.dev", "false")));

		final String hireURL = format("http://%s/data/hire/stallone", getAppHost());
		expect().statusCode(SC_OK).when().post(hireURL);
		expect().statusCode(SC_FORBIDDEN).body(is("Sorry, stallone is already hired...")).when().post(hireURL);
	}

	@Test
	public void should_hire_statham_as_polka() throws Exception {
		assumeTrue(parseBoolean(getProperty("fr.valtech.dev", "false")));

		final String hireURL = format("http://%s/data/hire/statham?codeName=polka", getAppHost());
		expect().statusCode(SC_OK).when().post(hireURL);
		expect().root("soldiers.soldiers[0][1]") //
				.body("hired", equalTo(true)) //
				.content("codeName", equalTo("polka")) //
				.when().get("http://{appHost}/data/soldiers.json", getAppHost());
	}

	@Test
	public void should_not_hire_norris() throws Exception {
		final String hireURL = format("http://%s/data/hire/norris", getAppHost());
		expect().statusCode(SC_FORBIDDEN).body(is("You can never hire chuck norris because chuck norris hired you...")).when().post(hireURL);
	}
}
