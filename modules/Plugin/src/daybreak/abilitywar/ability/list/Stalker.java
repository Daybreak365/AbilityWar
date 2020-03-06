package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

@AbilityManifest(Name = "스토커", Rank = Rank.A, Species = Species.HUMAN)
public class Stalker extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Stalker.class, "Cooldown", 210,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Stalker(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 다른 플레이어가 타게팅할 수 없는 상태로 변하여"),
				ChatColor.translateAlternateColorCodes('&', "&f마지막으로 때렸던 플레이어에게 돌진합니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f동일한 플레이어를 연속적으로 공격할 때마다 스택이 쌓이며 실명 효과를 주고,"),
				ChatColor.translateAlternateColorCodes('&', "&f다른 플레이어를 공격하면 스택이 초기화됩니다."),
				ChatColor.translateAlternateColorCodes('&', "&f플레이어를 공격할 때마다 쿨타임이 스택 만큼 감소하며, 1 스택 당"),
				ChatColor.translateAlternateColorCodes('&', "&f0.1의 추가 대미지를 냅니다."));
	}

	private static final RGB BLACK = new RGB(0, 0, 0);
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final Timer skill = new Timer() {
		GameMode originalMode;
		Player p;
		Player target;

		@Override
		protected void onStart() {
			p = getPlayer();
			target = lastVictim;
			for (int i = 0; i < 30; i++) {
				ParticleLib.SPELL_MOB.spawnParticle(p.getLocation().add(Vector.getRandom()), BLACK);
			}
			originalMode = p.getGameMode();
			getParticipant().attributes().TARGETABLE.setValue(false);
			p.setGameMode(GameMode.SPECTATOR);
			SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(p);
		}

		@Override
		protected void run(int count) {
			Location targetLocation = target.getLocation();
			Location playerLocation = p.getLocation();
			for (int i = 0; i < 10; i++) {
				ParticleLib.SPELL_MOB.spawnParticle(playerLocation.clone().add(Vector.getRandom()), BLACK);
			}
			p.setVelocity(targetLocation.toVector().subtract(playerLocation.toVector()).multiply(0.7));
			if (playerLocation.distanceSquared(targetLocation) < 1.0) {
				stop(false);
				p.teleport(target);
			}
		}

		@Override
		protected void onEnd() {
			p.setGameMode(originalMode);
			p.setVelocity(new Vector());
			for (int i = 0; i < 30; i++) {
				ParticleLib.SPELL_MOB.spawnParticle(p.getLocation().add(Vector.getRandom()), BLACK);
			}
			getParticipant().attributes().TARGETABLE.setValue(true);
		}

		@Override
		protected void onSilentEnd() {
			p.setGameMode(originalMode);
			p.setVelocity(new Vector());
			getParticipant().attributes().TARGETABLE.setValue(true);
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !cooldownTimer.isCooldown()) {
			if (lastVictim != null) {
				cooldownTimer.start();
				skill.start();
				return true;
			} else {
				getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4마지막으로 때렸던 플레이어가 존재하지 않습니다."));
			}
		}
		return false;
	}

	private Player lastVictim = null;
	private int stack = 0;

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

	@SubscribeEvent(onlyRelevant = true)
	private void onGameModeChange(PlayerGameModeChangeEvent e) {
		if (skill.isRunning() && getPlayer().getGameMode() == GameMode.SPECTATOR) e.setCancelled(true);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerTeleport(PlayerTeleportEvent e) {
		if (skill.isRunning() && getPlayer().getGameMode() == GameMode.SPECTATOR) e.setCancelled(true);
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
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
