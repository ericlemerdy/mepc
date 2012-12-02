package it;

import static java.lang.String.format;
import net.gageot.test.rules.ServiceRule;
import net.sourceforge.jwebunit.htmlunit.HtmlUnitTestingEngineImpl;
import net.sourceforge.jwebunit.junit.WebTester;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import server.SoldierServer;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
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
			final AjaxController ajaxController = new NicelyResynchronizingAjaxController();
			webClient.setAjaxController(ajaxController);
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
	public void should_hire_stallone() {
		webTester.gotoPage("/");
		webTester.assertLinkPresent("hire-stallone");
	}

}
