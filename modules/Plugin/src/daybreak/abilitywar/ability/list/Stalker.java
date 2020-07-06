package daybreak.abilitywar.ability.list;

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
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.Arrays;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

@AbilityManifest(name = "스토커", rank = Rank.A, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 다른 플레이어가 타게팅할 수 없고 벽을 통과할 수 있는",
		"상태로 변하여 마지막으로 타격했던 플레이어에게 돌진합니다. $[COOLDOWN_CONFIG]",
		"동일한 플레이어를 계속 공격하면 §c스택§f이 쌓이며 §8실명 §f효과를 주고,",
		"다른 플레이어를 공격하면 §c스택§f이 초기화됩니다. 플레이어를 공격할 때마다",
		"스킬 쿨타임이 §c스택 §f만큼 감소하며, §c1 스택 §f당 §60.15§f의 추가 대미지를 냅니다.",
		"철괴를 좌클릭하면 마지막으로 타격한 대상의 위치로 조금 돌진합니다. $[LEFT_COOLDOWN_CONFIG]"
})
public class Stalker extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> LEFT_COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Stalker.class, "LEFT_SKILL_COOLDOWN", 4,
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

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Stalker.class, "COOLDOWN", 210,
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

	public Stalker(Participant participant) {
		super(participant);
	}

	private static final RGB BLACK = new RGB(0, 0, 0);
	private final CooldownTimer cooldownTimer = new CooldownTimer(COOLDOWN_CONFIG.getValue());
	private final CooldownTimer leftCooldownTimer = new CooldownTimer(LEFT_COOLDOWN_CONFIG.getValue(), "돌진");
	private final Timer skill = new Timer() {
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
	}.setPeriod(TimeUnit.TICKS, 1);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
				if (cooldownTimer.isCooldown()) return false;
				if (lastVictim != null) {
					cooldownTimer.start();
					skill.start();
					return true;
				} else {
					getPlayer().sendMessage("§4마지막으로 때렸던 플레이어가 존재하지 않습니다.");
				}
			} else if (clickType == ClickType.LEFT_CLICK) {
				if (leftCooldownTimer.isCooldown()) return false;
				if (lastVictim != null) {
					leftCooldownTimer.start();
					getPlayer().setVelocity(lastVictim.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).normalize().setY(0));
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

				double ceil = Math.ceil(stack / 3.0);
				int soundNumber = (int) (ceil - ((Math.ceil(ceil / SOUND_RUNNABLES.size()) - 1) * SOUND_RUNNABLES.size())) - 1;
				SOUND_RUNNABLES.get(soundNumber).run();
				if (cooldownTimer.isRunning()) cooldownTimer.setCount(Math.max(0, cooldownTimer.getCount() - stack));
				if (leftCooldownTimer.isRunning())
					leftCooldownTimer.setCount(Math.max(0, leftCooldownTimer.getCount() - stack));
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
