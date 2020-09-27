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
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.ProgressBar;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil.Vectors;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@AbilityManifest(name = "해커", rank = Rank.A, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 자신에게 가장 가까운 플레이어를 해킹해 좌표를 알아내고",
		"$[DURATION_CONFIG]초간 해당 플레이어를 움직이지 못하게 합니다. $[COOLDOWN_CONFIG]",
		"해킹을 당하는 플레이어는 해킹 진척도를 볼 수 있습니다."
})
@Tips(tip = {
		"상대를 불시에 해킹해 움직이지 못하게 만든 다음, 뒤에서 공격해 빠르게",
		"죽이세요. 타게팅이 불가능한 상대라도, 타게팅이 가능할 때 해킹이 시작되어",
		"이미 해킹이 진행 중인 경우, 타게팅 여부에 상관 없이 이동이 불가능해집니다."
}, strong = {
		@Description(subject = "스턴", explain = {
				"한 대상을 긴 시간동안 기절시킬 수 있다는 것은, 굉장히 의미가",
				"큽니다. 그 시간동안 상대는 무방비해지며, 자신을 보호할 수 없게",
				"되죠. 물론 일부 능력은 이에 대처할 수 있게 해주기도 합니다."
		})
}, weak = {
		@Description(subject = "선쿨", explain = {
				"해커는 해킹을 시작하면 약 5초간 선쿨을 가지며, 그 동안",
				"해킹을 당하는 상대는 해킹의 진척도를 확인할 수 있습니다.",
				"빠르게 이에 대처할 수 있는 상대라면, 해커가 할 수 있는건",
				"아무것도 없겠죠."
		}),
		@Description(subject = "접근 불가능", explain = {
				"사용자를 바라보지 않은 상태로도 밀쳐낼 수 있는 능력을 가진",
				"상대에게 취약합니다. 해킹을 했는데도 접근을 할 수 없다면,",
				"아무 의미가 없을겁니다."
		}),
		@Description(subject = "순간 이동", explain = {
				"해킹은 이동만 막을 뿐, 순간 이동은 막지 않습니다. 순간 이동을",
				"통해 다른 장소로 이동할 수 있는 능력들은 해킹을 해도 별 효과가",
				"없을겁니다."
		})
}, stats = @Stats(offense = Level.ZERO, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.SEVEN), difficulty = Difficulty.EASY)
public class Hacker extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Hacker.class, "Cooldown", 180,
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

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Hacker.class, "DURATION", 5,
			"# 스턴 지속시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Hacker(Participant participant) {
		super(participant);
	}

	private Player target = null;

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final int stunDuration = DURATION_CONFIG.getValue();
	private static final RGB PURPLE = RGB.of(113, 43, 204);

	private final int amount = 25;
	private final Vectors top = Circle.of(1, amount);
	private final Vectors bottom = Circle.of(1, amount);
	private final AbilityTimer particleShow = new AbilityTimer(stunDuration * 20) {

		private double y;
		private boolean add;

		@Override
		public void onStart() {
			y = 0.0;
			add = true;
		}

		@Override
		public void run(int count) {
			if (target != null) {
				if (add && y >= 2.0) {
					add = false;
				} else if (!add && y <= 0) {
					add = true;
				}

				if (add) {
					y += 0.1;
				} else {
					y -= 0.1;
				}

				for (Location location : top.toLocations(target.getLocation().add(0, y, 0)))
					ParticleLib.REDSTONE.spawnParticle(location, PURPLE);
				for (Location location : bottom.toLocations(target.getLocation().add(0, 2.0 - y, 0)))
					ParticleLib.REDSTONE.spawnParticle(location, PURPLE);
			}
		}

		@Override
		public void onEnd() {
			target = null;
		}

	}.setPeriod(TimeUnit.TICKS, 1).register();
	private final AbilityTimer skill = new AbilityTimer(100) {

		private int count;
		private ProgressBar progressBar;
		private ActionbarChannel targetActionbar;

		@Override
		protected void onStart() {
			this.count = 1;
			this.progressBar = new ProgressBar(100, 20);
			this.targetActionbar = getGame().getParticipant(target).actionbar().newChannel();
		}

		@Override
		protected void onEnd() {
			if (target != null) {
				NMS.clearTitle(getPlayer());

				int X = (int) target.getLocation().getX(), Y = (int) target.getLocation().getY(), Z = (int) target.getLocation().getZ();
				getPlayer().sendMessage("§e" + target.getName() + "§f님은 §aX " + X + "§f, §aY " + Y + "§f, §aZ " + Z + "§f에 있습니다.");

				target.sendMessage("§5해킹당했습니다!");
				Stun.apply(getGame().getParticipant(target), TimeUnit.SECONDS, stunDuration);
				particleShow.start();
			}
			this.progressBar = null;
			targetActionbar.unregister();
			this.targetActionbar = null;
		}

		@Override
		protected void onSilentEnd() {
			this.progressBar = null;
			targetActionbar.unregister();
			this.targetActionbar = null;
		}

		@Override
		protected void run(int count) {
			if (target != null) {
				progressBar.step();

				NMS.sendTitle(getPlayer(),
						"§e" + target.getName() + " §f해킹중...",
						progressBar.toString() + " §f" + this.count + "%",
						0, 5, 0
				);
				targetActionbar.update(progressBar.toString("A", ChatColor.DARK_PURPLE + ChatColor.MAGIC.toString(), ChatColor.WHITE + ChatColor.MAGIC.toString()));

				this.count++;
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1).register();

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
				if (!cooldownTimer.isCooldown()) {
					Player target = LocationUtil.getNearestEntity(Player.class, getPlayer().getLocation(), predicate);

					if (target != null) {
						this.target = target;
						skill.start();

						cooldownTimer.start();

						return true;
					} else {
						getPlayer().sendMessage("§a가장 가까운 플레이어§f가 존재하지 않습니다.");
					}
				}
			}
		}

		return false;
	}

}
