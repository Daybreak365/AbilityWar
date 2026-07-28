package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.SubscribeEvent.Priority;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.minecraft.entity.health.Healths;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@AbilityManifest(name = "구울", rank = Rank.S, species = Species.UNDEAD, explain = {
		"§7철괴 우클릭 §f- §c체내 효소§f: 자연 회복할 수 없습니다.",
		"§7근접 공격 §f- §4할퀴기§f: 가한 피해량의 절반만큼 체력을 회복합니다. $[COOLDOWN_CONFIG]",
        " 쿨타임과 관계없이, 공격하면 대상의 주변으로 핏방울을 떨어뜨리고,",
		" 핏방울을 줏으면 가한 피해량만큼의 체력을 회복합니다.",
		"§7패시브 §f- §c식탐§f: 체력이 $[HEALTH_SCALE_CONFIG]% 이하인 적에게는 가하는 피해가 $[DAMAGE_INCREASE_CONFIG]% 증가합니다."
})
public class Ghoul extends AbilityBase {

	public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Ghoul.class, "cooldown", 5,
			"# 쿨타임", "# 쿨타임 감소 최대치는 50%입니다.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final AbilitySettings.SettingObject<Integer> HEALTH_SCALE_CONFIG = abilitySettings.new SettingObject<Integer>(Ghoul.class, "health-scale", 40,
			"# 남은 체력 비율 (%)") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public static final AbilitySettings.SettingObject<Integer> DAMAGE_INCREASE_CONFIG = abilitySettings.new SettingObject<Integer>(Ghoul.class, "damage-increase", 25,
			"# 공격력 증가량 (%)") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	private static class Component {

		private final RGB color;
		private final double radius, length;

		private Component(RGB color, double radius, double length) {
			this.color = color;
			this.radius = radius;
			this.length = length;
		}

	}

	private static final Component[] components = new Component[]{
			new Component(RGB.BLACK, 1.5, 1.05),
			new Component(RGB.RED, 1.5, 1)
	};

	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue(), "할퀴기", CooldownDecrease._50);
	private final double healthScale = HEALTH_SCALE_CONFIG.getValue() * 0.01, damageIncrease = 1 + (DAMAGE_INCREASE_CONFIG.getValue() * 0.01);
	private boolean state = false;

	public Ghoul(Participant participant) {
		super(participant);
	}

	@SubscribeEvent(onlyRelevant = true, ignoreCancelled = true)
	private void onEntityRegainHealth(final EntityRegainHealthEvent e) {
		if (e.getRegainReason() == RegainReason.REGEN) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent(priority = Priority.HIGHEST, ignoreCancelled = true)
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		final Entity entity = e.getEntity();
		if (getPlayer().equals(e.getDamager()) && entity instanceof LivingEntity) {
			final LivingEntity livingEntity = (LivingEntity) entity;
            if ((livingEntity.getHealth() / livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) <= healthScale) {
                e.setDamage(e.getDamage() * damageIncrease);
            }
			final double amount = e.getFinalDamage() * 0.5;
            if (!cooldown.isRunning()) {
                final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), amount, RegainReason.CUSTOM);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    getPlayer().setHealth(RangesKt.coerceIn(getPlayer().getHealth() + event.getAmount(), 0, getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
                }
                new Cut(entity, state = !state).start();
                ParticleLib.BLOCK_CRACK.spawnParticle(livingEntity.getEyeLocation(), .3f, .3f, .3f, 30, MaterialX.REDSTONE_BLOCK);
                cooldown.start();
            }

			new Bloods(e.getEntity().getLocation(), (e.getEntity() instanceof Player) ? amount * 2 : amount).start();
		}
	}

	public class Cut extends AbilityTimer {

		private final Location base;
		private final double angle;

		private Cut(Entity target, boolean state) {
			super(state ? TaskType.NORMAL : TaskType.REVERSE, 3);
			setPeriod(TimeUnit.TICKS, 1);
			final Vector delta = target.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).normalize();
			this.base = target.getLocation().clone().subtract(delta.multiply(1.6)).setDirection(delta.normalize()).add(0, 1, 0);
			this.angle = state ? 25 : -25;
		}

		@Override
		protected void run(int count) {
			final Vector direction = base.getDirection().setY(0).normalize(), axis = VectorUtil.rotateAroundAxisY(direction.clone(), 90);
			for (int i = 0; i < 8; i++) {
				final double radians = 0.78539816339744830961566084581988 + 0.06544984694978735913463840381832 * ((count - 1) * 8 + i);
				final double cos = FastMath.cos(radians), sin = FastMath.sin(radians);
				for (Component component : components) {
					final Location loc = base.clone().add(
							VectorUtil.rotateAroundAxis(
									VectorUtil.rotateAroundAxis(
											VectorUtil.rotateAroundAxisY(new Vector(cos, 0, component.length * sin), -base.getYaw()), direction, angle
									), axis, base.getPitch()
							).multiply(component.radius)
					).subtract(0, .75, 0);
					for (int y = 0; y < 3; y++) {
						ParticleLib.REDSTONE.spawnParticle(loc.add(0, .5, 0), component.color);
					}
				}
			}

		}

	}

	public class Bloods extends AbilityTimer implements Listener {

		private final Location location;
		private final double heal;
		private Item blood;
		private final Random random = new Random();

		public Bloods(Location location, Double heal) {
			super(TaskType.REVERSE, 300);
			setPeriod(TimeUnit.TICKS, 1);
			this.location = location;
			this.heal = heal;
		}

		@Override
		public void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
			ItemStack bloodStack = new ItemStack(Material.REDSTONE);
			blood = location.getWorld().dropItem(location, bloodStack);
			blood.setVelocity(new Vector(((random.nextDouble() * 2) - 1) / 2.5, .55, ((random.nextDouble() * 2) - 1) / 2.5));
		}

		@Override
		public void run(int count) {
			if (count <= 50) {
                blood.setGlowing(count % 2 == 0);
			}
		}

		@Override
		public void onEnd() {
			onSilentEnd();
		}

		@Override
		public void onSilentEnd() {
			HandlerList.unregisterAll(this);
			blood.remove();
		}

		@EventHandler
		public void onEntityPickup(EntityPickupItemEvent e) {
			if (e.getItem().equals(blood)) {
				e.setCancelled(true);
				if (e.getEntity().equals(getPlayer()) && this.getCount() < 190) {
					this.stop(false);
					final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), heal, RegainReason.CUSTOM);
					Bukkit.getPluginManager().callEvent(event);
					if (!event.isCancelled()) {
						SoundLib.ENTITY_ITEM_PICKUP.playSound(getPlayer().getLocation(), 1, 0.65f);
						Healths.setHealth(getPlayer(), getPlayer().getHealth() + event.getAmount());
						ParticleLib.HEART.spawnParticle(getPlayer().getLocation(), 0.2, 2, 0.2, 2, 0);
					}
				}
			}
		}

	}

}
