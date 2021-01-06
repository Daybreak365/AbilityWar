package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.utils.base.ProgressBar;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

@AbilityManifest(name = "카니스", rank = Rank.L, species = Species.HUMAN, explain = {
		"§7철괴 우클릭 §8- §3말 탑승§f: 말을 소환하여 탑승합니다. 말에서 내린 후 말을 다시",
		" 소환하려면 35초간 기다려야 합니다.",
		"§7패시브 §8- §3말 기력§f: 쾌속 질주가 사용중이지 않을 때 말의 기력이 §63단계§f까지",
		" 서서히 차오릅니다.",
		"§7철괴 좌클릭 §8- §3쾌속 질주§f: 말이 8초간 기력을 모두 소모하여 빠른 속도로",
		" 질주하며, 말의 기력 단계가 높을수록 질주 속도가 빨라집니다.",
		"§7충전형 무기 §8- §3랜스§f: 말에 탑승한 상태에서 25초마다 랜스가 준비됩니다.",
		" 랜스가 준비된 상태에서 상대를 근접 공격할 경우 원래 대미지의 40% 위력을 내는",
		" 방어 관통 공격을 합니다. §3쾌속 질주 §f중 §3랜스§f로 공격하면 말의 기력 단계에 따라",
		" 공격 위력이 각각 50%, 75%, 100%로 증가합니다.",
		"§7패시브 §8- §3하나가 되어§f: 말에서 내리면 말의 현재 체력 일부를 §e흡수 체력§f으로",
		" 전환합니다. §e흡수 체력§f은 2초간 지속되며, 이후 급격히 사라집니다."
})
public class Canis extends AbilityBase implements ActiveHandler {

	private static final Random random = new Random();
	private static final ItemStack saddleItem = MaterialX.SADDLE.createItem(), armorItem = MaterialX.IRON_HORSE_ARMOR.createItem();
	private static final AttributeModifier slowModifier = new AttributeModifier("minusSpeed", -.35, Operation.ADD_SCALAR);

	private Horse horse;
	private final Style horseStyle = random.pick(Style.class);
	private final Color horseColor = random.pick(Color.class);

	public Canis(Participant participant) {
		super(participant);
	}

	private class Charge extends AbilityTimer {

		private final ActionbarChannel actionbarChannel = newActionbarChannel();
		private final ProgressBar progressBar;
		private final String name;
		private final BooleanSupplier state;

		private Charge(final String name, final int count, final BooleanSupplier state) {
			super(TaskType.NORMAL, (int) (count * Wreck.calculateDecreasedAmount(25)));
			setBehavior(RestrictionBehavior.PAUSE_RESUME);
			this.name = name;
			this.progressBar = new ProgressBar(getMaximumCount(), 10);
			this.state = state;
		}

		@Override
		protected void onStart() {
			progressBar.setStep(0);
		}

		@Override
		protected void run(int count) {
			if (state != null && !state.getAsBoolean()) {
				stop(true);
				return;
			}
			progressBar.setStep(count);
			actionbarChannel.update("§3" + name + "§f: " + progressBar.toString("|", ChatColor.GREEN, ChatColor.GRAY));
		}

		@Override
		protected void onCountSet() {
			progressBar.setStep(getCount());
			actionbarChannel.update("§3" + name + "§f: " + progressBar.toString("|", ChatColor.GREEN, ChatColor.GRAY));
		}

		@Override
		protected void onEnd() {
			actionbarChannel.update(null);
		}

		@Override
		protected void onSilentEnd() {
			actionbarChannel.update(null);
		}

	}

	private enum Tier {
		RELAXED(BarColor.GREEN, new AttributeModifier("add-horse-speed", .4, Operation.ADD_SCALAR), new Note[] {
			Note.flat(0, Tone.E)
		}, .5) {
			@Override
			Tier previousTier() {
				return null;
			}
			@Override
			Tier nextTier() {
				return POISED;
			}
		}, POISED(BarColor.YELLOW, new AttributeModifier("add-horse-speed", .65, Operation.ADD_SCALAR), new Note[] {
				Note.flat(0, Tone.B),
				Note.flat(0, Tone.E),
				Note.natural(1, Tone.G)
		}, .75) {
			@Override
			Tier previousTier() {
				return RELAXED;
			}
			@Override
			Tier nextTier() {
				return EXCITED;
			}
		}, EXCITED(BarColor.RED, new AttributeModifier("add-horse-speed", .95, Operation.ADD_SCALAR), new Note[] {
				Note.natural(0, Tone.G),
				Note.flat(0, Tone.B),
				Note.flat(0, Tone.E),
				Note.natural(1, Tone.G),
				Note.flat(1, Tone.B)
		}, 1) {
			@Override
			Tier previousTier() {
				return POISED;
			}
			@Override
			Tier nextTier() {
				return null;
			}
		};

		private final BarColor barColor;
		private final AttributeModifier attributeModifier;
		private final Note[] notes;
		private final double lanceMultiplier;

		Tier(final BarColor barColor, final AttributeModifier attributeModifier, final Note[] notes, final double lanceMultiplier) {
			this.barColor = barColor;
			this.attributeModifier = attributeModifier;
			this.notes = notes;
			this.lanceMultiplier = lanceMultiplier;
		}

		@Nullable
		abstract Tier previousTier();
		@Nullable
		abstract Tier nextTier();
	}

	private static final int maxCharge = 160;

	private class HighSpeedDash extends AbilityTimer implements Listener {

		private @NotNull Tier currentTier;
		private int charge = 0;
		private boolean dashing = false, charging = false;
		private final BossBar bossBar;

		private HighSpeedDash() {
			super();
			setBehavior(RestrictionBehavior.PAUSE_RESUME);
			this.currentTier = Tier.RELAXED;
			this.bossBar = Bukkit.createBossBar("말 기력", currentTier.barColor, BarStyle.SEGMENTED_10);
			setPeriod(TimeUnit.TICKS, 1);
		}

		@Override
		protected void onStart() {
			reset();
			bossBar.setProgress(0);
			bossBar.addPlayer(getPlayer());
			if (ServerVersion.getVersion() >= 10) bossBar.setVisible(true);
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@EventHandler
		private void onPlayerJoin(final PlayerJoinEvent e) {
			if (getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
				bossBar.addPlayer(e.getPlayer());
			}
		}

		@EventHandler
		private void onPlayerQuit(final PlayerQuitEvent e) {
			if (getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
				bossBar.removePlayer(e.getPlayer());
			}
		}

		@Override
		protected void run(int count) {
			if (!isMounted()) {
				stop(true);
				return;
			}
			if (!dashing) {
				if (!charging) return;
				final Tier nextTier = currentTier.nextTier();
				if (++charge >= maxCharge) {
					for (Note note : this.currentTier.notes) {
						SoundLib.PIANO.playInstrument(getPlayer(), note);
					}
					if (nextTier != null) {
						this.charge = 0;
						this.currentTier = nextTier;
						bossBar.setColor(currentTier.barColor);
					} else {
						this.charging = false;
					}
				}
			} else {
				if (--charge > 0) {
					final Horse horse = Canis.this.horse;
					if (horse == null) return;
					ParticleLib.CLOUD.spawnParticle(horse.getLocation(), .2, .2, .2, 2, .15);
					if (count % 10 == 0) {
						SoundLib.ENTITY_HORSE_GALLOP.playSound(getPlayer().getLocation());
					}
				} else {
					final Horse horse = Canis.this.horse;
					if (horse == null) return;
					horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(slowModifier);
					new AbilityTimer(0) {
						@Override
						protected void onEnd() {
							onSilentEnd();
						}
						@Override
						protected void onSilentEnd() {
							final Horse horse = Canis.this.horse;
							if (horse == null) return;
							horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(slowModifier);
						}
					}.setInitialDelay(TimeUnit.SECONDS, 2).start();
					for (Tier tier : Tier.values()) {
						horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(tier.attributeModifier);
					}
					reset();
				}
			}
			bossBar.setProgress(RangesKt.coerceIn(charge / (double) maxCharge, 0, 1));
		}

		public void reset() {
			this.charging = true;
			this.charge = 0;
			this.currentTier = Tier.RELAXED;
			bossBar.setColor(Tier.RELAXED.barColor);
			this.dashing = false;
		}

		public boolean isDashing() {
			return dashing;
		}

		@Nullable
		public Tier getTier() {
			if (charge == maxCharge) {
				return currentTier;
			} else {
				return currentTier.previousTier();
			}
		}

		public boolean startDash() {
			if (dashing) return false;
			final Tier tier = getTier();
			if (tier != null) {
				this.dashing = true;
				this.charge = maxCharge;
				this.currentTier = tier;
				bossBar.setColor(tier.barColor);
				horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(tier.attributeModifier);
				return true;
			} else return false;
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			bossBar.removeAll();
			HandlerList.unregisterAll(this);
		}

	}

	private class Absorption extends AbilityTimer implements Listener {

		private final float originalAbsorption;

		private Absorption(final double amount) {
			super();
			this.originalAbsorption = NMS.getAbsorptionHearts(getPlayer());
			NMS.setAbsorptionHearts(getPlayer(), (float) (originalAbsorption + amount));
			setPeriod(TimeUnit.TICKS, 1);
			setInitialDelay(TimeUnit.SECONDS, 2);
			start();
		}

		@EventHandler
		private void onPlayerQuit(final PlayerQuitEvent e) {
			if (getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
				stop(true);
			}
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@Override
		protected void run(int count) {
			final float current = NMS.getAbsorptionHearts(getPlayer());
			if (current > originalAbsorption) {
				NMS.setAbsorptionHearts(getPlayer(), Math.max(originalAbsorption, current - 2));
			} else {
				stop(false);
			}
		}

		@Override
		protected void onEnd() {
			HandlerList.unregisterAll(this);
		}

		@Override
		protected void onSilentEnd() {
			NMS.setAbsorptionHearts(getPlayer(), originalAbsorption);
			HandlerList.unregisterAll(this);
		}
	}

	private final Charge horseCharge = new Charge("말", 35, null), lanceCharge = new Charge("랜스", 25, this::isMounted);
	private final HighSpeedDash highSpeedDash = new HighSpeedDash();

	public boolean mount() {
		if (isMounted()) {
			getPlayer().sendMessage("§c이미 말에 타고있습니다.");
			return false;
		}
		if (horseCharge.isRunning()) {
			getPlayer().sendMessage("§c말이 아직 준비되지 않았습니다.");
			return false;
		}
		final Horse horse = getPlayer().getWorld().spawn(getPlayer().getLocation(), Horse.class);
		horse.setStyle(horseStyle);
		horse.setColor(horseColor);
		horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(60);
		horse.setHealth(60);
		horse.setAdult();
		horse.setOwner(getPlayer());
		final HorseInventory inventory = horse.getInventory();
		inventory.setArmor(armorItem);
		inventory.setSaddle(saddleItem);
		horse.setJumpStrength(.7);
		horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(.175);
		horse.addPassenger(getPlayer());
		this.horse = horse;
		lanceCharge.start();
		highSpeedDash.start();
		SoundLib.ENTITY_HORSE_SADDLE.playSound(getPlayer().getLocation());
		return true;
	}

	public boolean unmount(boolean absorption) {
		final Horse horse = this.horse;
		if (horse == null) return false;
		if (absorption && horse.isValid()) {
			new Absorption(horse.getHealth() * .7);
		}
		horse.remove();
		this.horse = null;
		horseCharge.start();
		highSpeedDash.stop(true);
		lanceCharge.stop(true);
		return true;
	}

	public boolean isMounted() {
		return horse != null;
	}

	public boolean isLanceReady() {
		return isMounted() && !lanceCharge.isRunning();
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT) {
			if (clickType == ClickType.RIGHT_CLICK) {
				return mount();
			} else if (clickType == ClickType.LEFT_CLICK) {
				if (!isMounted()) {
					getPlayer().sendMessage("§c말에 타고있지 않습니다.");
					return false;
				}
				if (!highSpeedDash.startDash()) {
					getPlayer().sendMessage("§c아직 쾌속 질주를 사용할 수 없습니다.");
					return false;
				}
				return true;
			}
		}
		return false;
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerQuit(final PlayerQuitEvent e) {
		unmount(false);
	}

	@SubscribeEvent(ignoreCancelled = true)
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		final Entity entity = e.getEntity();
		if (getPlayer().equals(e.getDamager()) && isLanceReady()) {
			lanceCharge.start();
			e.setCancelled(true);
			if (!highSpeedDash.isDashing()) {
				Damages.damageFixed(entity, getPlayer(), (float) (e.getDamage() * .4));
				SoundLib.BLOCK_ANVIL_LAND.playSound(getPlayer().getLocation(), .5f, 1.5f);
			} else {
				Damages.damageFixed(entity, getPlayer(), (float) (e.getDamage() * highSpeedDash.currentTier.lanceMultiplier));
				SoundLib.BLOCK_ANVIL_LAND.playSound(getPlayer().getLocation(), .5f, 1.5f);
				SoundLib.ENTITY_ITEM_BREAK.playSound(getPlayer().getLocation());
			}
		}
		if (!getPlayer().equals(e.getEntity()) && lanceCharge.isRunning() && getPlayer().getWorld() == entity.getWorld() && getPlayer().getLocation().distanceSquared(entity.getLocation()) <= 225) {
			final double reduced = e.getDamage() - e.getFinalDamage();
			lanceCharge.setCount(lanceCharge.getCount() + (int) (reduced / 3.0));
		}
	}

	@SubscribeEvent
	private void onVehicleExit(final VehicleExitEvent e) {
		if (getPlayer().equals(e.getExited())) {
			final Horse horse = this.horse;
			if (horse != null && horse.equals(e.getVehicle())) {
				unmount(true);
			}
		}
	}

	@SubscribeEvent
	private void onEntityDeath(final EntityDeathEvent e) {
		if (this.horse != null && e.getEntity() == this.horse) {
			e.getDrops().clear();
			unmount(false);
		}
	}

	@SubscribeEvent
	private void onInventoryClick(final InventoryClickEvent e) {
		if (getPlayer().equals(e.getWhoClicked()) && isMounted() && e.getClickedInventory() instanceof HorseInventory) {
			e.setCancelled(true);
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			new AbilityTimer() {
				@Override
				protected void run(int count) {
					final Horse horse = Canis.this.horse;
					if (horse != null) {
						if (!horse.isValid() || !horse.equals(getPlayer().getVehicle())) {
							unmount(true);
						}
					}
				}
			}.setPeriod(TimeUnit.SECONDS, 1).start();
		} else if (update == Update.RESTRICTION_SET || update == Update.ABILITY_DESTROY) {
			unmount(false);
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onVelocity(final PlayerVelocityEvent e) {
		e.setCancelled(true);
	}

}
