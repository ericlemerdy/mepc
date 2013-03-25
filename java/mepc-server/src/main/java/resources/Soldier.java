package resources;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.google.common.base.Predicate;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "SOLDIER")
public class Soldier {
	public static final Predicate<Soldier> withId(final String soldierId) {
		return new Predicate<Soldier>() {
			@Override
			public boolean apply(Soldier input) {
				return input.getId().equals(soldierId);
			}
		};
	}

	@Id
	@NonNull
	public String id;
	@NonNull
	public String name;
	@NonNull
	public String description;
	public Boolean hired = Boolean.FALSE;
	public String codeName;

}