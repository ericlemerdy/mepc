package it;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assume.assumeThat;

import org.junit.BeforeClass;

public class HomePageWithSystemPropertiesTest extends HomePageTest {

	@BeforeClass
	public static void ignoreIfUrlDefinedAsSystemProperty() {
		assumeThat(System.getProperty("fr.valtech.frontHost"), notNullValue());
	}

	@Override
	protected String getFrontHost() {
		return System.getProperty("fr.valtech.frontHost");
	}
}
