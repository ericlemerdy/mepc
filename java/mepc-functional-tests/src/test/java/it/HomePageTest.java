package it;

import static com.google.common.base.Splitter.on;
import static com.google.inject.name.Names.named;
import static java.lang.String.format;
import static org.fest.assertions.Assertions.assertThat;
import net.gageot.test.rules.ServiceRule;
import net.sourceforge.jwebunit.htmlunit.HtmlUnitTestingEngineImpl;
import net.sourceforge.jwebunit.junit.WebTester;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import server.FrontServer;
import server.SoldierServer;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class HomePageTest {

	public ServiceRule<SoldierServer> soldierRule = ServiceRule.startWithRandomPort(SoldierServer.class, (Module) null);
	public ServiceRule<FrontServer> frontRule = ServiceRule.startWithRandomPort(FrontServer.class, new AbstractModule() {
		@Override
		protected void configure() {
			bind(String.class).annotatedWith(named("dataHost")).toInstance(format("localhost:%d", soldierRule.getPort()));
		}
	});
	@Rule
	public TestRule bothServers = RuleChain.outerRule(soldierRule).around(frontRule);

	private WebTester webTester;

	@Before
	public void createWebTester() {
		final String frontHost = System.getProperty("fr.valtech.frontHost", format("localhost:%d", frontRule.getPort()));
		webTester = new WebTester();
		webTester.setBaseUrl(format("http://%s/", frontHost));
		webTester.beginAt("/");
		getWebClient().setAjaxController(new NicelyResynchronizingAjaxController());
	}

	private WebClient getWebClient() {
		if (webTester.getTestingEngine() instanceof HtmlUnitTestingEngineImpl) {
			final HtmlUnitTestingEngineImpl testingEngine = (HtmlUnitTestingEngineImpl) webTester.getTestingEngine();
			return testingEngine.getWebClient();
		}
		return null;
	}

	@Test
	public void should_display_the_app() {
		webTester.gotoPage("/");
		webTester.assertTextInElement("brand", "Soldier Store");
	}

	@Test
	public void should_display_stallone() {
		webTester.gotoPage("/");
		getWebClient().waitForBackgroundJavaScript(2000);
		webTester.assertTextPresent("Sylvester Stallone");
	}

	@Test
	public void when_hire_stallone_should_not_hire_him_again() {
		webTester.gotoPage("/");
		getWebClient().waitForBackgroundJavaScript(2000);
		webTester.assertLinkPresent("hire-stallone");
		webTester.clickLink("hire-stallone");
		webTester.clickButton("btn-dialog-hire-soldier");
		assertThat(on(' ').split(webTester.getElementById("hire-stallone").getAttribute("class"))).contains("disabled");
	}

	@After
	public void close() {
		webTester.closeBrowser();
	}

}
