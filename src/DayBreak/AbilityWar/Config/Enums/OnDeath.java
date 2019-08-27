package DayBreak.AbilityWar.Config.Enums;

import org.bukkit.ChatColor;

import com.google.common.base.Enums;

public enum OnDeath {

	탈락(ChatColor.WHITE + "플레이어를 탈락시킵니다.") {
		@Override
		public OnDeath Next() {
			return OnDeath.관전모드;
		}
	},
	관전모드(ChatColor.WHITE + "플레이어를 관전 모드로 전환합니다.") {
		@Override
		public OnDeath Next() {
			return OnDeath.없음;
		}
	},
	없음(ChatColor.WHITE + "아무 작업도 하지 않습니다.") {
		@Override
		public OnDeath Next() {
			return OnDeath.탈락;
		}
	};
	
	private final String description;
	
	private OnDeath(final String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public abstract OnDeath Next();
	
	/**
	 * 해당 이름의 상수가 있으면 반환합니다.
	 * 존재하지 않을 경우 'OnDeath.없음'을 반환합니다.
	 */
	public static OnDeath getIfPresent(String name) {
		return Enums.getIfPresent(OnDeath.class, name).or(OnDeath.없음);
	}
	
}
