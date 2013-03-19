import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;

import org.fluentlenium.adapter.FluentTest;
import org.junit.Before;
import org.junit.Test;

public class ITHomePageTest extends FluentTest {

	@Before
	public void createWebTester() {
		goTo("http://localhost:8080/");
		await().until(".soldier-name").isPresent();
	}

	@Test
	public void should_display_the_app() {
		assertThat(findFirst(".brand")).hasText("SOLDIER STORE");
	}

	@Test
	public void should_display_stallone() {
		assertThat(find(".soldier-name")).hasText("Sylvester Stallone");
	}

}
