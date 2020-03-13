package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;

import java.util.LinkedList;

@AbilityManifest(name = "암살자", rank = Rank.A, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 $[DistanceConfig]칸 이내에 있는 생명체 $[TeleportCountConfig]명(마리)에게 이동하며",
		"각각 $[DamageConfig]의 대미지를 줍니다. $[CooldownConfig]"
})
public class Assassin extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> DistanceConfig = new SettingObject<Integer>(Assassin.class, "Distance", 10,
			"# 스킬 대미지") {

		@Override
		public boolean Condition(Integer value) {
			return value > 0;
		}

	};

	public static final SettingObject<Integer> DamageConfig = new SettingObject<Integer>(Assassin.class, "Damage", 9,
			"# 스킬 대미지") {

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

		@Override
		public String toString() {
			return Messager.formatCooldown(getValue());
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
		super(participant);
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private LinkedList<Damageable> entities = null;

	private final int damage = DamageConfig.getValue();
	private final int distance = DistanceConfig.getValue();

	private final Timer skill = new Timer(TeleportCountConfig.getValue()) {

		@Override
		public void run(int count) {
			if (entities != null) {
				if (!entities.isEmpty()) {
					Damageable e = entities.remove();
					getPlayer().teleport(e);
					e.damage(damage, getPlayer());
					SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
					SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
				} else {
					stop(false);
				}
			}
		}

	}.setPeriod(TimeUnit.TICKS, 3);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !cooldownTimer.isCooldown()) {
			this.entities = new LinkedList<>(LocationUtil.getNearbyDamageableEntities(getPlayer(), distance, distance));
			if (entities.size() > 0) {
				skill.start();
				cooldownTimer.start();
				return true;
			} else {
				getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&f" + distance + "칸 이내에 &a엔티티&f가 존재하지 않습니다."));
			}
		}

		return false;
	}

}
