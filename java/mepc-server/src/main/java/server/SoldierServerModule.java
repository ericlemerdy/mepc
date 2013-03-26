package server;

import static org.hibernate.cfg.Environment.DIALECT;
import static org.hibernate.cfg.Environment.DRIVER;
import static org.hibernate.cfg.Environment.HBM2DDL_AUTO;
import static org.hibernate.cfg.Environment.PASS;
import static org.hibernate.cfg.Environment.POOL_SIZE;
import static org.hibernate.cfg.Environment.SHOW_SQL;
import static org.hibernate.cfg.Environment.URL;
import static org.hibernate.cfg.Environment.USER;

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
		return new AnnotationConfiguration() //
				.setProperty(DRIVER, "org.hsqldb.jdbcDriver") //
				.setProperty(URL, "jdbc:hsqldb:hsql://localhost") //
				.setProperty(USER, "sa") //
				.setProperty(PASS, "") //
				.setProperty(POOL_SIZE, "1") //
				.setProperty(DIALECT, "org.hibernate.dialect.HSQLDialect") //
				.setProperty(SHOW_SQL, "true") //
				.setProperty(HBM2DDL_AUTO, "create") //
				.addAnnotatedClass(resources.Soldier.class) //
				.buildSessionFactory();
	}
}