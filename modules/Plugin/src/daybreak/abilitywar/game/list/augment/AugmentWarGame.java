package daybreak.abilitywar.game.list.augment;

import daybreak.abilitywar.ability.event.AbilityCooldownEndEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.GameAliases;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.augment.Augment;
import daybreak.abilitywar.game.augment.AugmentRegistry;
import daybreak.abilitywar.game.augment.AugmentSelection;
import daybreak.abilitywar.game.augment.DefaultAugments;
import daybreak.abilitywar.game.augment.ParticipantAugments;
import daybreak.abilitywar.game.list.standard.WarGame;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@GameManifest(name = "증강 능력자 전쟁", description = {
		"§f일정 시간마다 참가자가 증강을 획득하는 능력자 전쟁입니다.",
		"",
		"§a● §f첫 증강은 게임 시작 직후 지급됩니다.",
		"§a● §f이후 2분마다 생존 참가자에게 새로운 증강이 지급됩니다."
})
@GameAliases({"증강", "augment", "augments"})
public class AugmentWarGame extends WarGame {

	private static final int FIRST_AUGMENT_SECONDS = 18;
	private static final int AUGMENT_INTERVAL_SECONDS = 120;

	private final ParticipantAugments participantAugments = new ParticipantAugments(this);
	private final AugmentRegistry augmentRegistry = DefaultAugments.createRegistry();
	private final AugmentSelection augmentSelection = new AugmentSelection(augmentRegistry, new Random());
	private final Set<UUID> lastStandUsed = new HashSet<>();

	@Override
	protected void progressGame(int seconds) {
		super.progressGame(seconds);
		if (seconds >= FIRST_AUGMENT_SECONDS && (seconds - FIRST_AUGMENT_SECONDS) % AUGMENT_INTERVAL_SECONDS == 0) {
			grantAugments(seconds);
		}
	}

	private void grantAugments(int seconds) {
		Bukkit.broadcastMessage("§d[증강] §f새로운 증강 라운드가 시작되었습니다. §7(" + seconds + "초)");
		for (Participant participant : getParticipants()) {
			if (!getDeathManager().isExcluded(participant.getPlayer())) {
				final List<Augment> candidates = augmentSelection.roll(participantAugments, participant, 3);
				if (!candidates.isEmpty()) {
					final Augment selected = candidates.get(0);
					selected.apply(participantAugments, participant);
					participant.getPlayer().sendMessage("§d[증강] §f획득: " + DefaultAugments.format(selected));
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		final Participant damager = getDamager(event.getDamager());
		final Participant victim = event.getEntity() instanceof Player ? getParticipant((Player) event.getEntity()) : null;

		if (damager != null) {
			if (hasAugment(damager, "glass_cannon")) event.setDamage(event.getDamage() * 1.25);
			if (hasAugment(damager, "executioner") && event.getEntity() instanceof LivingEntity) {
				final LivingEntity target = (LivingEntity) event.getEntity();
				final AttributeInstance maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
				if (maxHealth != null && target.getHealth() <= maxHealth.getValue() * 0.30) event.setDamage(event.getDamage() * 1.30);
			}
			if (hasAugment(damager, "arrow_focus") && event.getDamager() instanceof Projectile) {
				final double distanceSquared = damager.getPlayer().getLocation().distanceSquared(event.getEntity().getLocation());
				if (distanceSquared >= 100) event.setDamage(event.getDamage() * 1.35);
			}
			if (hasAugment(damager, "vampiric_blade") && victim != null) {
				heal(damager.getPlayer(), event.getFinalDamage() * 0.20);
			}
		}

		if (victim != null) {
			if (hasAugment(victim, "glass_cannon")) event.setDamage(event.getDamage() * 1.15);
			if (hasAugment(victim, "thornmail") && damager != null && !(event.getDamager() instanceof Projectile)) {
				damager.getPlayer().damage(event.getFinalDamage() * 0.20);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		final Participant participant = getParticipant((Player) event.getEntity());
		if (participant == null) return;
		final Player player = participant.getPlayer();
		if (hasAugment(participant, "last_stand") && !lastStandUsed.contains(player.getUniqueId()) && player.getHealth() - event.getFinalDamage() <= 0) {
			event.setCancelled(true);
			lastStandUsed.add(player.getUniqueId());
			player.setHealth(1.0);
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 1, true, false));
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 1, true, false));
			player.sendMessage("§d[증강] §f최후의 저항이 발동했습니다!");
			return;
		}
		if (hasAugment(participant, "comeback")) {
			final AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			if (maxHealth != null && player.getHealth() - event.getFinalDamage() <= maxHealth.getValue() * 0.40) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0, true, false));
				player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, true, false));
			}
		}
	}

	@EventHandler
	public void onAbilityCooldownEnd(AbilityCooldownEndEvent event) {
		final Participant participant = event.getParticipant();
		if (participant != null && hasAugment(participant, "ability_echo")) {
			participant.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0, true, false));
			participant.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0, true, false));
		}
	}

	private boolean hasAugment(Participant participant, String key) {
		return participant != null && participantAugments.hasAugment(participant, key);
	}

	private Participant getDamager(Entity damager) {
		if (damager instanceof Player) return getParticipant((Player) damager);
		if (damager instanceof Projectile) {
			final ProjectileSource shooter = ((Projectile) damager).getShooter();
			if (shooter instanceof Player) return getParticipant((Player) shooter);
		}
		return null;
	}

	private void heal(Player player, double amount) {
		final AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (maxHealth != null) player.setHealth(Math.min(maxHealth.getValue(), player.getHealth() + amount));
	}
}
