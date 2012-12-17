package server;

import static com.google.inject.name.Names.named;
import static com.google.inject.util.Modules.override;
import static com.sun.jersey.api.container.httpserver.HttpServerFactory.create;
import static java.lang.String.format;

import java.io.IOException;

import resources.FrontResources;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory;
import com.sun.net.httpserver.HttpServer;

public class FrontServer {

	private HttpServer httpServer;
	private final Injector injector;

	public FrontServer(final Module... modules) {
		injector = Guice.createInjector(override(new AbstractModule() {
			@Override
			protected void configure() {
				bind(String.class).annotatedWith(named("dataHost")).toInstance("localhost:8081");
			}
		}).with(modules));
	}

	public void start(final int port) throws IllegalArgumentException, IOException {
		final DefaultResourceConfig config = new DefaultResourceConfig(FrontResources.class);
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
