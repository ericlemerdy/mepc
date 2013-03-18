
import static java.lang.String.format;
import net.sourceforge.jwebunit.htmlunit.HtmlUnitTestingEngineImpl;
import net.sourceforge.jwebunit.junit.WebTester;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;

public class ITHomePageTest {

	private WebTester webTester;

	@Before
	public void createWebTester() {
		final String frontHost = System.getProperty("fr.valtech.frontHost", "localhost:8080");
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

	@After
	public void close() {
		webTester.closeBrowser();
	}

}
