package it;

import static com.google.common.base.Splitter.on;
import static java.lang.String.format;
import static org.fest.assertions.Assertions.assertThat;
import net.sourceforge.jwebunit.htmlunit.HtmlUnitTestingEngineImpl;
import net.sourceforge.jwebunit.junit.WebTester;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;

public abstract class HomePageTest {

	private WebTester webTester;

	protected abstract String getFrontHost();

	@Before
	public void createWebTester() {
		webTester = new WebTester();
		webTester.setBaseUrl(format("http://%s/", getFrontHost()));
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
