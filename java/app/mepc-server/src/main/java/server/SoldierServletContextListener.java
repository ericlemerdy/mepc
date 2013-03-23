package server;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.jaxrs.JsonMappingExceptionMapper;
import org.codehaus.jackson.jaxrs.JsonParseExceptionMapper;

import resources.Soldier;
import resources.Soldiers;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class SoldierServletContextListener extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new JerseyServletModule() {

			@Override
			protected void configureServlets() {
				bind(Soldiers.class).asEagerSingleton();
				bind(Soldier.class);
				bind(JacksonJsonProvider.class).asEagerSingleton();
				bind(JacksonJaxbJsonProvider.class).asEagerSingleton();
				bind(JsonParseExceptionMapper.class).asEagerSingleton();
				bind(JsonMappingExceptionMapper.class).asEagerSingleton();

				serve("/*").with(GuiceContainer.class, ImmutableMap.of(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE.toString()));
			}
		});
	}
}
