package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.ProgressBar;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Predicates;
import daybreak.abilitywar.utils.base.math.VectorUtil.Vectors;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.compat.nms.NMSHandler;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import java.util.function.Predicate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@AbilityManifest(name = "해커", rank = Rank.A, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 자신에게 가장 가까운 플레이어를 해킹해 좌표를 알아내고",
		"$[DurationConfig]초간 해당 플레이어를 움직이지 못하게 합니다. $[CooldownConfig]",
		"해킹을 당하는 플레이어는 해킹 진척도를 볼 수 있습니다."
})
public class Hacker extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(Hacker.class, "Cooldown", 180,
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

	public static final SettingObject<Integer> DurationConfig = abilitySettings.new SettingObject<Integer>(Hacker.class, "Duration", 5,
			"# 능력 지속시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Hacker(Participant participant) {
		super(participant);
	}

	private Player target = null;

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final int stunDuration = DurationConfig.getValue();
	private final RGB PURPLE = RGB.of(113, 43, 204);

	private final int amount = 25;
	private final Vectors top = Circle.of(1, amount);
	private final Vectors bottom = Circle.of(1, amount);
	private final Timer particleShow = new Timer(stunDuration * 20) {

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

	}.setPeriod(TimeUnit.TICKS, 1);
	private final Timer skill = new Timer(100) {

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
				NMSHandler.getNMS().clearTitle(getPlayer());

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

				NMSHandler.getNMS().sendTitle(getPlayer(),
						"§e" + target.getName() + " §f해킹중...",
						progressBar.toString() + " §f" + this.count + "%",
						0, 5, 0
				);
				targetActionbar.update(progressBar.toString("A", ChatColor.DARK_PURPLE + ChatColor.MAGIC.toString(), ChatColor.WHITE + ChatColor.MAGIC.toString()));

				this.count++;
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	private final Predicate<Entity> STRICT_PREDICATE = Predicates.STRICT(getPlayer());

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!cooldownTimer.isCooldown()) {
					Player target = LocationUtil.getNearestEntity(Player.class, getPlayer().getLocation(), STRICT_PREDICATE);

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
