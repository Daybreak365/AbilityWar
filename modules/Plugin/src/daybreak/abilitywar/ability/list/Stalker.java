package daybreak.abilitywar.ability.list;

import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.event.participant.ParticipantDeathEvent;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@AbilityManifest(name = "스토커", rank = Rank.A, species = Species.HUMAN, explain = {
		"§7패시브 §8- §c오직 너만을§f: 같은 플레이어를 계속 공격하면 스택이 쌓이며 §8실명 §f효과를",
		"§f줍니다. 스택이 쌓일 때마다 다른 모든 스킬의 쿨타임이 스택만큼 감소하며, 대상",
		"§f플레이어에게 주는 추가 대미지가 0.15씩 증가합니다.",
		"§7철괴 우클릭 §8- §c오직 나만이§f: 순간 벽을 통과할 수 있고 타게팅되지 않는 상태로",
		"§f변하여 마지막으로 타격한 플레이어에게 빠르게 돌진합니다. $[COOLDOWN_CONFIG]",
		"§7검 우클릭 §8- §c간 보기§f: 마지막으로 타격한 플레이어에게 작게",
		"§f돌진합니다. $[LEFT_COOLDOWN_CONFIG]"
})
public class Stalker extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> LEFT_COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Stalker.class, "left-skill-cooldown", 4,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Stalker.class, "cooldown", 210,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	private static final Set<Material> swords;

	static {
		if (MaterialX.NETHERITE_SWORD.isSupported()) {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD, MaterialX.NETHERITE_SWORD.getMaterial());
		} else {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD);
		}
	}

	public Stalker(Participant participant) {
		super(participant);
	}

	private static final RGB BLACK = new RGB(0, 0, 0);
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Cooldown rightCooldownTimer = new Cooldown(LEFT_COOLDOWN_CONFIG.getValue(), "돌진", 0);
	private final AbilityTimer skill = new AbilityTimer() {
		private GameMode originalMode;
		private Player target;
		private float flySpeed;

		@Override
		protected void onStart() {
			this.target = lastVictim;
			for (int i = 0; i < 30; i++) {
				ParticleLib.SPELL_MOB.spawnParticle(getPlayer().getLocation().add(Vector.getRandom()), BLACK);
			}
			this.originalMode = getPlayer().getGameMode();
			if (originalMode == GameMode.SPECTATOR) originalMode = GameMode.SURVIVAL;
			this.flySpeed = getPlayer().getFlySpeed();
			getParticipant().attributes().TARGETABLE.setValue(false);
			getPlayer().setGameMode(GameMode.SPECTATOR);
			SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(getPlayer());
		}

		@Override
		protected void run(int count) {
			if (getPlayer().getGameMode() == GameMode.SPECTATOR) {
				getPlayer().setSpectatorTarget(null);
			}
			getPlayer().setFlySpeed(0f);
			Location targetLocation = target.getLocation();
			Location playerLocation = getPlayer().getLocation();
			for (int i = 0; i < 10; i++) {
				ParticleLib.SPELL_MOB.spawnParticle(playerLocation.clone().add(Vector.getRandom()), BLACK);
			}
			getPlayer().setVelocity(targetLocation.toVector().subtract(playerLocation.toVector()).multiply(0.7));
			if (playerLocation.distanceSquared(targetLocation) < 4.0) {
				stop(false);
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
			getPlayer().teleport(target);
			for (int i = 0; i < 30; i++) {
				ParticleLib.SPELL_MOB.spawnParticle(getPlayer().getLocation().add(Vector.getRandom()), BLACK);
			}
		}

		@Override
		protected void onSilentEnd() {
			getPlayer().setGameMode(originalMode);
			getPlayer().setVelocity(new Vector());
			getPlayer().setFlySpeed(flySpeed);
			getParticipant().attributes().TARGETABLE.setValue(true);
		}
	}.setPeriod(TimeUnit.TICKS, 1).register();

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
				if (cooldownTimer.isCooldown()) return false;
				if (lastVictim != null) {
					cooldownTimer.start();
					skill.start();
					return true;
				} else {
					getPlayer().sendMessage("§4마지막으로 때렸던 플레이어가 존재하지 않습니다.");
				}
			}
		}
		return false;
	}

	private Player lastVictim = null;
	private int stack = 0;

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerInteract(final PlayerInteractEvent e) {
		if ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) && e.getItem() != null && swords.contains(e.getItem().getType())) {
			if (rightCooldownTimer.isCooldown()) return;
			if (lastVictim != null) {
				rightCooldownTimer.start();
				getPlayer().setVelocity(lastVictim.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).normalize().multiply(0.75).setY(0));
			} else {
				getPlayer().sendMessage("§4마지막으로 때렸던 플레이어가 존재하지 않습니다.");
			}
		}
	}

	@SubscribeEvent
	private void onParticipantDeath(ParticipantDeathEvent e) {
		if (e.getPlayer().equals(lastVictim)) {
			lastVictim = null;
			stack = 0;
			if (skill.isRunning()) skill.stop(false);
		}
	}

	@SubscribeEvent
	private void onAttack(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if ((damager.equals(getPlayer()) || (damager instanceof Arrow && getPlayer().equals(((Arrow) damager).getShooter()))) && e.getEntity() instanceof Player && !getPlayer().equals(e.getEntity())) {
			Player victim = (Player) e.getEntity();
			if (getGame().isParticipating(victim)) {
				if (victim.equals(lastVictim)) {
					stack++;
				} else {
					lastVictim = victim;
					stack = 1;
				}

				final double ceil = Math.ceil(stack / 3.0);
				SOUND_RUNNABLES.get((int) (ceil - ((Math.ceil(ceil / SOUND_RUNNABLES.size()) - 1) * SOUND_RUNNABLES.size())) - 1).run();
				if (cooldownTimer.isRunning()) cooldownTimer.setCount(Math.max(0, cooldownTimer.getCount() - stack));
				PotionEffects.BLINDNESS.addPotionEffect(victim, 30, 0, true);
				e.setDamage(e.getDamage() + (stack * .15));
			}
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onGameModeChange(PlayerGameModeChangeEvent e) {
		if (skill.isRunning() && getPlayer().getGameMode() == GameMode.SPECTATOR) e.setCancelled(true);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerTeleport(PlayerTeleportEvent e) {
		if (skill.isRunning() && getPlayer().getGameMode() == GameMode.SPECTATOR) e.setCancelled(true);
	}

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

}
