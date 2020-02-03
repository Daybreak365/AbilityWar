package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.base.ProgressBar;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.VectorUtil.Vectors;
import daybreak.abilitywar.utils.math.geometry.Circle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@AbilityManifest(Name = "해커", Rank = Rank.A, Species = Species.HUMAN)
public class Hacker extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Hacker.class, "Cooldown", 180,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Hacker.class, "Duration", 5,
			"# 능력 지속시간") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public Hacker(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 자신에게 제일 가까운 플레이어를 해킹해 좌표를 알아내고"),
				ChatColor.translateAlternateColorCodes('&', "&f" + DurationConfig.getValue() + "초간 해당 플레이어가 움직이지 못하게 합니다."),
				Messager.formatCooldown(CooldownConfig.getValue()));
	}

	private Player target = null;

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final int stunTick = DurationConfig.getValue() * 20;
	private final Timer skill = new Timer(100) {

		private int count;
		private ProgressBar progressBar;

		@Override
		protected void onStart() {
			this.count = 1;
			this.progressBar = new ProgressBar(100, 20);
		}

		@Override
		protected void onEnd() {
			if (target != null) {
				getPlayer().resetTitle();

				int X = (int) target.getLocation().getX();
				int Y = (int) target.getLocation().getY();
				int Z = (int) target.getLocation().getZ();
				getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + target.getName() + "&f님은 &aX " + X + "&f, &aY " + Y + "&f, &aZ " + Z + "&f에 있습니다."));

				target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5해킹당했습니다!"));
				target.sendTitle(ChatColor.translateAlternateColorCodes('&', "&5해킹당했습니다!"), "", 0, 40, 0);
				getGame().getEffectManager().Stun(target, stunTick);
				particleShow.startTimer();
			}
		}

		@Override
		protected void onProcess(int count) {
			if (target != null) {
				progressBar.step();

				getPlayer().sendTitle(
						ChatColor.translateAlternateColorCodes('&', "&e" + target.getName() + " &f해킹중..."),
						ChatColor.translateAlternateColorCodes('&', progressBar.toString() + " &f" + this.count + "%"),
						0, 5, 0
				);

				this.count++;
			}
		}
	}.setPeriod(1);

	private final int amount = 25;
	private final Vectors top = Circle.of(1, amount);
	private final Vectors bottom = Circle.of(1, amount);
	private final RGB PURPLE = RGB.of(168, 121, 171);

	private final Timer particleShow = new Timer(stunTick) {

		private double y;
		private boolean add;

		@Override
		public void onStart() {
			y = 0.0;
			add = true;
		}

		@Override
		public void onProcess(int count) {
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

	}.setPeriod(1);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!cooldownTimer.isCooldown()) {
					Player target = LocationUtil.getNearestPlayer(getPlayer());

					if (target != null) {
						this.target = target;
						skill.startTimer();

						cooldownTimer.startTimer();

						return true;
					} else {
						getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a가장 가까운 플레이어&f가 존재하지 않습니다."));
					}
				}
			}
		}

		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
