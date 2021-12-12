package daybreak.abilitywar.ability.list.clown;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.manager.effect.Fear;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@AbilityManifest(name = "광대", rank = Rank.A, species = Species.HUMAN, explain = {
		"§7철괴 우클릭 §8- §5환상§f: 바라보는 방향으로 순간이동하고 3초간 §b은신§f합니다. 이후 4초",
		" 이내에 능력을 재사용하면 순간이동 전의 위치로 복귀하고 주위 다섯 칸 이내의",
		" 모든 플레이어를 3초 동안 공포에 빠뜨리고 3초간 §b은신§f합니다. $[COOLDOWN_CONFIG]",
		"§7패시브 §8- §5암습§f: 플레이어를 후방에서 타격하면 1.5초간 공포에 빠뜨리고 추가",
		" 대미지를 줍니다. 유닛별 쿨타임§8(§710초§8)§f이 적용됩니다.",
		"§7특수 효과 §8- §b은신§f: 갑옷 및 손에 들고 있는 아이템이 보이지 않으며, 몸이",
		" 투명해집니다."
})
public abstract class AbstractClown extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(AbstractClown.class, "cooldown", 35,
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

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
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

	private static final Note[] notes = {
			Note.natural(0, Tone.G),
			Note.sharp(0, Tone.A),
			Note.natural(0, Tone.B),
			Note.natural(0, Tone.D),
			Note.sharp(1, Tone.F),
			Note.natural(1, Tone.G),
			Note.sharp(1, Tone.A)
	};

	protected AbstractClown(Participant participant) {
		super(participant);
	}

	protected abstract void hide0();

	protected abstract void show0();

	private void hide() {
		if (hiding) return;
		this.hiding = true;
		hide0();
	}

	private void show() {
		if (!hiding) return;
		this.hiding = false;
		show0();
	}

	private boolean hiding = false;

	public boolean isHiding() {
		return hiding;
	}

	private Teleport teleport = null;

	@Nullable
	private static Entity getDamager(final Entity damager) {
		if (damager instanceof Projectile) {
			final ProjectileSource shooter = ((Projectile) damager).getShooter();
			return shooter instanceof Entity ? (Entity) shooter : null;
		} else return damager;
	}

	private final Map<UUID, Long> lastFear = new HashMap<>();
	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue(), CooldownDecrease._50);

	private final int unitCooldown = (int) (10000 * Wreck.calculateDecreasedAmount(25));

	@SubscribeEvent
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		if (getPlayer().equals(getDamager(e.getDamager())) && predicate.test(e.getEntity())) {
			Participant entity = getGame().getParticipant(e.getEntity().getUniqueId());
			if (LocationUtil.isBehind(e.getEntity(), getPlayer())) {
				final long current = System.currentTimeMillis();
				if (current - lastFear.getOrDefault(entity.getPlayer().getUniqueId(), 0L) >= unitCooldown) {
					lastFear.put(entity.getPlayer().getUniqueId(), current);
					Fear.apply(entity, TimeUnit.TICKS, 30, getPlayer());
					e.setDamage(e.getDamage() * 1.35);
					for (Note note : notes) {
						SoundLib.CHIME.playInstrument(entity.getPlayer(), note);
						SoundLib.CHIME.playInstrument(getPlayer(), note);
					}
				}
			}
		}
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
			if (teleport == null && !cooldown.isCooldown()) {
				Block lastEmpty = null;
				try {
					for (BlockIterator iterator = new BlockIterator(getPlayer().getWorld(), getPlayer().getLocation().toVector(), getPlayer().getLocation().getDirection(), 1, 10); iterator.hasNext(); ) {
						final Block block = iterator.next();
						if (!block.getType().isSolid()) {
							lastEmpty = block;
						}
					}
				} catch (IllegalStateException ignored) {
				}
				if (lastEmpty != null) {
					this.teleport = new Teleport(lastEmpty.getLocation().setDirection(getPlayer().getLocation().getDirection()));
					return true;
				} else {
					getPlayer().sendMessage(ChatColor.RED + "바라보는 방향에 이동할 수 있는 곳이 없습니다.");
				}
			} else {
				teleport.returnToStart();
			}
		}
		return false;
	}

	private enum Phase {
		HIDE(3, false, "§7은신") {
			@Override
			public void onStart(AbstractClown clown, Teleport teleport) {
				ParticleLib.CLOUD.spawnParticle(clown.getPlayer().getLocation(), .5, .5, .5, 20, 0);
				clown.new AbilityTimer(TaskType.NORMAL, 5) {
					@Override
					protected void run(int count) {
						SoundLib.ENTITY_WITCH_AMBIENT.playSound(clown.getPlayer().getLocation(), .45f, 2);
					}
				}.setPeriod(TimeUnit.TICKS, 1).start();
				clown.hide();
				clown.getPlayer().teleport(teleport.dest);
				clown.getPlayer().setFallDistance(0);
			}

			@Override
			public void onEnd(AbstractClown clown, Teleport teleport) {
				clown.show();
				teleport.phase = Phase.IDLE;
				teleport.start();
			}
		}, IDLE(4, true, "§b귀환 가능") {
			@Override
			public void onStart(AbstractClown clown, Teleport teleport) {
			}

			@Override
			public void onEnd(AbstractClown clown, Teleport teleport) {
				clown.cooldown.start();
				clown.teleport = null;
				teleport.actionbarChannel.unregister();
			}
		}, AFTER_RETURN(3, false, "§7은신") {
			@Override
			public void onStart(AbstractClown clown, Teleport teleport) {
				ParticleLib.CLOUD.spawnParticle(clown.getPlayer().getLocation(), .5, .5, .5, 20, 0);
				clown.hide();
				clown.getPlayer().teleport(teleport.start);
				SoundLib.ENTITY_WITHER_SPAWN.playSound(teleport.start, .45f, 2);
				for (Player player : LocationUtil.getEntitiesInCircle(Player.class, teleport.start, 5, clown.predicate)) {
					PotionEffects.BLINDNESS.addPotionEffect(player, 80, 0, true);
					Fear.apply(clown.getGame().getParticipant(player), TimeUnit.SECONDS, 3, clown.getPlayer());
				}
			}

			@Override
			public void onEnd(AbstractClown clown, Teleport teleport) {
				clown.show();
				clown.cooldown.start();
				clown.teleport = null;
				teleport.actionbarChannel.unregister();
			}
		};

		private final int count;
		private final boolean canReturn;
		private final String state;

		Phase(int count, boolean canReturn, String state) {
			this.count = count;
			this.canReturn = canReturn;
			this.state = state;
		}

		public int getCount() {
			return count;
		}

		public boolean canReturn() {
			return canReturn;
		}

		public abstract void onStart(AbstractClown clown, Teleport teleport);

		public abstract void onEnd(AbstractClown clown, Teleport teleport);
	}

	private class Teleport extends AbilityTimer {

		private Phase phase = Phase.HIDE;
		private final Location start, dest;
		private final ActionbarChannel actionbarChannel = getParticipant().actionbar().newChannel();

		private Teleport(final Location dest) {
			super(TaskType.REVERSE, 1);
			this.start = getPlayer().getLocation();
			this.dest = dest;
			AbstractClown.this.teleport = this;
			start();
		}

		@Override
		protected void onStart() {
			setCount(phase.getCount());
			phase.onStart(AbstractClown.this, this);
		}

		@Override
		protected void run(int count) {
			actionbarChannel.update(phase.state + " §0| §f" + count + "초");
		}

		protected void returnToStart() {
			if (phase.canReturn()) {
				stop(true);
				AbstractClown.this.teleport = this;
				this.phase = Phase.AFTER_RETURN;
				start();
			}
		}

		@Override
		protected void onEnd() {
			phase.onEnd(AbstractClown.this, this);
		}

		@Override
		protected void onSilentEnd() {
			show();
			AbstractClown.this.teleport = null;
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_SET || update == Update.ABILITY_DESTROY) {
			show();
		}
	}
}
