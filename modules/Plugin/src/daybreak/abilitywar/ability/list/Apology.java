package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.SubscribeEvent.Priority;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.math.LocationUtil.Predicates;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

@AbilityManifest(name = "기나죄", rank = Rank.C, species = Species.HUMAN, explain = {
		"§b기§f분 §b나§f쁘셨다면 §b죄§f송합니다...",
		"플레이어를 타격한 후 지속 시간 5초가 지나면 대상에게",
		"넣었던 모든 대미지의 절반만큼 대상을 회복시켜주며, 본인은",
		"3분의 1만큼 회복합니다. 지속 시간 5초가 지나기 전에 같은 대상을",
		"다시 타격할 경우 지속 시간이 5초로 연장됩니다."
})
public class Apology extends AbilityBase {

	private static final String[] MESSAGES = {
			"너무 많이 때렸나..?",
			"원래 맞으면서 크는 ㅂ...",
			"헐... 많이 아프셨죠...",
			"그러니까 누가 맞을 짓을 하ㄹ...",
			"아파요? 아프니까 청ㅊ.. (읍읍)"
	};
	private final Predicate<Entity> STRICT = Predicates.STRICT(getPlayer());
	private final Map<UUID, DamageStacker> damageStackers = new HashMap<>();
	private final Random random = new Random();

	public Apology(Participant participant) {
		super(participant);
	}

	@SubscribeEvent(priority = Priority.HIGHEST, ignoreCancelled = true)
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if (damager instanceof Projectile) {
			ProjectileSource source = ((Projectile) damager).getShooter();
			if (getPlayer().equals(source)) {
				damager = getPlayer();
			}
		}
		if (getPlayer().equals(damager) && e.getEntity() instanceof Player && STRICT.test(e.getEntity())) {
			if (damageStackers.containsKey(e.getEntity().getUniqueId())) {
				damageStackers.get(e.getEntity().getUniqueId()).addDamage(e.getFinalDamage());
			} else {
				damageStackers.put(e.getEntity().getUniqueId(), new DamageStacker((Player) e.getEntity(), e.getFinalDamage()));
			}
		}
	}

	@SubscribeEvent
	private void onDeath(PlayerDeathEvent e) {
		if (damageStackers.containsKey(e.getEntity().getUniqueId())) {
			damageStackers.get(e.getEntity().getUniqueId()).stop(true);
		}
	}

	private class DamageStacker extends Timer {

		private final ActionbarChannel actionbarChannel = newActionbarChannel();
		private final Player target;
		private double damage = 0.0;

		public DamageStacker(Player target, double damage) {
			super(TaskType.REVERSE, 5);
			this.target = target;
			this.damage += damage;
			start();
		}

		@Override
		protected void run(int count) {
			actionbarChannel.update("§f" + target.getName() + " §6지속 시간§f: " + getCount() + "초");
		}

		private void addDamage(double damage) {
			if (isRunning()) {
				this.damage += damage;
				setCount(5);
				actionbarChannel.update("§f" + target.getName() + " §6지속 시간§f: " + getCount() + "초");
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
			target.setHealth(Math.min(target.getHealth() + (damage / 2.0), target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			getPlayer().setHealth(Math.min(getPlayer().getHealth() + (damage / 3.0), getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			target.sendMessage(getPlayer().getDisplayName() + "§7: §f" + MESSAGES[random.nextInt(MESSAGES.length)] + " 기분 나쁘셨다면 죄송합니다...");
		}

		@Override
		protected void onSilentEnd() {
			actionbarChannel.update("§f" + target.getName() + " §6지속 시간 §f종료");
			actionbarChannel.unregister();
			damageStackers.remove(target.getUniqueId());
		}
	}

}
