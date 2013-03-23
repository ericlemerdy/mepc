package resources;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.google.common.base.Predicate;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Soldier {
	public static final Predicate<Soldier> withId(final String soldierId) {
		return new Predicate<Soldier>() {
			@Override
			public boolean apply(Soldier input) {
				return input.getId().equals(soldierId);
			}
		};
	}

	@NonNull
	public String id;
	@NonNull
	public String name;
	@NonNull
	public String description;
	public Boolean hired = Boolean.FALSE;
	public String codeName;

}