import static com.google.common.collect.ImmutableMap.of;
import static java.lang.String.format;
import static org.openqa.selenium.phantomjs.PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY;

import java.io.File;

import org.fluentlenium.core.FluentAdapter;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.service.DriverService;

public abstract class PhantomJsTest extends FluentAdapter {
	private static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(1024, 768);
	private static WebDriver driver;

	@Rule
	public LifeCycle lifecycle = new LifeCycle();

	class LifeCycle extends TestWatcher {
		@Override
		protected void starting(Description description) {
			if (null == driver) {
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						if (driver != null) {
							driver.quit();
						}
					}
				});
				driver = createDriver();
			}
			driver.manage().deleteAllCookies();
			driver.manage().window().setSize(DEFAULT_WINDOW_SIZE);
			initFluent(driver);
		}

		@Override
		protected void succeeded(Description description) {
			snapshotFile(description).delete();
		}

		@Override
		protected void failed(Throwable e, Description description) {
			takeScreenShot(snapshotFile(description).getAbsolutePath());
		}

		private File snapshotFile(Description description) {
			return new File("target", format("snapshots/%s.png", description.getMethodName()));
		}

		private WebDriver createDriver() {
			File phantomJsExe = new PhantomJsDownloader().downloadAndExtract();
			DesiredCapabilities capabilities = new DesiredCapabilities(of(PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomJsExe.getAbsolutePath()));
			DriverService service = PhantomJSDriverService.createDefaultService(capabilities);
			return new PhantomJSDriver(service, capabilities);
		}
	}
}
