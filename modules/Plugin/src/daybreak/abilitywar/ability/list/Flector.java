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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;

@Beta
@AbilityManifest(Name = "플렉터", Rank = Rank.S, Species = Species.HUMAN)
public class Flector extends AbilityBase {

	public Flector(Participant participant) {
		super(participant,
				"투사체를 바라본 상태로 검을 휘두를 경우, 해당 투사체를",
				"반사합니다. 본인이 발사한 투사체는 반사할 수 없으며, ",
				"다른 플레이어가 발사한 발사체를 반사할 경우 " + ChatColor.YELLOW + "♥" + ChatColor.WHITE + " 만큼의",
				ChatColor.YELLOW + "추가 체력" + ChatColor.WHITE + "을 얻습니다.");
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	private static final Set<Material> materials = ImmutableSet.of(MaterialX.WOODEN_SWORD.parseMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.parseMaterial(), Material.DIAMOND_SWORD);

	private final CenteredBoundingBox boundingBox = CenteredBoundingBox.of(getPlayer().getLocation(), -1.5, -1.5, -1.5, 1.5, 1.5, 1.5);

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerInteract(PlayerInteractEvent e) {
		if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && e.getItem() != null && materials.contains(e.getItem().getType())) {
			Projectile projectile = LocationUtil.getEntityLookingAt(Projectile.class, boundingBox, getPlayer(), 5, null);
			if (projectile != null && !projectile.isOnGround() && projectile.isValid() && !getPlayer().equals(projectile.getShooter())) {
				projectile.setVelocity(getPlayer().getLocation().getDirection().multiply(2.2 * NMSHandler.getNMS().getAttackCooldown(e.getPlayer())));
				SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
				if (projectile.getShooter() instanceof Player)
					NMSHandler.getNMS().setAbsorptionHearts(getPlayer(), Math.min(NMSHandler.getNMS().getAbsorptionHearts(getPlayer()) + 2, 20));
			} else {
				Deflectable deflectable = LocationUtil.getCustomEntityLookingAt(Deflectable.class, getGame(), boundingBox, getPlayer(), 5, null);
				if (deflectable != null && !getPlayer().equals(deflectable.getShooter())) {
					deflectable.onDeflect(getParticipant(), getPlayer().getLocation().getDirection().multiply(2.2 * NMSHandler.getNMS().getAttackCooldown(e.getPlayer())));
					SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
					if (deflectable.getShooter() instanceof Player)
						NMSHandler.getNMS().setAbsorptionHearts(getPlayer(), Math.min(NMSHandler.getNMS().getAbsorptionHearts(getPlayer()) + 2, 20));
				}
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {

	}

}
