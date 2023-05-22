package daybreak.abilitywar.game.manager.effect;

import com.google.common.collect.MapMaker;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Effect;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.registry.ApplicationMethod;
import daybreak.abilitywar.game.manager.effect.registry.EffectManifest;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.block.Blocks;
import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Map;
import java.util.WeakHashMap;

public class Frost {

	private static final Map<LivingEntity, Object> frozenEntities = new WeakHashMap<>();

	public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
		registration.apply(participant, timeUnit, duration);
	}

	public static final EffectRegistration<ParticipantFrost> registration = EffectRegistry.registerEffect(ParticipantFrost.class);

	@EffectManifest(name = "빙결", displayName = "§b빙결", method = ApplicationMethod.UNIQUE_LONGEST, description = {
			"몸이 얼어붙어 이동이 불가능해지고 대미지를 입지 않습니다.",
			"얼음으로 인해 시야가 차단됩니다."
	})
	public static class ParticipantFrost extends Effect implements Listener {

		private static final Map<Location, ParticipantFrost> frosts = new MapMaker().weakValues().makeMap();

		public static ParticipantFrost getFrost(Location location) {
			return frosts.get(location);
		}

		private final Participant target;
		private final Block[] blocks = new Block[2];
		private final IBlockSnapshot[] snapshots = new IBlockSnapshot[2];
		private final Location teleport;
		public boolean noDamage = true;

		public ParticipantFrost(Participant target, TimeUnit timeUnit, int duration) {
			target.getGame().super(registration, target, timeUnit.toTicks(duration));
			setPeriod(TimeUnit.TICKS, 1);
			this.target = target;
			blocks[0] = target.getPlayer().getEyeLocation().getBlock();
			blocks[1] = blocks[0].getRelative(BlockFace.DOWN);

			frosts.put(blocks[0].getLocation(), this);
			frosts.put(blocks[1].getLocation(), this);

			if (ServerVersion.getVersion() >= 10) target.getPlayer().setInvulnerable(true);
			for (int i = 0; i < 2; i++) {
				snapshots[i] = Blocks.createSnapshot(blocks[i]);
				blocks[i].setType(Material.ICE);
			}
			this.teleport = blocks[1].getLocation().clone().add(0.5, 0, 0.5).setDirection(target.getPlayer().getLocation().getDirection());
		}

		@EventHandler
		public void onBlockBreak(BlockBreakEvent e) {
			if (e.getBlock().equals(blocks[0]) || e.getBlock().equals(blocks[1])) e.setCancelled(true);
		}

		@EventHandler
		public void onExplode(BlockExplodeEvent e) {
			e.blockList().removeIf(block -> block.equals(blocks[0]) || block.equals(blocks[1]));
		}

		@EventHandler
		public void onExplode(EntityExplodeEvent e) {
			e.blockList().removeIf(block -> block.equals(blocks[0]) || block.equals(blocks[1]));
		}

		@EventHandler
		private void onEntityDamage(EntityDamageEvent e) {
			if (e.getEntity().equals(target.getPlayer()) && noDamage) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
			onEntityDamage(e);
		}

		@EventHandler
		private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
			onEntityDamage(e);
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@Override
		protected void run(int count) {
			super.run(count);
			target.getPlayer().teleport(teleport);
		}

		@Override
		protected void onEnd() {
			super.onEnd();
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			super.onSilentEnd();
			HandlerList.unregisterAll(this);
			if (ServerVersion.getVersion() >= 10) target.getPlayer().setInvulnerable(false);
			for (int i = 0; i < 2; i++) {
				snapshots[i].apply();
			}
			frosts.remove(blocks[0].getLocation());
			frosts.remove(blocks[1].getLocation());
		}

	}

	public static void apply(AbstractGame game, LivingEntity livingEntity, TimeUnit timeUnit, int duration) {
		if (game.isParticipating(livingEntity.getUniqueId())) {
			apply(game.getParticipant(livingEntity.getUniqueId()), timeUnit, duration);
		} else {
			if (frozenEntities.putIfAbsent(livingEntity, Object.class) == null) {
				new LivingEntityFrost(game, livingEntity, timeUnit, duration).start();
			}
		}
	}

	private static class LivingEntityFrost extends GameTimer implements Listener {

		private final LivingEntity target;
		private final Block[] blocks = new Block[2];
		private final IBlockSnapshot[] snapshots = new IBlockSnapshot[2];
		private final Location teleport;

		private LivingEntityFrost(AbstractGame game, LivingEntity target, TimeUnit timeUnit, int duration) {
			game.super(TaskType.REVERSE, timeUnit.toTicks(duration));
			setPeriod(TimeUnit.TICKS, 1);
			this.target = target;
			blocks[0] = target.getEyeLocation().getBlock();
			blocks[1] = blocks[0].getRelative(BlockFace.DOWN);
			if (ServerVersion.getVersion() >= 10) target.setInvulnerable(true);
			for (int i = 0; i < 2; i++) {
				snapshots[i] = Blocks.createSnapshot(blocks[i]);
				blocks[i].setType(Material.ICE);
			}
			this.teleport = blocks[1].getLocation().clone().add(0.5, 0, 0.5).setDirection(target.getLocation().getDirection());
		}

		@EventHandler
		public void onBlockBreak(BlockBreakEvent e) {
			if (e.getBlock().equals(blocks[0]) || e.getBlock().equals(blocks[1])) e.setCancelled(true);
		}

		@EventHandler
		public void onExplode(BlockExplodeEvent e) {
			e.blockList().removeIf(block -> block.equals(blocks[0]) || block.equals(blocks[1]));
		}

		@EventHandler
		public void onExplode(EntityExplodeEvent e) {
			e.blockList().removeIf(block -> block.equals(blocks[0]) || block.equals(blocks[1]));
		}

		@EventHandler
		private void onEntityDamage(EntityDamageEvent e) {
			if (e.getEntity().equals(target)) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
			onEntityDamage(e);
		}

		@EventHandler
		private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
			onEntityDamage(e);
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@Override
		protected void run(int count) {
			target.teleport(teleport);
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
			if (ServerVersion.getVersion() >= 10) target.setInvulnerable(false);
			for (int i = 0; i < 2; i++) {
				snapshots[i].apply();
			}
			frozenEntities.remove(target);
		}

	}

}
