package daybreak.abilitywar.config.enums;

import com.google.common.base.Enums;
import org.bukkit.ChatColor;

public enum CooldownDecrease {

	LARGO("라르고", 25) {
		public CooldownDecrease next() {
			return ANDANTE;
		}
	},
	ANDANTE("안단테", 50) {
		public CooldownDecrease next() {
			return MODERATO;
		}
	},
	MODERATO("모데라토", 75) {
		public CooldownDecrease next() {
			return ALLEGRO;
		}
	},
	ALLEGRO("알레그로", 90) {
		public CooldownDecrease next() {
			return PRESTO;
		}
	},
	PRESTO("프레스토", 100) {
		public CooldownDecrease next() {
			return LARGO;
		}
	};

	private final String displayName, lore;
	private final int percentage;

	CooldownDecrease(String displayName, int percentage) {
		this.displayName = displayName;
		this.lore = ChatColor.GRAY + "쿨타임 " + percentage + "% 감소";
		this.percentage = percentage;
	}

	public static CooldownDecrease getIfPresent(String name) {
		return Enums.getIfPresent(CooldownDecrease.class, name).or(CooldownDecrease.ALLEGRO);
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getLore() {
		return lore;
	}

	public int getPercentage() {
		return percentage;
	}

	public abstract CooldownDecrease next();

}
