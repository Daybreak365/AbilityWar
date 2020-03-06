package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Circle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.Iterator;

@AbilityManifest(Name = "제우스", Rank = Rank.S, Species = Species.GOD)
public class Zeus extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Zeus.class, "Cooldown", 180,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Zeus(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f번개의 신 제우스."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 주변에 번개를 떨어뜨리며 폭발을 일으킵니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f번개를 맞은 플레이어는 3초간 구속됩니다."),
				ChatColor.translateAlternateColorCodes('&', "&f번개 대미지와 폭발 대미지를 받지 않습니다."));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private final Timer Skill = new Timer(3) {

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
				for (Damageable d : LocationUtil.getNearbyDamageableEntities(loc, 4, 4)) {
					if (!d.equals(getPlayer())) {
						d.damage(d.getHealth() / 5, getPlayer());
						if (d instanceof Player) {
							Zeus.this.getGame().getEffectManager().Stun((Player) d, 60);
						}
					}
				}
				loc.getWorld().createExplosion(loc, 3);
			}
		}

		@Override
		public void onEnd() {
		}

	}.setPeriod(TimeUnit.TICKS, 2);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!cooldownTimer.isCooldown()) {
					Skill.start();

					cooldownTimer.start();

					return true;
				}
			}
		}

		return false;
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
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
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
