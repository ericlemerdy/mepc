import static java.lang.String.format;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import net.gageot.test.rules.ServiceRule;
import net.sourceforge.jwebunit.junit.WebTester;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.inject.Module;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.net.httpserver.HttpServer;

@Path("/")
@SuppressWarnings("restriction")
public class HomePageTest {

    @GET
    public File index() {
	return new File("../static/index.html");
    }

    @GET
    @Path("/{subfolder}")
    public File index(@PathParam("subfolder") String subFolder) {
	return new File(format("../static/%s",
		subFolder));
    }

    @Rule
    public ServiceRule<HomePageTest> serviceRule = ServiceRule
	    .startWithRandomPort(HomePageTest.class, (Module) null);

    private HttpServer httpServer;

    private WebTester webTester;

    @Before
    public void createWebTester() {
	webTester = new WebTester();
	webTester.setBaseUrl(format("http://localhost:%d/",
		serviceRule.getPort()));
    }

    @Test
    public void should_display_hello_world_on_home_page() {
	webTester.beginAt("/");
	webTester.assertTextPresent("Hello, world!");
    }

    public void start(int port) throws IllegalArgumentException,
	    IOException {
	httpServer = HttpServerFactory.create(format("http://localhost:%d/",
 port),
		new DefaultResourceConfig(HomePageTest.class));
	httpServer.start();
    }

    public void stop() {
	httpServer.stop(0);
    }

}
