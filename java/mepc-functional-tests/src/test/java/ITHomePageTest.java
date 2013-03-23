import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.fest.assertions.Condition;
import org.fluentlenium.adapter.FluentTest;
import org.fluentlenium.adapter.IsolatedTest;
import org.fluentlenium.core.Fluent;
import org.fluentlenium.core.domain.FluentWebElement;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.google.common.base.Predicate;

public class ITHomePageTest extends FluentTest {
	private static final Condition<FluentWebElement> HAS_CLASS(final String classname) {
		return new Condition<FluentWebElement>("has class " + classname) {
			@Override
			public boolean matches(FluentWebElement value) {
				return value.getAttribute("class").contains(classname);
			}
		};
	};

	private static final Condition<FluentWebElement> DISABLED = HAS_CLASS("disabled");
	private static final Condition<FluentWebElement> MUTED = HAS_CLASS("muted");

	private Predicate<WebDriver> dialogHidden() {
		return new Predicate<WebDriver>() {
			@Override
			public boolean apply(WebDriver input) {
				return findFirst("#hire-soldier-dialog").getAttribute("class").contains("hide");
			}
		};
	};

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
		fill("#hire-form-code-name").with("Dolphy");
		click("#btn-dialog-hire-soldier");
		await().atMost(2000).until(dialogHidden());
		assertLundgrenHired(this);

		IsolatedTest lundgrenIsHiredForOtherClients = new IsolatedTest();
		Fluent otherClient = lundgrenIsHiredForOtherClients //
				.goTo("http://localhost:8080/") //
				.await().atMost(2000, TimeUnit.SECONDS).until(".soldier-name").areDisplayed();
		assertLundgrenHired(otherClient);
		lundgrenIsHiredForOtherClients.quit();
	}

	private void assertLundgrenHired(Fluent page) {
		assertThat(page.findFirst("#hire-lundgren")).is(DISABLED);
		assertThat(page.findFirst("#lundgren")).is(MUTED);
		assertThat(page.find("#lundgren")).hasText("Dolphy");
	}
}
