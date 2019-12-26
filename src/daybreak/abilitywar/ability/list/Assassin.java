package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;

import java.util.LinkedList;

@AbilityManifest(Name = "암살자", Rank = Rank.A, Species = Species.HUMAN)
public class Assassin extends AbilityBase {

	public static final SettingObject<Integer> DistanceConfig = new SettingObject<Integer>(Assassin.class, "Distance", 10,
			"# 스킬 데미지") {

		@Override
		public boolean Condition(Integer value) {
			return value > 0;
		}

	};
	
	public static final SettingObject<Integer> DamageConfig = new SettingObject<Integer>(Assassin.class, "Damage", 9,
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
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 " + DistanceConfig.getValue() + "칸 이내에 있는 엔티티 " + TeleportCountConfig.getValue() + "명(마리)에게 이동하며"),
				ChatColor.translateAlternateColorCodes('&', "&f각각 " + DamageConfig.getValue() + "의 데미지를 줍니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}
	
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private LinkedList<Damageable> entities = null;
	
	private final int distance = DistanceConfig.getValue();
	private final int damage = DamageConfig.getValue();
	
	private final Timer durationTimer = new Timer(TeleportCountConfig.getValue()) {

		@Override
		public void onProcess(int count) {
			if(entities != null) {
				if(!entities.isEmpty()) {
					Damageable e = entities.remove();
					getPlayer().teleport(e);
					e.damage(damage, getPlayer());
					SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
					SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
				} else {
					stopTimer(false);
				}
			}
		}

	}.setPeriod(3);

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.IRON_INGOT)) {
			if(ct.equals(ClickType.RIGHT_CLICK)) {
				if(!cooldownTimer.isCooldown()) {
					this.entities = new LinkedList<>(LocationUtil.getNearbyDamageableEntities(getPlayer(), distance, 5));
					if(entities.size() > 0) {
						durationTimer.startTimer();
						cooldownTimer.startTimer();
						return true;
					} else {
						getPlayer().sendMessage( ChatColor.translateAlternateColorCodes('&', "&f" + distance + "칸 이내에 &a엔티티&f가 존재하지 않습니다."));
					}
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
