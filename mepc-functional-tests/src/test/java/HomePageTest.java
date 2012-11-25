import static java.lang.String.format;
import lightweightTestServer.MepcStaticResourcesServer;
import net.gageot.test.rules.ServiceRule;
import net.sourceforge.jwebunit.junit.WebTester;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

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
	}

	@Test
	public void should_display_the_list_of_soldiers() {
		webTester.beginAt("/");
		webTester.assertTextInElement("brand", "Soldier Store");
	}

	@Test
	public void should_display_rambo() {
		webTester.beginAt("/");
		webTester.assertTextPresent("Sylvester Stallone");
	}

}
