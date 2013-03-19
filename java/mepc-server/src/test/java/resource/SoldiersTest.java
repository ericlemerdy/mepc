package resource;

import static java.lang.Boolean.FALSE;
import static org.fest.assertions.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import resources.Soldier;
import resources.Soldiers;
import resources.ThreeSoldiers;

public class SoldiersTest {

	@Test
	public void should_get_all_soldiers() {
		final Soldiers soldierResources = new Soldiers();

		final Map<String, List<ThreeSoldiers>> allSoldiers = soldierResources.allSoldiers();

		assertThat(allSoldiers.keySet()).hasSize(1).containsOnly("soldiers");
		final List<ThreeSoldiers> list = allSoldiers.get("soldiers");
		assertThat(list).hasSize(3);
		final List<Soldier> firstThreeSoldiers = list.get(0).getSoldiers();
		assertThat(firstThreeSoldiers).hasSize(3);
		assertThat(firstThreeSoldiers).onProperty("name").containsExactly("Sylvester Stallone", "Jason Statham", "Jet Li");
		assertThat(firstThreeSoldiers).onProperty("hired").containsExactly(FALSE, FALSE, FALSE);
	}

	@Test
	public void should_hire_soldier() {
		final Soldiers soldierResources = new Soldiers();
		soldierResources.hireSoldier("stallone");

		final List<Soldier> firstThreeSoldiers = soldierResources.allSoldiers().get("soldiers").get(0).getSoldiers();

		assertThat(firstThreeSoldiers.get(0).getName()).isEqualTo("Sylvester Stallone");
		assertThat(firstThreeSoldiers.get(0).getHired()).isTrue();
	}
}
