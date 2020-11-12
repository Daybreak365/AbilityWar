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
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.serializable.SpawnLocation;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@AbilityManifest(name = "광대", rank = Rank.B, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 스폰으로 이동합니다. $[COOLDOWN_CONFIG]",
		"스폰으로 이동한 후 10초 안에 철괴를 다시 우클릭하면 원래 위치로 돌아가",
		"주변 $[RANGE_CONFIG]칸 이내의 플레이어들을 5초간 실명시킵니다."
})
@Tips(tip = {
		"정신 없는 플레이를 보여줄 수 있는 능력. 능력으로 상대의 능력을 피한 후",
		"다시 돌아와 실명으로 인해 무방비해진 적을 공격하세요."
}, strong = {
		@Description(subject = "능력 피하기", explain = {
				"어떤 능력이던 손쉽게 피할 수 있습니다. 기절을 당한 경우에도 순간 이동은",
				"가능하기 때문에 위기 상황을 쉽게 빠져나올 수 있습니다."
		})
}, weak = {
		@Description(subject = "스폰 근처에서의 플레이", explain = {
				"광대는 능력 사용 시 스폰으로 순간 이동합니다.",
				"스폰 주변에서 플레이를 하고 있다면 순간 이동이 아무 의미가 없을 것입니다."
		})
}, stats = @Stats(offense = Level.ZERO, survival = Level.THREE, crowdControl = Level.THREE, mobility = Level.ZERO, utility = Level.FIVE), difficulty = Difficulty.EASY)
public class Clown extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Clown.class, "cooldown", 45,
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

	public static final SettingObject<Integer> RANGE_CONFIG = abilitySettings.new SettingObject<Integer>(Clown.class, "range", 10,
			"# 스킬 범위") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public Clown(Participant participant) {
		super(participant);
	}

	private Location originalPoint = null;

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Duration skill = new Duration(10, cooldownTimer) {

		@Override
		protected void onDurationStart() {
			originalPoint = getPlayer().getLocation();
			final SpawnLocation spawnLocation = Settings.getSpawnLocation();
			if (getPlayer().getWorld().getName().equals(spawnLocation.world)) {
				getPlayer().teleport(spawnLocation.toBukkitLocation());
			} else {
				getPlayer().teleport(getPlayer().getWorld().getSpawnLocation());
			}
		}

		@Override
		protected void onDurationProcess(int seconds) {
		}

	};

	private final int range = RANGE_CONFIG.getValue();

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

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
				if (!skill.isDuration()) {
					if (!cooldownTimer.isCooldown()) {
						skill.start();

						return true;
					}
				} else {
					if (originalPoint != null) getPlayer().teleport(originalPoint);
					SoundLib.ENTITY_BAT_TAKEOFF.playSound(getPlayer());
					skill.stop(false);

					for (Player p : LocationUtil.getEntitiesInCircle(Player.class, getPlayer().getLocation(), range, predicate)) {
						SoundLib.ENTITY_WITHER_SPAWN.playSound(p);
						PotionEffects.BLINDNESS.addPotionEffect(p, 100, 2, true);
					}
				}
			}
		}

		return false;
	}

}
