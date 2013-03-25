import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;
import static org.junit.Assume.assumeTrue;

import java.util.concurrent.TimeUnit;

import org.fest.assertions.Condition;
import org.fluentlenium.adapter.IsolatedTest;
import org.fluentlenium.core.Fluent;
import org.fluentlenium.core.domain.FluentWebElement;
import org.hsqldb.Server;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.google.common.base.Predicate;

public class ITHomePageTest extends PhantomJsTest {
	private static Server server;

	private static final Condition<FluentWebElement> HAS_CLASS(final String classname) {
		return new Condition<FluentWebElement>("has class " + classname) {
			@Override
			public boolean matches(FluentWebElement value) {
				return value.getAttribute("class").contains(classname);
			}
		};
	};

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

	private static final Condition<FluentWebElement> DISABLED = HAS_CLASS("disabled");
	private static final Condition<FluentWebElement> MUTED = HAS_CLASS("muted");

	private String getAppHost() {
		return getProperty("fr.valtech.appHost");
	}

	private Predicate<WebDriver> dialogHidden() {
		return new Predicate<WebDriver>() {
			@Override
			public boolean apply(WebDriver input) {
				return findFirst("#hire-soldier-dialog").getAttribute("class").contains("hide");
			}
		};
	};

	@BeforeClass
	public static void conf() {
		System.out.println(format("Data test configuration { apphost: '%s', dev: '%s' }", //
				getProperty("fr.valtech.appHost"), //
				getProperty("fr.valtech.dev")));
	}

	@Before
	public void createWebTester() {
		goTo(format("http://%s/", getAppHost()));
		await().atMost(2000, TimeUnit.SECONDS).until(".soldier-name").areDisplayed();
	}

	@Test
	public void should_display_the_app() {
		assertThat(findFirst(".brand")).hasText("SOLDIER STORE");
	}

	@Test
	public void should_display_stallone() {
		assertThat(find(".soldier-name")).hasText("Sylvester Stallone");
	}

	@Test
	public void should_hire_lundgren() {
		assumeTrue(parseBoolean(getProperty("fr.valtech.dev", "false")));

		click("#hire-lundgren");
		await().atMost(2000).until("#btn-dialog-hire-soldier").areDisplayed();
		fill("#hire-form-code-name").with("Dolphy");
		click("#btn-dialog-hire-soldier");
		await().atMost(2000).until(dialogHidden());
		assertLundgrenHired(this);

		IsolatedTest lundgrenIsHiredForOtherClients = new IsolatedTest(getDriver());
		Fluent otherClient = lundgrenIsHiredForOtherClients //
				.goTo(format("http://%s/", getAppHost())) //
				.await().atMost(2000, TimeUnit.SECONDS).until(".soldier-name").areDisplayed();
		assertLundgrenHired(otherClient);
		lundgrenIsHiredForOtherClients.quit();
	}

	private void assertLundgrenHired(Fluent page) {
		assertThat(page.findFirst("#hire-lundgren")).is(DISABLED);
		assertThat(page.findFirst("#lundgren")).is(MUTED);
		assertThat(page.find("#lundgren")).hasText("Dolphy");
	}
}
