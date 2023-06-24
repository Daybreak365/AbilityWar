package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.event.AbilityCooldownEndEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Effect;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Oppress;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.manager.effect.event.ParticipantPreEffectApplyEvent;
import daybreak.abilitywar.game.manager.effect.registry.EffectType;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@AbilityManifest(name = "제우스", rank = Rank.S, species = Species.GOD, explain = {
		"§7패시브 §8- §e하늘의 지배자§f: §5제압§f되지 않습니다. §b공중§f에 있는 동안 타게팅의 대상이",
		" 되지 않습니다. 낙하 피해를 입지 않습니다.",
		"§7웅크리기 §8- §e행차§f: 이동을 방해하는 모든 상태 이상을 해제하고, 이후 6초간 비행할",
		" 수 있습니다. 능력 지속 중 철괴 우클릭으로 지속 시간 1.5초를 소모해 바라보는",
		" 지점에 §e번개§f를 떨어뜨리고 순간 이동할 수 있습니다. 비행을 시작하고 끝내는",
		" 지점에서 주위에 §e번개§f를 떨어뜨립니다. 이 능력으로 인해 발생한 §e번개§f에 맞은",
		" 플레이어는 1.5초간 기절합니다. $[COOLDOWN_CONFIG] (단, 다른 능력의 §c쿨타임§f이 종료될",
		" 때마다 §c쿨타임§f이 25% 감소)"
})
public class Zeus extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Zeus.class, "fly-cooldown", 150,
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

	public Zeus(Participant participant) {
		super(participant);
	}

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

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private FlyAbility flyAbility;

	private final AbilityTimer noTarget = new AbilityTimer() {
		@Override
		protected void run(int count) {
			if (!getPlayer().isOnGround()) {
				getParticipant().attributes().TARGETABLE.setValue(false);
			} else {
				getParticipant().attributes().TARGETABLE.setValue(true);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@SubscribeEvent
	private void onCooldownEnd(AbilityCooldownEndEvent e) {
		if (e.getCooldown() != cooldownTimer && cooldownTimer.isRunning()) {
			cooldownTimer.setCount((int) (cooldownTimer.getCount() * 0.75f));
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onEffectApply(ParticipantPreEffectApplyEvent e) {
		if (e.getEffectType() == Oppress.registration) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onSneak(PlayerToggleSneakEvent e) {
		if (e.isSneaking()) {
			if (!cooldownTimer.isCooldown() && flyAbility == null) {
				cooldownTimer.start();
				final List<Effect> toRemove = new ArrayList<>();
				for (Effect effect : getParticipant().getEffects()) {
					final List<EffectType> list = Arrays.asList(effect.getRegistration().getManifest().type());
					if (list.contains(EffectType.MOVEMENT_RESTRICTION) || list.contains(EffectType.MOVEMENT_INTERRUPT)) {
						toRemove.add(effect);
					}
				}
				for (Effect effect : toRemove) {
					effect.stop(true);
				}
				new FlyAbility().start();
			}
		}
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
			if (flyAbility != null) {
				if (flyAbility.getCount() >= 30) {
					Block firstSolid = null;
					try {
						for (BlockIterator iterator = new BlockIterator(getPlayer().getWorld(), getPlayer().getLocation().toVector(), getPlayer().getLocation().getDirection(), 1, 35); iterator.hasNext(); ) {
							final Block block = iterator.next();
							if (block.getType().isSolid()) {
								firstSolid = block;
								break;
							}
						}
					} catch (IllegalStateException ignored) {
					}
					if (firstSolid != null) {
						flyAbility.setCount(flyAbility.getCount() - 30);
						final Location loc = firstSolid.getLocation().clone().add(0, 1, 0);
						loc.getWorld().strikeLightningEffect(loc);
						for (Player target : LocationUtil.getNearbyEntities(Player.class, loc, 4, 4, predicate)) {
							Stun.apply(getGame().getParticipant(target), TimeUnit.TICKS, 30);
						}
						getPlayer().teleport(loc.setDirection(getPlayer().getLocation().getDirection()));
						return true;
					} else {
						getPlayer().sendMessage(ChatColor.RED + "바라보는 방향에 이동할 수 있는 곳이 없습니다.");
					}
				} else getPlayer().sendMessage("§c더 이상 사용할 수 없습니다.");
			} else getPlayer().sendMessage("§b행차 §f사용 중에만 사용할 수 있습니다.");
		}
		return false;
	}

	private class FlyAbility extends AbilityTimer {

		private final BossBar bossBar = Bukkit.createBossBar("§f행차", BarColor.BLUE, BarStyle.SOLID);

		private FlyAbility() {
			super(TaskType.REVERSE, 6 * 20);
			setPeriod(TimeUnit.TICKS, 1);
			Zeus.this.flyAbility = this;
			bossBar.setProgress(1);
			bossBar.addPlayer(getPlayer());
		}

		private void lightning() {
			new AbilityTimer(3) {
				Location center;
				@Override
				public void onStart() {
					center = getPlayer().getLocation();
				}

				@Override
				public void run(int count) {
					double playerY = getPlayer().getLocation().getY();
					for (Iterator<Location> iterator = Circle.iteratorOf(center, 2 * (5 - getCount()), 7); iterator.hasNext(); ) {
						Location loc = iterator.next();
						loc.setY(LocationUtil.getFloorYAt(loc.getWorld(), playerY, loc.getBlockX(), loc.getBlockZ()));
						loc.getWorld().strikeLightningEffect(loc);
						for (Damageable d : LocationUtil.getNearbyEntities(Damageable.class, loc, 4, 4, predicate)) {
							if (!d.equals(getPlayer())) {
								d.damage(d.getHealth() / 5, getPlayer());
								if (d instanceof Player) {
									Stun.apply(getGame().getParticipant(d.getUniqueId()), TimeUnit.TICKS, 30);
								}
							}
						}
						loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 3, false, false);
					}
				}

			}.setPeriod(TimeUnit.TICKS, 2).start();
		}

		@Override
		protected void onStart() {
			lightning();
		}

		@Override
		protected void run(int count) {
			bossBar.setProgress(count / (double) getMaximumCount());
			getPlayer().setAllowFlight(true);
			getPlayer().setFlying(true);
			getPlayer().setFlySpeed(.1f);
		}

		@Override
		protected void onEnd() {
			lightning();
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			getPlayer().setFlying(false);
			final GameMode mode = getPlayer().getGameMode();
			getPlayer().setAllowFlight(mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE);
			bossBar.removeAll();
			Zeus.this.flyAbility = null;
		}
	}

	@SubscribeEvent
	private void onEntityDamage(EntityDamageEvent e) {
		if (!e.isCancelled() && getPlayer().equals(e.getEntity()) && e.getCause().equals(DamageCause.FALL)) {
			e.setCancelled(true);
			getPlayer().sendMessage("§b낙하 피해를 입지 않습니다.");
			SoundLib.ENTITY_BAT_TAKEOFF.playSound(getPlayer());
		}

		if (e.getEntity().equals(getPlayer())) {
			if (e.getCause().equals(DamageCause.LIGHTNING) || e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			if (e.getCause().equals(DamageCause.LIGHTNING) || e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByBlockEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			if (e.getCause().equals(DamageCause.LIGHTNING) || e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
				e.setCancelled(true);
			}
		}
	}
	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			noTarget.start();
		}
	}
}
