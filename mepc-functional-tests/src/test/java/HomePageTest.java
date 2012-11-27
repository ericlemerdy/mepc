import static java.lang.String.format;
import lightweightTestServer.MepcStaticResourcesServer;
import net.gageot.test.rules.ServiceRule;
import net.sourceforge.jwebunit.htmlunit.HtmlUnitTestingEngineImpl;
import net.sourceforge.jwebunit.junit.WebTester;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.inject.Module;

public class HomePageTest {

	@Rule
	public ServiceRule<MepcStaticResourcesServer> serviceRule = ServiceRule
			.startWithRandomPort(MepcStaticResourcesServer.class, (Module) null);

	private WebTester webTester;

	@Before
	public void createWebTester() {
		webTester = new WebTester();
		webTester.setBaseUrl(format("http://localhost:%d/",
				serviceRule.getPort()));
		webTester.beginAt("/");
		configureSynchronousAjax();
	}

	private void configureSynchronousAjax() {
		if (webTester.getTestingEngine() instanceof HtmlUnitTestingEngineImpl) {
			HtmlUnitTestingEngineImpl testingEngine = (HtmlUnitTestingEngineImpl) webTester
					.getTestingEngine();
			WebClient webClient = testingEngine.getWebClient();
			AjaxController ajaxController = new NicelyResynchronizingAjaxController();
			webClient.setAjaxController(ajaxController);
		}
	}

	@Test
	public void should_display_the_list_of_soldiers() {
		webTester.gotoPage("/");
		webTester.assertTextInElement("brand", "Soldier Store");
	}

	@Test
	public void should_display_rambo() {
		webTester.gotoPage("/");
		webTester.assertTextPresent("Sylvester Stallone");
	}

}
