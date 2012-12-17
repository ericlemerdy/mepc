package resource;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import resources.Soldiers;
import resources.Soldiers.Soldier;
import resources.Soldiers.ThreeSoldiers;

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
	}
}
