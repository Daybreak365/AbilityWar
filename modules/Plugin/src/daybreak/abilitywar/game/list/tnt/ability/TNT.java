package daybreak.abilitywar.game.list.tnt.ability;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Bat;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

@AbilityManifest(name = "폭탄", rank = Rank.SPECIAL, species = Species.SPECIAL, explain = {

})
@Support.Version(min = NMSVersion.v1_11_R1)
public class TNT extends TNTAbility {

	private static final ItemStack ITEM_TNT = new ItemStack(Material.TNT);

	public TNT(Participant participant) {
		super(participant);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		detonate();
	}

	@Override
	public void resetInventory() {
		final PlayerInventory playerInventory = getPlayer().getInventory();
		playerInventory.clear();
		for (int i = 0; i < 9; i++) {
			playerInventory.setItem(i, ITEM_TNT);
		}
	}

	public void detonate() {
		getPlayer().setHealth(0);
		SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer().getLocation(), .35f, 1f);
		ParticleLib.EXPLOSION_LARGE.spawnParticle(getPlayer().getLocation(), .5, .5, .5, 3);
		final Bat[] bats = new Bat[10];
		final World world = getPlayer().getWorld();
		final Location playerLocation = getPlayer().getLocation();
		for (int i = 0; i < bats.length; i++) {
			final Bat bat = world.spawn(playerLocation, Bat.class);
			bat.setAwake(true);
			bat.setInvulnerable(true);
			bats[i] = bat;
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Bat bat : bats) {
					bat.remove();
				}
			}
		}.runTaskLater(AbilityWar.getPlugin(), 100L);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			resetInventory();
		}
	}
}
