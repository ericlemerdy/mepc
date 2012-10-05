import net.sourceforge.jwebunit.junit.WebTester;

import org.junit.Before;
import org.junit.Test;

public class HomePageTest {

    private WebTester webTester;

    @Before
    public void createWebTester() {
	webTester = new WebTester();
	webTester.setBaseUrl("http://localhost:8080/");
    }

    @Test
    public void should_display_hello_world_on_home_page() {
	webTester.beginAt("/");
	webTester.assertTextPresent("Hollo, world !");
    }

}
