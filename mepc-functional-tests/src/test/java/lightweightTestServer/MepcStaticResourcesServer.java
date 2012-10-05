package lightweightTestServer;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.net.httpserver.HttpServer;

@Path("/")
@SuppressWarnings("restriction")
public class MepcStaticResourcesServer {

    private HttpServer httpServer;

    @GET
    public File index() {
	return new File("../static/index.html");
    }

    @GET
    @Path("/{subfolder}")
    public File index(@PathParam("subfolder") String subFolder) {
	return new File(format("../static/%s", subFolder));
    }

    public void start(int port) throws IllegalArgumentException, IOException {
	httpServer = HttpServerFactory.create(
		format("http://localhost:%d/", port),
		new DefaultResourceConfig(MepcStaticResourcesServer.class));
	httpServer.start();
    }

    public void stop() {
	httpServer.stop(0);
    }

}
