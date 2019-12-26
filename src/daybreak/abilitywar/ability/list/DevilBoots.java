package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.LinkedList;

@AbilityManifest(Name = "악마의 부츠", Rank = Rank.B, Species = Species.OTHERS)
public class DevilBoots extends AbilityBase {

	public DevilBoots(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f신속하게 이동하며 지나가는 자리에 불이 붙습니다. 화염 피해를 받지 않습니다."));
	}

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	private final Timer SPEED = new Timer() {
		@Override
		protected void onProcess(int count) {
			PotionEffects.SPEED.addPotionEffect(getPlayer(), 20, 1, true);
		}
	};

	private final LinkedList<Block> blocks = new LinkedList<>();

	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().equals(getPlayer())) {
			Block to = e.getTo().getBlock();
			Block toBelow = to.getLocation().subtract(0, 1, 0).getBlock();
			if (to.getType().equals(Material.AIR) || to.getType().equals(Material.SNOW)) {
				to.setType(Material.FIRE);
				if (to.getType().equals(Material.FIRE)) {
					blocks.add(to);
					if (blocks.size() >= 30) {
						blocks.removeFirst().setType(Material.AIR);
					}
				}
			}
			if (toBelow.getType().equals(Material.SNOW_BLOCK)) {
				toBelow.setType(Material.DIRT);
			} else if (toBelow.getType().equals(Material.PACKED_ICE) || toBelow.getType().equals(Material.FROSTED_ICE) || toBelow.getType().equals(Material.ICE) || (ServerVersion.getVersion() >= 13 && toBelow.getType().equals(Material.BLUE_ICE))) {
				toBelow.setType(Material.WATER);
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			DamageCause cause = e.getCause();
			if (cause.equals(DamageCause.FIRE) || cause.equals(DamageCause.FIRE_TICK) || cause.equals(DamageCause.LAVA)) {
				e.setCancelled(true);
			}
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onRestrictionClear(AbilityRestrictionClearEvent e) {
		SPEED.startTimer();
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {
	}

}
