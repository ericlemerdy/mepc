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

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class SoldierServerHSQLDBModule extends AbstractModule {

	@Provides
	@Singleton
	public SessionFactory createSessionFactory() {
		return new AnnotationConfiguration() //
				.setProperty(DRIVER, "org.hsqldb.jdbcDriver") //
				.setProperty(URL, "jdbc:hsqldb:file:target/test") //
				.setProperty(USER, "sa") //
				.setProperty(PASS, "") //
				.setProperty(POOL_SIZE, "1") //
				.setProperty(DIALECT, "org.hibernate.dialect.HSQLDialect") //
				.setProperty(SHOW_SQL, "true") //
				.setProperty(HBM2DDL_AUTO, "create") //
				.addAnnotatedClass(resources.Soldier.class) //
				.buildSessionFactory();
	}

	@Override
	protected void configure() {
		// nothing to configure.
	}
}