import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.fest.assertions.Condition;
import org.fluentlenium.adapter.FluentTest;
import org.fluentlenium.core.domain.FluentWebElement;
import org.junit.Before;
import org.junit.Test;

public class ITHomePageTest extends FluentTest {

	@Before
	public void createWebTester() {
		goTo("http://localhost:8080/");
		await().atMost(2000, TimeUnit.SECONDS).until(".soldier-name").areDisplayed();
	}

	@Test
	public void should_display_the_app() {
		assertThat(findFirst(".brand")).hasText("SOLDIER STORE");
	}

	@Test
	public void should_display_stallone() {
		assertThat(find(".soldier-name")).hasText("Sylvester Stallone");
	}

	@Test
	public void should_hire_lundgren() {
		click("#hire-lundgren");
		await().atMost(2000).until("#btn-dialog-hire-soldier").areDisplayed();
		click("#btn-dialog-hire-soldier");
		await().until("#soldier-dialog").isNotPresent();
		assertThat(findFirst("#hire-lundgren")).satisfies(new Condition<FluentWebElement>("has class disabled") {
			@Override
			public boolean matches(FluentWebElement value) {
				return value.getAttribute("class").contains("disabled");
			}
		});
	}
}
