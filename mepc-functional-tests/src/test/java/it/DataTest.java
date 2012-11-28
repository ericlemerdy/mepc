package it;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.restassured.RestAssured.expect;
import static java.lang.String.format;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import lightweightTestServer.MepcStaticResourcesServer;
import net.gageot.test.rules.ServiceRule;

import org.fest.assertions.Assertions;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class DataTest {

	@Rule
	public ServiceRule<MepcStaticResourcesServer> serviceRule = ServiceRule
			.startWithRandomPort(MepcStaticResourcesServer.class, (Module) null);

	@Test
	public void should_get_soldiers() throws Exception {
//		assertThat(
//				newArrayList(newArrayList((Object) "stallone", "statham", "li")),
//				contains(contains((Object) "stallone", "statham", "li")));

		expect().body(
				"soldiers.soldiers.id",
				contains(contains((Object) "stallone", "statham", "li"),
						contains((Object) "lundgren", "norris", "crews"),
						contains((Object) "couture", "hemsworth", "van-damme"),
						contains((Object) "willis", "schwarzenegger", "adkins")))
				.when()
				.get(format("http://localhost:%d/data/soldiers.json",
						serviceRule.getPort()));
	}

}
