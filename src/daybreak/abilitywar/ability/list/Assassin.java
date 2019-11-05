package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.timer.CooldownTimer;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.thread.TimerBase;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;

@AbilityManifest(Name = "암살자", Rank = Rank.A, Species = Species.HUMAN)
public class Assassin extends AbilityBase {

	public static final SettingObject<Integer> DistanceConfig = new SettingObject<Integer>(Assassin.class, "Distance", 6,
			"# 스킬 데미지") {

		@Override
		public boolean Condition(Integer value) {
			return value > 0;
		}

	};
	
	public static final SettingObject<Integer> DamageConfig = new SettingObject<Integer>(Assassin.class, "Damage", 12,
			"# 스킬 데미지") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Assassin.class, "Cooldown", 25,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public static final SettingObject<Integer> TeleportCountConfig = new SettingObject<Integer>(Assassin.class, "TeleportCount", 4,
			"# 능력 사용 시 텔레포트 횟수") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};
	
	public Assassin(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 6칸 이내에 있는 적 " + TeleportCountConfig.getValue() + "명에게 이동하며"),
				ChatColor.translateAlternateColorCodes('&', "&f데미지를 줍니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}
	
	private final CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());

	private List<Damageable> Entities = null;
	
	private final int Distance = DistanceConfig.getValue();
	
	private final TimerBase Duration = new TimerBase(TeleportCountConfig.getValue()) {
		
		final Integer Damage = DamageConfig.getValue();
		
		@Override
		public void onStart() {}
		
		@Override
		public void onProcess(int count) {
			if(Entities != null) {
				if(Entities.size() >= 1) {
					Damageable e = Entities.get(0);
					Entities.remove(e);
					getPlayer().teleport(e);
					e.damage(Damage, getPlayer());
					SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
					SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
				} else {
					this.stopTimer(false);
				}
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(3);

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.IRON_INGOT)) {
			if(ct.equals(ClickType.RIGHT_CLICK)) {
				if(!Cool.isCooldown()) {
					this.Entities = LocationUtil.getNearbyDamageableEntities(getPlayer(), Distance, 5);
					if(Entities.size() > 0) {
						Duration.startTimer();
						Cool.startTimer();
						return true;
					} else {
						getPlayer().sendMessage( ChatColor.translateAlternateColorCodes('&', "&f" + Distance + "칸 이내에 &a엔티티&f가 존재하지 않습니다."));
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
