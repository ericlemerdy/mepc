package lightweightTestServer;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.annotations.VisibleForTesting;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.net.httpserver.HttpServer;

@Path("/")
public class MepcStaticResourcesServer {

	private HttpServer httpServer;

	@GET
	public Response index() {
		return Response.ok(serveStaticFile("index.html"))
				.type(MediaType.TEXT_HTML).build();
	}

	@GET
	@Path("/{subfolder: .*}")
	public File serveStaticFile(@PathParam("subfolder") String subFolder) {
		String pathname = format("../static/%s", subFolder);
		return new File(pathname);
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

	@VisibleForTesting
	public static void main(String[] args) throws IllegalArgumentException,
			IOException {
		MepcStaticResourcesServer server = new MepcStaticResourcesServer();
		server.start(8080);
	}

}
