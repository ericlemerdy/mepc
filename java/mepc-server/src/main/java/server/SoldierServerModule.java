package server;

import static com.google.common.io.Files.newReader;
import static org.hibernate.cfg.Environment.DIALECT;
import static org.hibernate.cfg.Environment.DRIVER;
import static org.hibernate.cfg.Environment.HBM2DDL_AUTO;
import static org.hibernate.cfg.Environment.PASS;
import static org.hibernate.cfg.Environment.POOL_SIZE;
import static org.hibernate.cfg.Environment.SHOW_SQL;
import static org.hibernate.cfg.Environment.URL;
import static org.hibernate.cfg.Environment.USER;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.inject.Singleton;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.jaxrs.JsonMappingExceptionMapper;
import org.codehaus.jackson.jaxrs.JsonParseExceptionMapper;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import resources.Soldier;
import resources.Soldiers;

import com.google.common.base.Charsets;
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
	public SessionFactory createSessionFactory() throws FileNotFoundException, IOException {
		Properties properties;

		Properties defaultProperties = new Properties();
		defaultProperties.put(DRIVER, "com.mysql.jdbc.Driver");
		defaultProperties.put(URL, "jdbc:mysql://localhost/mepcdata");
		defaultProperties.put(USER, "root");
		defaultProperties.put(PASS, "root");
		defaultProperties.put(POOL_SIZE, "1");
		defaultProperties.put(DIALECT, "org.hibernate.dialect.MySQL5Dialect");
		defaultProperties.put(SHOW_SQL, "true");
		defaultProperties.put(HBM2DDL_AUTO, "update");

		try {
			Properties propertiesFromFile = new Properties();
			propertiesFromFile.load(newReader(new File("/etc/mepc/", "mepc-server.properties"), Charsets.UTF_8));
			properties = propertiesFromFile;
		} catch (IOException e) {
			System.out.println("Default file not loaded. Using hard-coded defaults");
			properties = defaultProperties;
		}

		return new AnnotationConfiguration() //
				.addProperties(properties) //
				.addAnnotatedClass(resources.Soldier.class) //
				.buildSessionFactory();
	}
}