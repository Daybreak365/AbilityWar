package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.FallBlock;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.ArrayList;

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

	private final CooldownTimer LeftCool = new CooldownTimer(LeftCooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		if (materialType.equals(Material.IRON_INGOT)) {
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

	private final ArrayList<Projectile> projectiles = new ArrayList<Projectile>() {
		@Override
		public boolean add(Projectile projectile) {
			if (size() >= 15) clear();
			return super.add(projectile);
		}
	};

	private final Timer passive = new Timer() {
		@Override
		protected void onProcess(int count) {
			Location center = getPlayer().getLocation();
			for (Projectile projectile : LocationUtil.getNearbyEntities(Projectile.class, center, 7, 7)) {
				if (!projectile.isOnGround() && !projectiles.contains(projectile) && LocationUtil.isInCircle(center, projectile.getLocation(), 7)) {
					projectiles.add(projectile);
					projectile.setGravity(true);
					projectile.setVelocity(projectile.getVelocity().multiply(0.1));
					new Timer(3) {
						@Override
						protected void onStart() {
							projectile.getLocation().getBlock().setType(Material.ICE);
						}

						@Override
						protected void onProcess(int count) {
						}

						@Override
						protected void onEnd() {
							projectile.getLocation().getBlock().setType(Material.AIR);
						}
					}.startTimer();
				}
			}
		}
	}.setPeriod(1);

	@SubscribeEvent
	private void onProjectileHit(ProjectileHitEvent e) {
		projectiles.remove(e.getEntity());
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onRestrictionClear(AbilityRestrictionClearEvent e) {
		passive.startTimer();
	}

}
