package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.interfaces.TeamGame;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@AbilityManifest(name = "광대", rank = Rank.B, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 스폰으로 이동합니다. $[COOLDOWN_CONFIG]",
		"스폰으로 이동한 후 10초 안에 철괴를 다시 우클릭하면 원래 위치로 돌아가",
		"주변 $[RangeConfig]칸 이내의 플레이어들을 실명시킵니다."
})
public class Clown extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Clown.class, "Cooldown", 45,
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

	public static final SettingObject<Integer> RangeConfig = abilitySettings.new SettingObject<Integer>(Clown.class, "Range", 10,
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

	private final CooldownTimer cooldownTimer = new CooldownTimer(COOLDOWN_CONFIG.getValue());
	private final DurationTimer skill = new DurationTimer(10, cooldownTimer) {

		@Override
		protected void onDurationStart() {
			originalPoint = getPlayer().getLocation();
			getPlayer().teleport(getPlayer().getWorld().getSpawnLocation());
		}

		@Override
		protected void onDurationProcess(int seconds) {
		}

	};

	private final int range = RangeConfig.getValue();

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
				if (getGame() instanceof TeamGame) {
					final TeamGame teamGame = (TeamGame) getGame();
					final Participant entityParticipant = getGame().getParticipant(entity.getUniqueId());
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(getParticipant()) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(getParticipant())));
				}
			}
			return true;
		}
	};

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
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
