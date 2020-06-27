package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks.Behavior;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

@AbilityManifest(name = "죽음의 손아귀", rank = Rank.A, species = Species.OTHERS, explain = {

})
public class DeathGrasp extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = synergySettings.new SettingObject<Integer>(DeathGrasp.class, "Cooldown", 120, "# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> DamageConfig = synergySettings.new SettingObject<Integer>(DeathGrasp.class, "Damage", 30, "# 대미지") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final Timer fallBlockTimer = new Timer(5) {

		Location center;

		@Override
		public void onStart() {
			this.center = getPlayer().getLocation();
		}

		@SuppressWarnings("deprecation")
		@Override
		public void run(int count) {
			int distance = 6 - count;

			if (ServerVersion.getVersionNumber() >= 13) {
				for (Block block : LocationUtil.getBlocks2D(center, distance, true, true, false)) {
					if (block.getType() == Material.AIR) block = block.getRelative(BlockFace.DOWN);
					if (block.getType() == Material.AIR) continue;
					Location location = block.getLocation().add(0, 1, 0);
					FallingBlocks.spawnFallingBlock(location, block.getType(), false, getPlayer().getLocation().toVector().subtract(location.toVector()).multiply(-0.1).setY(Math.random()), Behavior.FALSE);
				}
			} else {
				for (Block block : LocationUtil.getBlocks2D(center, distance, true, true, false)) {
					if (block.getType() == Material.AIR) block = block.getRelative(BlockFace.DOWN);
					if (block.getType() == Material.AIR) continue;
					Location location = block.getLocation().add(0, 1, 0);
					FallingBlocks.spawnFallingBlock(location, block.getType(), block.getData(), false, getPlayer().getLocation().toVector().subtract(location.toVector()).multiply(-0.1).setY(Math.random()), Behavior.FALSE);
				}
			}

			for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(center, 5, 5)) {
				if (!damageable.equals(getPlayer())) {
					damageable.setVelocity(center.toVector().subtract(damageable.getLocation().toVector()).multiply(-1).setY(1.2));
				}
			}
		}

	}.setPeriod(TimeUnit.TICKS, 4);
	private final List<Runnable> SOUND_RUNNABLES = Arrays.asList(
			() -> SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.G)),
			() -> {
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.G));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(0, Note.Tone.B));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.D));
			},
			() -> {
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.G));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(0, Note.Tone.B));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.C));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(0, Note.Tone.E));
			},
			() -> {
				SoundLib.PIANO.playInstrument(getPlayer(), Note.sharp(0, Note.Tone.F));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.A));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.C));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(0, Note.Tone.E));
			},
			() -> {
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.G));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(0, Note.Tone.B));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.D));
			},
			() -> {
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.G));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(0, Note.Tone.B));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.D));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Note.Tone.G));
			},
			() -> {
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.G));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(0, Note.Tone.B));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.sharp(0, Note.Tone.D));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Note.Tone.G));
			},
			() -> {
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.A));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Note.Tone.D));
				SoundLib.PIANO.playInstrument(getPlayer(), Note.sharp(1, Note.Tone.F));
			}
	);
	private boolean noFallDamage = false;
	private boolean skillEnabled = false;
	private Player lastVictim = null;
	private final Timer follow = new Timer(60) {

		private Player target;

		@Override
		public void onStart() {
			this.target = lastVictim;
		}

		@Override
		protected void run(int count) {
			getPlayer().setVelocity(target.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).normalize().multiply(3.5));
		}
	}.setPeriod(TimeUnit.TICKS, 4);
	private final Timer Skill = new Timer(4) {

		private Player target;

		@Override
		public void onStart() {
			this.target = lastVictim;
			noFallDamage = true;
			getPlayer().setVelocity(getPlayer().getVelocity().add(new Vector(0, 4, 0)));
		}

		@Override
		public void run(int count) {
		}

		@Override
		public void onEnd() {
			skillEnabled = true;
			follow.start();
		}

	}.setPeriod(TimeUnit.TICKS, 10);
	private int stack = 0;

	public DeathGrasp(Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!cooldownTimer.isCooldown()) {
					if (lastVictim != null) {
						for (Player player : LocationUtil.getNearbyPlayers(getPlayer(), 5, 5)) {
							SoundLib.ENTITY_WITHER_SPAWN.playSound(player);
						}
						SoundLib.ENTITY_WITHER_SPAWN.playSound(getPlayer());
						Skill.start();
						cooldownTimer.start();
						return true;
					} else {
						getPlayer().sendMessage("§4마지막으로 때렸던 플레이어가 존재하지 않습니다.");
					}
				}
			}
		}

		return false;
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getEntity().equals(getPlayer())) {
				if (noFallDamage) {
					if (e.getCause().equals(DamageCause.FALL)) {
						e.setCancelled(true);
						noFallDamage = false;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().equals(getPlayer())) {
			if (skillEnabled) {
				Block b = getPlayer().getLocation().getBlock();
				Block db = getPlayer().getLocation().subtract(0, 1, 0).getBlock();

				if (!b.getType().equals(Material.AIR) || !db.getType().equals(Material.AIR)) {
					skillEnabled = false;
					final double damage = DamageConfig.getValue();
					for (Damageable d : LocationUtil.getNearbyEntities(Damageable.class, getPlayer(), 5, 5)) {
						if (d instanceof Player) SoundLib.ENTITY_GENERIC_EXPLODE.playSound((Player) d);
						d.damage(damage, getPlayer());
					}
					SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer());
					follow.stop(false);
					fallBlockTimer.start();
				}
			}
		}
	}

	@SubscribeEvent
	private void onAttack(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if ((damager.equals(getPlayer()) || (damager instanceof Projectile && getPlayer().equals(((Projectile) damager).getShooter()))) && e.getEntity() instanceof Player && !getPlayer().equals(e.getEntity())) {
			Player victim = (Player) e.getEntity();
			if (getGame().isParticipating(victim)) {
				if (victim.equals(lastVictim)) {
					stack++;
				} else {
					lastVictim = victim;
					stack = 1;
				}

				double ceil = Math.ceil(stack / 3.0);
				int soundNumber = (int) (ceil - ((Math.ceil(ceil / SOUND_RUNNABLES.size()) - 1) * SOUND_RUNNABLES.size())) - 1;
				SOUND_RUNNABLES.get(soundNumber).run();
				cooldownTimer.setCount(Math.max(0, cooldownTimer.getCount() - stack));
				PotionEffects.BLINDNESS.addPotionEffect(victim, 20, 0, true);
				e.setDamage(e.getDamage() + (stack * .1));
			}
		}
	}

}
