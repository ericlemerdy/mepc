package resource;

import static com.google.common.collect.Iterables.find;
import static java.lang.Boolean.FALSE;
import static org.fest.assertions.Assertions.assertThat;
import static resources.Soldier.withId;

import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import resources.Soldier;
import resources.Soldiers;
import resources.ThreeSoldiers;
import server.SoldierServerHSQLDBModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class SoldiersTest {

	private Injector injector = Guice.createInjector(Modules.override(new SoldierServerHSQLDBModule()).with(new SoldierServerHSQLDBModule()));
	private Soldiers soldierResources = null;

	@Before
	public void createTestSoldier() {
		soldierResources = injector.getInstance(Soldiers.class);
		Session session = injector.getInstance(SessionFactory.class).openSession();
		try {
			Transaction transaction = session.beginTransaction();
			transaction.begin();
			session.save(new Soldier("stallone", "Sylvester Stallone", "This ex-boxer is a vietnâm veteran that really had a rough."));
			session.save(new Soldier("statham", "Jason Statham", "Kickboxing expert, body-to-body, it leaves no chance to your enemies."));
			session.save(new Soldier("li", "Jet Li", "Do not be fooled by its size, this man can send you to the mat quickly thanks to its speed."));
			transaction.commit();
		} finally {
			session.close();
		}
	}

	@After
	public void after() {
		Session session = injector.getInstance(SessionFactory.class).openSession();
		try {
			Transaction transaction = session.beginTransaction();
			transaction.begin();
			session.createQuery("delete Soldier").executeUpdate();
			transaction.commit();
		} finally {
			session.close();
		}
	}

	@Test
	public void should_get_all_soldiers() {
		final Map<String, List<ThreeSoldiers>> allSoldiers = soldierResources.allSoldiers();

		assertThat(allSoldiers.keySet()).hasSize(1).containsOnly("soldiers");
		final List<ThreeSoldiers> list = allSoldiers.get("soldiers");
		assertThat(list).hasSize(1);
		final List<Soldier> firstThreeSoldiers = list.get(0).getSoldiers();
		assertThat(firstThreeSoldiers).hasSize(3);
		assertThat(firstThreeSoldiers).onProperty("name").containsOnly("Sylvester Stallone", "Jason Statham", "Jet Li");
		assertThat(firstThreeSoldiers).onProperty("hired").containsOnly(FALSE, FALSE, FALSE);
	}

	@Test
	public void should_hire_soldier() {
		soldierResources.hireSoldier("stallone", "Italian Stalion");

		final List<Soldier> firstThreeSoldiers = soldierResources.allSoldiers().get("soldiers").get(0).getSoldiers();

		Soldier stallone = find(firstThreeSoldiers, withId("stallone"));
		assertThat(stallone.getName()).isEqualTo("Sylvester Stallone");
		assertThat(stallone.getHired()).isTrue();
		assertThat(stallone.getCodeName()).isEqualTo("Italian Stalion");
	}
}
