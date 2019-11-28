package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@AbilityManifest(Name = "광대", Rank = Rank.B, Species = Species.HUMAN)
public class Clown extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Clown.class, "Cooldown", 60,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static final SettingObject<Integer> RangeConfig = new SettingObject<Integer>(Clown.class, "Range", 10,
			"# 스킬 범위") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public Clown(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 스폰으로 이동합니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f스폰으로 이동한 후 10초 안에 철괴를 다시 우클릭하면 원래 위치로 돌아가"),
				ChatColor.translateAlternateColorCodes('&', "&f주변 " + RangeConfig.getValue() + "칸 이내의 플레이어들을 실명시킵니다."));
	}

	private Location OriginalPoint = null;
	
	private final CooldownTimer Cool = new CooldownTimer(CooldownConfig.getValue());

	private final DurationTimer Duration = new  DurationTimer(10, Cool) {

		@Override
		protected void onDurationStart() {
			OriginalPoint = getPlayer().getLocation();
			Location Spawn = getPlayer().getWorld().getSpawnLocation();
			
			getPlayer().teleport(Spawn);
		}

		@Override
		protected void onDurationProcess(int seconds) {}

		@Override
		protected void onDurationEnd() {}
		
	};
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.IRON_INGOT)) {
			if(ct.equals(ClickType.RIGHT_CLICK)) {
				if(!Duration.isDuration()) {
					if(!Cool.isCooldown()) {
						Duration.startTimer();
						
						return true;
					}
				} else {
					if(OriginalPoint != null) getPlayer().teleport(OriginalPoint);
					SoundLib.ENTITY_BAT_TAKEOFF.playSound(getPlayer());
					Duration.stopTimer(false);
					
					for(Player p : LocationUtil.getNearbyPlayers(getPlayer(), RangeConfig.getValue(), 250)) {
						SoundLib.ENTITY_WITHER_SPAWN.playSound(p);
						PotionEffects.BLINDNESS.addPotionEffect(p, 200, 2, true);
					}
				}
			}
		}
		
		return false;
	}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
