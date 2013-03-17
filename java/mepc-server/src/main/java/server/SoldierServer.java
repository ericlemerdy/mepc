package server;

import java.io.IOException;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.jaxrs.JsonMappingExceptionMapper;
import org.codehaus.jackson.jaxrs.JsonParseExceptionMapper;

import resources.Soldier;
import resources.Soldiers;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory;
import com.sun.net.httpserver.HttpServer;

public class SoldierServer {

	private HttpServer httpServer;

	public void start(final int port) throws IllegalArgumentException, IOException {
		final ResourceConfig resourceConfig = new DefaultResourceConfig( //
				Soldiers.class, //
				Soldier.class, //
				JacksonJsonProvider.class, //
				JsonParseExceptionMapper.class, //
				JacksonJaxbJsonProvider.class, //
				JsonMappingExceptionMapper.class);
		resourceConfig.setPropertiesAndFeatures(ImmutableMap.<String, Object> of("com.sun.jersey.api.json.POJOMappingFeature", "true"));
		final GuiceComponentProviderFactory ioc = new GuiceComponentProviderFactory(resourceConfig, new SoldierServletContextListener().getInjector());
		httpServer = HttpServerFactory.create(String.format("http://localhost:%d/", port), resourceConfig, ioc);
		httpServer.start();
	}

	public void stop() {
		httpServer.stop(0);
	}

	public static void main(final String[] args) throws IllegalArgumentException, IOException {
		final SoldierServer server = new SoldierServer();
		server.start(8081);
	}
}
