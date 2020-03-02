package daybreak.abilitywar.config.enums;

import com.google.common.base.Enums;
import org.bukkit.ChatColor;

public enum OnDeath {

	탈락(true, ChatColor.RED + "● " + ChatColor.WHITE + "플레이어를 탈락시킵니다.", ChatColor.YELLOW + "● " + ChatColor.WHITE + "플레이어 능력을 제거합니다.") {
		@Override
		public OnDeath next() {
			return OnDeath.관전모드;
		}
	},
	관전모드(true, ChatColor.GOLD + "● " + ChatColor.WHITE + "플레이어를 관전 모드로 전환합니다.", ChatColor.YELLOW + "● " + ChatColor.WHITE + "플레이어 능력을 제거합니다.") {
		@Override
		public OnDeath next() {
			return OnDeath.없음;
		}
	},
	없음(false, ChatColor.GREEN + "● " + ChatColor.WHITE + "아무 작업도 하지 않습니다.") {
		@Override
		public OnDeath next() {
			return OnDeath.탈락;
		}
	};

	private final boolean abilityRemoval;
	private final String[] description;

	OnDeath(boolean abilityRemoval, String... description) {
		this.abilityRemoval = abilityRemoval;
		this.description = description;
	}

	public boolean getAbilityRemoval() {
		return abilityRemoval;
	}

	public String[] getDescription() {
		return description;
	}

	public abstract OnDeath next();

	/**
	 * 해당 이름의 상수가 있으면 반환합니다.
	 * 존재하지 않을 경우 'OnDeath.없음'을 반환합니다.
	 */
	public static OnDeath getIfPresent(String name) {
		return Enums.getIfPresent(OnDeath.class, name).or(OnDeath.없음);
	}

}
