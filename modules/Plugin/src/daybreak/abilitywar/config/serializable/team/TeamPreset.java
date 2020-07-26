package daybreak.abilitywar.config.serializable.team;

import com.google.common.base.Enums;
import com.google.common.base.Preconditions;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.team.interfaces.Members;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class TeamPreset implements ConfigurationSerializable {

	private final Map<String, TeamScheme> schemes;
	private final String name;
	private DivisionType divisionType;

	public TeamPreset(String name, DivisionType divisionType) {
		this.schemes = new HashMap<>();
		this.name = name;
		this.divisionType = Preconditions.checkNotNull(divisionType);
	}

	public TeamPreset(String name, DivisionType divisionType, TeamScheme... schemes) {
		this.schemes = new HashMap<>();
		this.name = name;
		this.divisionType = Preconditions.checkNotNull(divisionType);
		for (TeamScheme scheme : schemes) {
			addScheme(scheme);
		}
	}

	public TeamPreset(Map<String, Object> args) {
		final Map<String, TeamScheme> schemeList = new HashMap<>();
		for (Object o : (List<?>) args.get("schemes")) {
			if (o instanceof TeamScheme) {
				TeamScheme scheme = (TeamScheme) o;
				schemeList.put(scheme.getName(), scheme);
			}
		}
		this.schemes = schemeList;
		this.name = args.get("name").toString();
		this.divisionType = Enums.getIfPresent(DivisionType.class, args.get("divisionType").toString()).or(DivisionType.EQUAL);
	}

	public String getName() {
		return name;
	}

	public DivisionType getDivisionType() {
		return divisionType;
	}

	public void setDivisionType(DivisionType divisionType) {
		this.divisionType = divisionType;
	}

	public Collection<TeamScheme> getSchemes() {
		return schemes.values();
	}

	public boolean addScheme(TeamScheme scheme) {
		if (schemes.get(scheme.getName()) == null) {
			schemes.put(scheme.getName(), scheme);
			return true;
		} else return false;
	}

	public boolean hasScheme(String name) {
		return schemes.containsKey(name);
	}

	public void removeScheme(String name) {
		schemes.remove(name);
	}

	public boolean isValid() {
		return schemes.size() > 0;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		map.put("divisionType", divisionType.name());
		map.put("schemes", new ArrayList<>(schemes.values()));
		return map;
	}

	public enum DivisionType {

		EQUAL("모든 팀 동일", "모든 팀에 같은 수의 플레이어가 있도록 팀원을 분배합니다.", "약 1명 정도 차이날 수 있습니다.") {
			public void divide(Teamable game, TeamPreset structure) {
				final LinkedList<Participant> participants = new LinkedList<>(game.getParticipants());
				Collections.shuffle(participants);
				final List<TeamScheme> schemes = new ArrayList<>(structure.getSchemes());
				for (int i = 0, remain = game.getParticipants().size(), partsLeft = schemes.size(); partsLeft > 0; i++, partsLeft--) {
					int size = (remain + partsLeft - 1) / partsLeft;
					final TeamScheme scheme = schemes.get(i);
					final Members team = game.newTeam(scheme.getName(), scheme.getDisplayName());
					for (int j = 0; j < size && participants.size() > 0; j++) {
						game.setTeam(participants.removeFirst(), team);
					}
					remain -= size;
				}
			}

			public DivisionType next() {
				return EQUAL;
			}
		};

		public final String name;
		public final List<String> lore;

		DivisionType(String name, String... lore) {
			this.name = name;
			this.lore = Arrays.asList(lore);
		}

		public abstract void divide(Teamable game, TeamPreset structure);

		public abstract DivisionType next();

	}

	public static class TeamScheme implements ConfigurationSerializable {

		private final String name;
		private String displayName;

		public TeamScheme(String name, String displayName) {
			this.name = name;
			this.displayName = displayName;
		}

		public TeamScheme(Map<String, Object> args) {
			this.name = args.get("name").toString();
			this.displayName = args.get("displayName").toString();
		}

		public String getName() {
			return name;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public Map<String, Object> serialize() {
			Map<String, Object> map = new HashMap<>();
			map.put("name", name);
			map.put("displayName", displayName);
			return map;
		}

	}

}
