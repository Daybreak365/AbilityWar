package daybreak.abilitywar.ability.list;

import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.minecraft.compat.NMSHandler;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.math.geometry.Boundary.CenteredBoundingBox;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;

@Beta
@AbilityManifest(Name = "플렉터", Rank = Rank.S, Species = Species.HUMAN)
public class Flector extends AbilityBase {

	public Flector(Participant participant) {
		super(participant,
				"BETA");
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	private static final Set<Material> materials = ImmutableSet.of(MaterialX.WOODEN_SWORD.parseMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.parseMaterial(), Material.DIAMOND_SWORD);

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerInteract(PlayerInteractEvent e) {
		if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && e.getItem() != null && materials.contains(e.getItem().getType())) {
			Projectile projectile = LocationUtil.getEntityLookingAt(Projectile.class, CenteredBoundingBox.of(getPlayer().getLocation(), -1.5, -1.5, -1.5, 1.5, 1.5, 1.5), getPlayer(), 5, null);
			if (projectile != null && !projectile.isOnGround() && projectile.isValid()) {
				projectile.setVelocity(getPlayer().getLocation().getDirection().multiply(2.2 * NMSHandler.getNMS().getAttackCooldown(e.getPlayer())));
				SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
			} else {
				Deflectable deflectable = LocationUtil.getCustomEntityLookingAt(Deflectable.class, getGame(), CenteredBoundingBox.of(getPlayer().getLocation(), -1.5, -1.5, -1.5, 1.5, 1.5, 1.5), getPlayer(), 5, null);
				if (deflectable != null) {
					deflectable.onDeflect(getParticipant(), getPlayer().getLocation().getDirection().multiply(2.2 * NMSHandler.getNMS().getAttackCooldown(e.getPlayer())));
					SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
				}
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {

	}

}
