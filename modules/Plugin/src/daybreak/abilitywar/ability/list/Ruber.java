package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Bleed;
import daybreak.abilitywar.game.manager.effect.Bleed.ParticipantBleedEvent;
import daybreak.abilitywar.game.manager.effect.Hemophilia;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.entity.health.Healths;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@AbilityManifest(name = "루베르", rank = Rank.S, species = Species.HUMAN, explain = {
		"§7스택 §8- §c피의 잔§f: 스킬 적중 대상에게 쌓이며, 영구히 지속됩니다. 두 개 까지 쌓을",
		" 수 있고, 스택이 모두 쌓인 대상에게 스킬을 사용하면 강화 스킬이 시전되고 스택이",
		" 초기화됩니다.",
		"§7철괴 우클릭 §8- §c흡혈§f: 상대를 바라보고 이 능력을 사용하면 체력을 반 칸",
		" 흡혈합니다. §4(§c강화§4) §f체력을 한 칸 흡혈하고 대상을 3초간 출혈시킵니다.",
		" $[COOLDOWN_CONFIG]",
		"§7철괴 좌클릭 §8- §c전염병 창궐§f: 주변 10칸 이내의 모든 플레이어를 4초간 §4혈사병§f에",
		" 감염시킵니다. §4(§c강화§4) §f6초간 §4혈사병§f에 감염시키고 대상을 출혈시킵니다.",
		" $[PLAGUE_COOLDOWN_CONFIG]",
		"§7상태 이상 §8- §4혈사병§f: 혈사병 지속 중 플레이어가 가지고 있는 모든 출혈 효과가",
		" 멈추지 않습니다. 혈사병이 종료되면 모든 출혈 효과가 함께 사라집니다."
}, summarize = {
		"대상에게 쓰는 세 번째마다의 스킬 효과가 강화됩니다.",
		"§7적을 바라보고 철괴를 우클릭§f하면 대상에게서 체력을 반 칸 §c흡혈§f합니다.",
		"§4(§c강화§4)§f 한 칸 §c흡혈§f하며 3초간 §c출혈§f시킵니다.",
		"§7철괴 좌클릭§f 시 주변 적들을 4초간 §c출혈§f이 멎지 않는 §4혈사병§f을 감염시킵니다.",
		"§4(§c강화§4)§f 6초간 §4혈사병§f을 감염시키고 §c출혈§f도 부여합니다."
})
public class Ruber extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Ruber.class, "cooldown", 8,
			"# 흡혈 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};

	public static final SettingObject<Integer> PLAGUE_COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Ruber.class, "plague-cooldown", 20,
			"# 전염병 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};

	public static final SettingObject<Integer> PLAGUE_RADIUS_CONFIG = abilitySettings.new SettingObject<Integer>(Ruber.class, "plague-radius", 8,
			"# 전염병 범위") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};

	private static final RGB DARK_RED = RGB.of(143, 15, 6);
	private static final Note[] notes = new Note[] {
			Note.natural(0, Tone.B), Note.sharp(0, Tone.D), Note.natural(0, Tone.G),
			Note.sharp(0, Tone.G), Note.natural(0, Tone.B)
	};

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (!(entity instanceof Player)) return false;
			if (!getGame().isParticipating(entity.getUniqueId())
					|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
				return false;
			}
			if (getGame() instanceof Teamable) {
				final Teamable teamGame = (Teamable) getGame();
				final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
				return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
			}
			return true;
		}
	};

	private final Map<UUID, Stack> stacks = new HashMap<>();
	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue(), "흡혈", 0), plagueCooldown = new Cooldown(PLAGUE_COOLDOWN_CONFIG.getValue(), "전염병 창궐", 25);
	private final int plagueRadius = PLAGUE_RADIUS_CONFIG.getValue();
	private final Circle circle = Circle.of(plagueRadius, plagueRadius * 15);

	public Ruber(Participant participant) {
		super(participant);
	}

	@SubscribeEvent
	private void onParticipantBleed(final ParticipantBleedEvent e) {
		if (getParticipant().equals(e.getParticipant())) return;
		final Location playerLocation = getPlayer().getLocation(), targetLocation = e.getPlayer().getLocation();
		if (playerLocation.getWorld() != targetLocation.getWorld() || playerLocation.distanceSquared(targetLocation) > 169)
			return;
		new Transfusion(e.getParticipant(), false, e.getAmount() / 2, false).start();
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
				if (cooldown.isCooldown()) return false;
				final Player target = LocationUtil.getEntityLookingAt(Player.class, getPlayer(), 15, predicate);
				if (target != null) {
					if (Damages.canDamage(target, getPlayer(), DamageCause.MAGIC, 1)) {
						final Participant participant = getGame().getParticipant(target);
						new Transfusion(participant, true, 1, addStack(participant)).start();
						cooldown.start();
					}
				}
			} else if (clickType == ClickType.LEFT_CLICK) {
				if (plagueCooldown.isCooldown()) return false;
				final List<Player> players = LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), plagueRadius, plagueRadius, predicate);
				if (players.isEmpty()) {
					getPlayer().sendMessage("§c" + plagueRadius + "칸 이내에 대상이 존재하지 않습니다.");
					return false;
				}
				for (Player target : players) {
					final Participant participant = getGame().getParticipant(target);
					if (addStack(participant)) {
						Bleed.apply(participant, TimeUnit.SECONDS, 3, 5);
						Hemophilia.apply(participant, TimeUnit.SECONDS, 6);
					} else {
						Hemophilia.apply(participant, TimeUnit.SECONDS, 4);
					}
					SoundLib.ENTITY_EVOKER_PREPARE_ATTACK.playSound(target);
				}
				SoundLib.ENTITY_EVOKER_PREPARE_ATTACK.playSound(getPlayer());
				for (Location location : circle.toLocations(getPlayer().getLocation()).floor(getPlayer().getLocation().getY())) {
					ParticleLib.REDSTONE.spawnParticle(location, DARK_RED);
				}
				plagueCooldown.start();
			}
		}
		return false;
	}

	public boolean addStack(final Participant participant) {
		final Stack stack = stacks.get(participant.getPlayer().getUniqueId());
		if (stack != null) {
			return stack.addStack();
		} else {
			final Stack newStack = new Stack(participant);
			newStack.start();
			return newStack.addStack();
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerJoin(PlayerJoinEvent e) {
		for (Stack stack : stacks.values()) {
			stack.hologram.display(getPlayer());
		}
	}

	@SubscribeEvent
	private void onDeath(EntityDeathEvent e) {
		final Stack stack = stacks.get(e.getEntity().getUniqueId());
		if (stack != null) stack.stop(true);
	}

	@SubscribeEvent
	private void onDeath(PlayerDeathEvent e) {
		final Stack stack = stacks.get(e.getEntity().getUniqueId());
		if (stack != null) stack.stop(true);
	}

	private static final String[] characters = {
		"§f", "§7✗", "§4§l✗"
	};

	private static String getCharacter(final int stack) {
		if (stack < 0 || stack >= characters.length) return characters[0];
		return characters[stack];
	}

	private class Stack extends AbilityTimer {

		private final Player entity;
		private final IHologram hologram;
		private int stack = 0;

		private Stack(Participant participant) {
			super();
			setPeriod(TimeUnit.TICKS, 4);
			this.entity = participant.getPlayer();
			this.hologram = NMS.newHologram(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), getCharacter(stack));
			hologram.display(getPlayer());
			stacks.put(entity.getUniqueId(), this);
		}

		@Override
		protected void run(int count) {
			hologram.teleport(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), entity.getLocation().getYaw(), 0);
		}

		private boolean addStack() {
			stack++;
			hologram.setText(getCharacter(stack));
			ParticleLib.BLOCK_CRACK.spawnParticle(entity.getEyeLocation(), .3f, .3f, .3f, 10, MaterialX.REDSTONE_BLOCK);
			if (stack >= 3) {
				stop(false);
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			hologram.unregister();
			stacks.remove(entity.getUniqueId());
		}
	}

	private class Transfusion extends AbilityTimer {

		private final Participant participant;
		private final Player target;
		private final boolean damage;
		private final double amount;
		private final Location currentLocation;
		private final boolean strengthen;

		private Transfusion(final Participant target, final boolean damage, final double amount, final boolean strengthen) {
			super();
			setPeriod(TimeUnit.TICKS, 1);
			this.participant = target;
			this.target = target.getPlayer();
			this.damage = damage;
			this.strengthen = strengthen;
			this.amount = strengthen ? amount * 2 : amount;
			this.currentLocation = this.target.getLocation().clone().add(0, 1, 0);
		}

		@Override
		protected void onStart() {
			if (!damage) return;
			Healths.setHealth(target, target.getHealth() - amount);
			NMS.broadcastEntityEffect(target, (byte) 2);
			SoundLib.ENTITY_PLAYER_HURT.playSound(target.getLocation());
			if (strengthen) {
				Bleed.apply(participant, TimeUnit.SECONDS, 3, 5);
			}
		}

		@Override
		protected void run(int count) {
			final Location playerLocation = getPlayer().getLocation().clone().add(0, 1, 0);
			if (playerLocation.getWorld() != currentLocation.getWorld()) {
				stop(true);
				return;
			}
			final Vector direction = playerLocation.toVector().subtract(currentLocation.toVector()).normalize().multiply(.1);
			for (int i = 0; i < 5; i++) {
				currentLocation.setX(currentLocation.getX() + direction.getX());
				currentLocation.setY(currentLocation.getY() + direction.getY());
				currentLocation.setZ(currentLocation.getZ() + direction.getZ());
				ParticleLib.REDSTONE.spawnParticle(currentLocation, strengthen ? DARK_RED : RGB.RED);
				if (playerLocation.distanceSquared(currentLocation) <= .04) {
					stop(false);
					return;
				}
			}
			if (count >= 300) {
				stop(true);
			}
		}

		@Override
		protected void onEnd() {
			if (getPlayer().isDead()) return;
			getPlayer().setHealth(Math.min(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), Math.max(1, getPlayer().getHealth() + amount)));
			if (damage) {
				if (strengthen) {
					SoundLib.ENTITY_PLAYER_BREATH.playSound(getPlayer(), 100f, .7757f);
					SoundLib.ENTITY_PLAYER_BREATH.playSound(getPlayer(), 100f, 2f);
					for (Note note : notes) {
						SoundLib.PIANO.playInstrument(getPlayer(), note);
					}
				} else {
					SoundLib.ENTITY_PLAYER_BREATH.playSound(getPlayer(), 100f, .7757f);
					SoundLib.ENTITY_PLAYER_BREATH.playSound(getPlayer(), 100f, 2f);
				}
			}
		}
	}

}
