package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(Name = "황제", Rank = Rank.A, Species = Species.HUMAN)
public class TheEmperor extends AbilityBase {

	public static final SettingObject<Integer> DamageDecreaseConfig = new SettingObject<Integer>(TheEmperor.class, "DamageDecrease", 35,
			"# 공격 피해 감소량",
			"# 10으로 설정하면 공격을 받았을 때 전체 대미지의 90%를 받습니다.") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100;
		}
		
	};
	
	public TheEmperor(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f느리고 품위있게 걸어가며 공격 피해가 " + DamageDecreaseConfig.getValue() + "% 감소합니다."));
	}
	
	private final Timer Passive = new Timer() {
		
		@Override
		public void onStart() {}
		
		@Override
		public void onProcess(int count) {
			PotionEffects.SLOW.addPotionEffect(getPlayer(), 30, 1, true);
		}
		
		@Override
		public void onEnd() {}
		
	};
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}
	
	private final int DamageDecrease = DamageDecreaseConfig.getValue();
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			double damage = (e.getDamage() / 100) * (100 - DamageDecrease);
			e.setDamage(damage);

			if(getPlayer().getHealth() <= 2.0) {
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onRestrictionClear(AbilityRestrictionClearEvent e) {
		Passive.startTimer();
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
