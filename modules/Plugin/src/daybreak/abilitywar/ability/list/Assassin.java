package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Bleed;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.function.Predicate;

@AbilityManifest(name = "암살자", rank = Rank.A, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 $[DISTANCE_CONFIG]칸 이내에 있는 생명체 $[TELEPORT_COUNT_CONFIG]명(마리)에게 이동하며",
		"각각 $[DAMAGE_CONFIG]의 대미지를 줍니다. $[COOLDOWN_CONFIG]",
		"대미지를 받은 생명체는 3초간 추가로 출혈 피해를 입습니다."
})
@Tips(tip = {
		"텔레포트로 적에게 빠르게 접근하고, 출혈로 추가 대미지를 주면서 적이 도망가지",
		"못하도록 막아 빠르게 암살하세요."
}, strong = {
		@Description(subject = "빠른 접근", explain = {
				"능력 사용 시 텔레포트를 이용해 적에게 빠르게 접근할 수 있습니다."
		})
}, weak = {
		@Description(subject = "근접이 유리한 상대", explain = {
				"근접이 유리한 상대에게 이 능력을 사용하게 되면, 스스로 호랑이 굴로",
				"들어가는 꼴 밖에 되지 않습니다. 신중하게 능력을 사용하고 싶다면,",
				"상대의 능력을 파악하고 사용해보세요."
		})
}, stats = @Stats(offense = Level.SIX, survival = Level.ZERO, crowdControl = Level.TWO, mobility = Level.FIVE, utility = Level.ZERO), difficulty = Difficulty.EASY)
public class Assassin extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> DISTANCE_CONFIG = abilitySettings.new SettingObject<Integer>(Assassin.class, "Distance", 8,
			"# 스킬 대미지") {

		@Override
		public boolean condition(Integer value) {
			return value > 0;
		}

	};

	public static final SettingObject<Integer> DAMAGE_CONFIG = abilitySettings.new SettingObject<Integer>(Assassin.class, "Damage", 8,
			"# 스킬 대미지") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Assassin.class, "Cooldown", 35,
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

	public static final SettingObject<Integer> TELEPORT_COUNT_CONFIG = abilitySettings.new SettingObject<Integer>(Assassin.class, "TeleportCount", 4,
			"# 능력 사용 시 텔레포트 횟수") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Assassin(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}
	};

	private final int damage = DAMAGE_CONFIG.getValue();
	private final int distance = DISTANCE_CONFIG.getValue();
	private LinkedList<LivingEntity> entities = null;
	private final AbilityTimer skill = new AbilityTimer(TELEPORT_COUNT_CONFIG.getValue()) {

		@Override
		public void run(int count) {
			if (entities != null) {
				if (!entities.isEmpty()) {
					final LivingEntity target = entities.remove();
					getPlayer().teleport(target);
					target.damage(damage, getPlayer());
					SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
					SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
					Bleed.apply(getGame(), target, TimeUnit.SECONDS, 3);
				} else {
					stop(false);
				}
			}
		}

	}.setPeriod(TimeUnit.TICKS, 3).register();

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown()) {
			this.entities = new LinkedList<>(LocationUtil.getNearbyEntities(LivingEntity.class, getPlayer().getLocation(), distance, distance, predicate));
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
