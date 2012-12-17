package it;

import static com.google.inject.name.Names.named;
import static java.lang.String.format;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assume.assumeThat;
import net.gageot.test.rules.ServiceRule;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import server.FrontServer;
import server.SoldierServer;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class HomePageWithServerRulesTest extends HomePageTest {

	public ServiceRule<SoldierServer> soldierRule = ServiceRule.startWithRandomPort(SoldierServer.class, (Module) null);
	public ServiceRule<FrontServer> frontRule = ServiceRule.startWithRandomPort(FrontServer.class, new AbstractModule() {
		@Override
		protected void configure() {
			bind(String.class).annotatedWith(named("dataHost")).toInstance(format("localhost:%d", soldierRule.getPort()));
		}
	});
	@Rule
	public TestRule bothServers = RuleChain.outerRule(soldierRule).around(frontRule);

	@BeforeClass
	public static void ignoreIfUrlNotDefinedAsSystemProperty() {
		assumeThat(System.getProperty("fr.valtech.frontHost"), nullValue());
	}

	@Override
	protected String getFrontHost() {
		return format("localhost:%d", frontRule.getPort());
	}

}
