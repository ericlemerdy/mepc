package net.chaussalet;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.spi.container.servlet.ServletContainer;

@Path("/")
@Produces(TEXT_PLAIN)
public class VagrantResource {
	
	public static void main(String[] args) throws Exception {
		String port = args.length > 0 ? args[0] : "9999";
		ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
		servletHolder.setInitParameter("com.sun.jersey.config.property.packages", "net.chaussalet");

	    Server server = new Server(Integer.parseInt(port));
	    Context context = new Context(server, "/", Context.SESSIONS);
	    context.addServlet(servletHolder, "/");
	    server.start();
	}
	
	@GET
	public String getStatus() throws IOException {
		Process process = Runtime.getRuntime().exec("vagrant status");
		BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		int pos = 0;
		while ((line = stdoutReader.readLine()) != null) {
			System.out.println(line);
			pos++;
			if (pos == 3) {
				String[] elts = line.split(" ");
				return elts[elts.length-1];
			}
		}
		throw new NotFoundException();
	}
	
	@POST
	@Consumes(APPLICATION_FORM_URLENCODED)
	public Response changeStatus(final MultivaluedMap<String, String> formParameters) throws URISyntaxException, InterruptedException {
		if (!formParameters.containsKey("action")) {
			throw new WebApplicationException(400);
		}
		String action = formParameters.getFirst("action");
		if ("up".equals(action)) {
			if (!formParameters.containsKey("config")) {
				throw new WebApplicationException(400);
			}
			String config = formParameters.getFirst("config");
			PrintWriter configWriter;
			try {
				configWriter = new PrintWriter("Vagrantfile");
				configWriter.write(config);
				configWriter.close();
			} catch (FileNotFoundException e) {
				throw new WebApplicationException(Response.serverError().entity(e).build());
			}
		}
		Process process;
		try {
			process = Runtime.getRuntime().exec("vagrant "+action);
			BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = stdoutReader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			throw new WebApplicationException(Response.serverError().entity(e).build());
		}
		process.waitFor();
		if ("up".equals(action)) {
			if (process.exitValue() == 0) {
				return Response.created(new URI("/")).build();			
			} else {
				throw new WebApplicationException(Response.serverError().build());
			}
		}
		return Response.noContent().build();
	}
}
