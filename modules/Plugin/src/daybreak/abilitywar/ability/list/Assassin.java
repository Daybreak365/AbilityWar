package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Bleed;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Predicates;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.LinkedList;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

@AbilityManifest(name = "암살자", rank = Rank.A, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 $[DistanceConfig]칸 이내에 있는 생명체 $[TeleportCountConfig]명(마리)에게 이동하며",
		"각각 $[DamageConfig]의 대미지를 줍니다. $[CooldownConfig]",
		"대미지를 받은 생명체는 3초간 추가로 출혈 피해를 입습니다."
})
public class Assassin extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> DistanceConfig = abilitySettings.new SettingObject<Integer>(Assassin.class, "Distance", 10,
			"# 스킬 대미지") {

		@Override
		public boolean condition(Integer value) {
			return value > 0;
		}

	};

	public static final SettingObject<Integer> DamageConfig = abilitySettings.new SettingObject<Integer>(Assassin.class, "Damage", 9,
			"# 스킬 대미지") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(Assassin.class, "Cooldown", 25,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> TeleportCountConfig = abilitySettings.new SettingObject<Integer>(Assassin.class, "TeleportCount", 4,
			"# 능력 사용 시 텔레포트 횟수") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Assassin(Participant participant) {
		super(participant);
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private final Predicate<Entity> STRICT_PREDICATE = Predicates.STRICT(getPlayer());

	private final int damage = DamageConfig.getValue();
	private final int distance = DistanceConfig.getValue();
	private LinkedList<LivingEntity> entities = null;
	private final Timer skill = new Timer(TeleportCountConfig.getValue()) {

		@Override
		public void run(int count) {
			if (entities != null) {
				if (!entities.isEmpty()) {
					LivingEntity e = entities.remove();
					getPlayer().teleport(e);
					e.damage(damage, getPlayer());
					SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
					SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
					Bleed.apply(getGame(), e, TimeUnit.SECONDS, 3);
				} else {
					stop(false);
				}
			}
		}

	}.setPeriod(TimeUnit.TICKS, 3);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !cooldownTimer.isCooldown()) {
			this.entities = new LinkedList<>(LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), distance, distance, STRICT_PREDICATE));
			if (entities.size() > 0) {
				skill.start();
				cooldownTimer.start();
				return true;
			} else {
				getPlayer().sendMessage("§f" + distance + "칸 이내에 §a엔티티§f가 존재하지 않습니다.");
			}
		}

		return false;
	}

}
