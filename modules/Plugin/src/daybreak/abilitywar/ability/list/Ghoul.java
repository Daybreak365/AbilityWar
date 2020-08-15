package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.SubscribeEvent.Priority;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

@AbilityManifest(name = "구울", rank = Rank.S, species = Species.UNDEAD, explain = {
	"BETA"
})
@Beta
public class Ghoul extends AbilityBase {

	private static final ItemStack BLOOD = new ItemStack(Material.REDSTONE);

	@SuppressWarnings("deprecation")
	public Ghoul(Participant participant) {
		super(participant);
		if (ServerVersion.getVersion() < 12) {
			subscribeEvent(PlayerPickupItemEvent.class, new EventConsumer<PlayerPickupItemEvent>() {
				@Override
				public void onEvent(PlayerPickupItemEvent event) {
					if (bloods.containsKey(event.getItem())) {
						event.setCancelled(true);
						if (getPlayer().equals(event.getPlayer())) {
							NMS.fakeCollect(getPlayer(), event.getItem());
							event.getItem().remove();
							getPlayer().setHealth(Math.min(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), getPlayer().getHealth() + bloods.remove(event.getItem())));
						}
					}
				}
			}, false, false, Priority.NORMAL);
		} else {
			subscribeEvent(EntityPickupItemEvent.class, new EventConsumer<EntityPickupItemEvent>() {
				@Override
				public void onEvent(EntityPickupItemEvent event) {
					if (bloods.containsKey(event.getItem())) {
						event.setCancelled(true);
						if (getPlayer().equals(event.getEntity())) {
							NMS.fakeCollect(getPlayer(), event.getItem());
							event.getItem().remove();
							getPlayer().setHealth(Math.min(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), getPlayer().getHealth() + bloods.remove(event.getItem())));
						}
					}
				}
			}, false, false, Priority.NORMAL);
		}
	}

	@SubscribeEvent(onlyRelevant = true, ignoreCancelled = true)
	private void onEntityRegainHealth(final EntityRegainHealthEvent e) {
		if (e.getRegainReason() == RegainReason.REGEN) e.setAmount(e.getAmount() / 2);
	}

	private final Map<Item, Double> bloods = new HashMap<>();

	@SubscribeEvent
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && !getPlayer().equals(e.getEntity())) {
			if (isOwner(e.getDamager())) {
				spawnBlood(e.getEntity().getLocation(), e.getFinalDamage() / 3, true);
			} else {
				spawnBlood(e.getEntity().getLocation(), e.getFinalDamage() / 2, false);
			}
		}
	}

	public void spawnBlood(final Location location, final double healthToRegain, final boolean immediateCollect) {
		final Item item = location.getWorld().dropItemNaturally(location, BLOOD);
		if (immediateCollect) {
			NMS.fakeCollect(getPlayer(), item);
			item.remove();
			getPlayer().setHealth(Math.min(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), getPlayer().getHealth() + healthToRegain));
		} else {
			bloods.put(item, healthToRegain);
		}
	}

	@SubscribeEvent
	private void onItemMerge(final ItemMergeEvent e) {
		if (bloods.containsKey(e.getEntity())) {
			e.setCancelled(true);
		}
	}

	private boolean isOwner(final Entity entity) {
		if (entity instanceof Projectile) {
			return getPlayer().equals(((Projectile) entity).getShooter());
		} else return getPlayer().equals(entity);
	}

}
