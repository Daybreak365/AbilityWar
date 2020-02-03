package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.minecraft.FallBlock;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

@AbilityManifest(Name = "낙법의 달인", Rank = Rank.B, Species = Species.HUMAN)
public class ExpertOfFall extends AbilityBase {

	public ExpertOfFall(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f수십년간의 고된 수련으로 낙법과 하나가 된 낙법의 달인."),
				ChatColor.translateAlternateColorCodes('&', "&f낙하해 땅에 닿았을 때 자동으로 물낙법을 하며,"),
				ChatColor.translateAlternateColorCodes('&', "&f낙하 거리에 비례해 주변 3칸 내의 생명체들에게 대미지를 줍니다."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			if (e.getCause().equals(DamageCause.FALL)) {
				e.setCancelled(true);
				Block block = getPlayer().getLocation().getBlock();
				Material blockType = block.getType();
				new Timer(1) {
					@Override
					protected void onStart() {
						block.setType(Material.WATER);
					}

					@Override
					protected void onProcess(int count) {
					}

					@Override
					protected void onEnd() {
						block.setType(blockType);
					}
				}.setPeriod(10).startTimer();
				SoundLib.ENTITY_PLAYER_SPLASH.playSound(getPlayer());

				Block belowBlock = block.getRelative(BlockFace.DOWN);
				new FallBlock(belowBlock.getType(), belowBlock.getLocation().add(0, 1, 0), getPlayer().getLocation().toVector().subtract(belowBlock.getLocation().toVector()).multiply(-0.1).setY(Math.random())) {
					@Override
					public void onChangeBlock(FallingBlock block) {
					}
				}.Spawn();
				for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(getPlayer(), 3, 3)) {
					damageable.damage(getPlayer().getFallDistance() / 1.4, getPlayer());
				}
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
