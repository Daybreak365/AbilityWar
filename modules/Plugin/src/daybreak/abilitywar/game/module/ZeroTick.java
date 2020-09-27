package daybreak.abilitywar.game.module;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.WeakHashMap;

@ModuleBase(ZeroTick.class)
public final class ZeroTick implements ListenerModule {

	public static final AttributeModifier PLUS_FIVE = new AttributeModifier(UUID.fromString("73a02314-fe33-11ea-adc1-0242ac120002"), "zerotick", 6, Operation.ADD_NUMBER);
	public static final AttributeModifier MINUS_SCALAR = new AttributeModifier(UUID.fromString("78a049fc-fe33-11ea-adc1-0242ac120002"), "zerotick", -.35, Operation.ADD_SCALAR);
	public static final AttributeModifier PLUS_POINT_ONE = new AttributeModifier(UUID.fromString("f9b7b830-fe34-11ea-adc1-0242ac120002"), "zerotick", .15, Operation.ADD_NUMBER);

	private final Map<LivingEntity, Integer> entities;

	private void addModifier(final Player player) {
		addModifier0(player, Attribute.GENERIC_ATTACK_SPEED, PLUS_FIVE);
		addModifier0(player, Attribute.GENERIC_ATTACK_DAMAGE, MINUS_SCALAR);
		addModifier0(player, Attribute.GENERIC_KNOCKBACK_RESISTANCE, PLUS_POINT_ONE);
	}

	private void addModifier0(final Player player, final Attribute attribute, final AttributeModifier modifier) {
		try {
			player.getAttribute(attribute).addModifier(modifier);
		} catch (IllegalArgumentException ignored) {}
	}

	private void removeModifier(final LivingEntity livingEntity) {
		livingEntity.getAttribute(Attribute.GENERIC_ATTACK_SPEED).removeModifier(PLUS_FIVE);
		livingEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(MINUS_SCALAR);
		livingEntity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).removeModifier(PLUS_POINT_ONE);
	}

	public ZeroTick(AbstractGame game) {
		final Collection<? extends Participant> participants = game.getParticipants();
		this.entities = new WeakHashMap<>(participants.size());
		for (Participant participant : participants) {
			final Player player = participant.getPlayer();
			if (player.isOnline()) {
				entities.put(player, player.getMaximumNoDamageTicks());
				addModifier(player);
				player.setMaximumNoDamageTicks(0);
			}
		}
	}

	@EventHandler
	private void onPlayerJoin(final PlayerJoinEvent e) {
		final Player player = e.getPlayer();
		entities.put(player, player.getMaximumNoDamageTicks());
		addModifier(player);
		player.setMaximumNoDamageTicks(0);
	}

	@EventHandler
	private void onPlayerQuit(final PlayerQuitEvent e) {
		final Player player = e.getPlayer();
		entities.remove(player);
		removeModifier(player);
	}

	@EventHandler
	private void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			final LivingEntity livingEntity = ((LivingEntity) e.getEntity());
			if (!entities.containsKey(livingEntity)) {
				entities.put(livingEntity, livingEntity.getMaximumNoDamageTicks());
			}
			livingEntity.setMaximumNoDamageTicks(0);
		}
	}

	@EventHandler
	private void onEntityDamageByEntity(EntityDamageByBlockEvent e) {
		this.onEntityDamage(e);
	}

	@EventHandler
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		this.onEntityDamage(e);
	}

	@Override
	public void unregister() {
		HandlerList.unregisterAll(this);
		for (final Iterator<Entry<LivingEntity, Integer>> iterator = entities.entrySet().iterator(); iterator.hasNext();) {
			final Entry<LivingEntity, Integer> entry = iterator.next();
			final LivingEntity livingEntity = entry.getKey();
			livingEntity.setMaximumNoDamageTicks(entry.getValue());
			if (livingEntity instanceof Player) {
				removeModifier(livingEntity);
			}
			iterator.remove();
		}
	}
}
