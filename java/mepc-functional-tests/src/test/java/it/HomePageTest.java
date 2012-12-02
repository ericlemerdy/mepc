package it;

import static com.google.common.base.Splitter.on;
import static java.lang.String.format;
import static org.fest.assertions.Assertions.assertThat;
import net.gageot.test.rules.ServiceRule;
import net.sourceforge.jwebunit.htmlunit.HtmlUnitTestingEngineImpl;
import net.sourceforge.jwebunit.junit.WebTester;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import server.SoldierServer;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.common.base.Splitter;
import com.google.inject.Module;

public class HomePageTest {

	@Rule
	public ServiceRule<SoldierServer> serviceRule = ServiceRule.startWithRandomPort(SoldierServer.class, (Module) null);

	private WebTester webTester;

	@Before
	public void createWebTester() {
		webTester = new WebTester();
		webTester.setBaseUrl(format("http://localhost:%d/", serviceRule.getPort()));
		webTester.beginAt("/");
		configureSynchronousAjax();
	}

	private void configureSynchronousAjax() {
		if (webTester.getTestingEngine() instanceof HtmlUnitTestingEngineImpl) {
			final HtmlUnitTestingEngineImpl testingEngine = (HtmlUnitTestingEngineImpl) webTester.getTestingEngine();
			final WebClient webClient = testingEngine.getWebClient();
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		}
	}

	@Test
	public void should_display_the_list_of_soldiers() {
		webTester.gotoPage("/");
		webTester.assertTextInElement("brand", "Soldier Store");
	}

	@Test
	public void should_display_stallone() {
		webTester.gotoPage("/");
		webTester.assertTextPresent("Sylvester Stallone");
	}

	@Test
	public void when_hire_stallone_should_not_hire_him_again() {
		webTester.gotoPage("/");
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
