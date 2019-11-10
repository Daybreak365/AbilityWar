package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.timer.CooldownTimer;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.FallBlock;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.thread.TimerBase;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;

@AbilityManifest(Name = "카쟈드", Rank = Rank.A, Species = Species.GOD)
public class Khazhad extends AbilityBase {

	private static final SettingObject<Integer> LeftCooldownConfig = new SettingObject<Integer>(Khazhad.class, "LeftCooldown", 4, "# 좌클릭 쿨타임") {

		@Override
		public boolean Condition(Integer arg0) {
			return arg0 >= 0;
		}

	};

	private static final SettingObject<Integer> RightCooldownConfig = new SettingObject<Integer>(Khazhad.class, "RightCooldown", 10, "# 우클릭 쿨타임") {

		@Override
		public boolean Condition(Integer arg0) {
			return arg0 >= 0;
		}

	};

	public Khazhad(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 좌클릭하면 자신이 보고 있는 방향으로 얼음을 날립니다. " + Messager.formatCooldown(LeftCooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f주변을 지나가는 투사체들이 모두 얼어붙어 바닥으로 떨어집니다."),
				ChatColor.translateAlternateColorCodes('&', Messager.formatCooldown(RightCooldownConfig.getValue())));
	}

	private final CooldownTimer LeftCool = new CooldownTimer(this, LeftCooldownConfig.getValue(), "좌클릭");
	private final CooldownTimer RightCool = new CooldownTimer(this, RightCooldownConfig.getValue(), "우클릭").setActionbarNotice(false);

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if (mt.equals(MaterialType.IRON_INGOT)) {
			if (ct.equals(ClickType.LEFT_CLICK)) {
				if (!LeftCool.isCooldown()) {
					FallBlock fall = new FallBlock(Material.PACKED_ICE, getPlayer().getEyeLocation(), getPlayer().getLocation().getDirection().multiply(1.7)) {

						@Override
						public void onChangeBlock(FallingBlock block) {
							for (int x = -1; x < 1; x++) {
								for (int y = -1; y < 1; y++) {
									for (int z = -1; z < 1; z++) {
										Location l = block.getLocation().clone().add(x, y, z);
										l.getBlock().setType(Material.PACKED_ICE);
									}
								}
							}
						}

					};

					fall.toggleGlowing(true).toggleSetBlock(true).Spawn();

					LeftCool.startTimer();
					return true;
				}
			}
		}
		return false;
	}

	private final ArrayList<Projectile> projectiles = new ArrayList<>();

	TimerBase passive = new TimerBase() {
		@Override
		protected void onProcess(int count) {
			for (Projectile projectile : LocationUtil.getNearbyEntities(Projectile.class, getPlayer().getLocation(), 7, 7)) {
				if (!projectile.isOnGround() && !projectiles.contains(projectile)) {
					projectiles.add(projectile);
					projectile.setGravity(true);
					projectile.setVelocity(projectile.getVelocity().multiply(0.1));
				}
			}
		}
	}.setPeriod(1);

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}

	@Override
	protected void onRestrictClear() {
		passive.startTimer();
	}

}
