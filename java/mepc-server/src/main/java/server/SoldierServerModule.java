package server;

import javax.inject.Singleton;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.jaxrs.JsonMappingExceptionMapper;
import org.codehaus.jackson.jaxrs.JsonParseExceptionMapper;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import resources.Soldier;
import resources.Soldiers;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class SoldierServerModule extends JerseyServletModule {
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

	@Provides
	@Singleton
	public SessionFactory createSessionFactory() {
		return new AnnotationConfiguration().configure().buildSessionFactory();
	}
}