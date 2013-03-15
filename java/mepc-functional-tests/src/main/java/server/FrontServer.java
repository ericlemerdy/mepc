package server;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.inject.name.Names.named;
import static com.google.inject.util.Modules.override;
import static com.sun.jersey.api.container.httpserver.HttpServerFactory.create;
import static java.lang.String.format;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import resources.FrontResources;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.net.httpserver.HttpServer;

public class FrontServer {

	private HttpServer httpServer;
	private final Injector injector;

	public FrontServer(final Module... modules) {
		injector = Guice.createInjector(override(new AbstractModule() {
			@Override
			protected void configure() {
				bind(String.class).annotatedWith(named("dataHost")).toInstance("localhost:8081");
				bind(String.class).annotatedWith(named("frontDir")).toInstance(
						firstNonNull(System.getProperty("fr.valtech.frontdir"), "../../front/"));
			}
		}).with(modules));
	}

	public void start(final int port) throws IllegalArgumentException, IOException {
		final ResourceConfig config = new DefaultResourceConfig(FrontResources.class);
		final ContainerResponseFilter containerResponseFilter = new ContainerResponseFilter() {
			@Override
			public ContainerResponse filter(final ContainerRequest request, final ContainerResponse response) {
				final Map<String, List<Object>> headers = ImmutableMap.<String, List<Object>> of("Access-Control-Allow-Origin",
						Lists.<Object> newArrayList("*"), "Access-Control-Allow-Methods", Lists.<Object> newArrayList("POST"));
				response.getHttpHeaders().putAll(headers);
				return response;
			}
		};
		config.getContainerResponseFilters().add(containerResponseFilter);
		final GuiceComponentProviderFactory ioc = new GuiceComponentProviderFactory(config, injector);
		httpServer = create(format("http://localhost:%d/", port), config, ioc);
		httpServer.start();
	}

	public void stop() {
		httpServer.stop(0);
	}

	public static void main(final String[] args) throws IllegalArgumentException, IOException {
		final FrontServer server = new FrontServer();
		server.start(8080);
	}
}
