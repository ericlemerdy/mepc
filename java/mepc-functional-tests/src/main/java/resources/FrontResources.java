package resources;

import static com.google.common.base.Objects.firstNonNull;
import static java.lang.String.format;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@Path("/")
public class FrontResources {

	private final String frontDirectory;
	private final String dataHost;

	@Inject
	public FrontResources(@Named("dataHost") final String dataHost) {
		this.dataHost = dataHost;
		this.frontDirectory = firstNonNull(System.getProperty("fr.valtech.frontdir"), "../../front/");
	}

	@GET
	public Response index() {
		return Response.ok(serveFrontFile("index.html")).type(MediaType.TEXT_HTML).build();
	}

	@GET
	@Path("/{subfolder: .*}")
	public File serveFrontFile(@PathParam("subfolder") final String subFolder) {
		final String pathname = format("%s/%s", frontDirectory, subFolder);
		return new File(pathname);
	}

	@GET
	@Path("/{subfolder: .*}.json")
	@Produces(MediaType.APPLICATION_JSON)
	public File serveFrontJson(@PathParam("subfolder") final String subFolder) {
		final String pathname = format("%s/%s.json", frontDirectory, subFolder);
		return new File(pathname);
	}

	@GET
	@Path("/conf.js")
	@Produces(MediaType.APPLICATION_JSON)
	public String serveConfiguration(@PathParam("subfolder") final String subFolder) {
		return format("{\"dataHost\":\"%s\"}", dataHost);
	}
}
