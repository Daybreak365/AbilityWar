package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.versioncompat.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@AbilityManifest(Name = "빠른 회복", Rank = Rank.A, Species = Species.HUMAN)
public class FastRegeneration extends AbilityBase {
	
	public static final SettingObject<Integer> RegenSpeedConfig = new SettingObject<Integer>(FastRegeneration.class, "RegenSpeed", 20,
			"# 회복 속도를 설정합니다.",
			"# 숫자가 낮을수록 회복이 더욱 빨라집니다.") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};
	
	public FastRegeneration(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f다른 능력들에 비해서 더 빠른 속도로 체력을 회복합니다."));
	}
	
	private final Timer Passive = new Timer() {
		
		@Override
		public void onStart() {}
		
		@Override
		public void onProcess(int count) {
			if(!isRestricted()) {
				Player p = getPlayer();
				if(!p.isDead()) {
					double MaxHealth = VersionUtil.getMaxHealth(p);
					
					if(p.getHealth() < MaxHealth) {
						p.setHealth(Math.min(p.getHealth() + 1.5, 20.0));
					}
				}
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(RegenSpeedConfig.getValue());
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}
	
	@Override
	public void onRestrictClear() {
		Passive.startTimer();
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
