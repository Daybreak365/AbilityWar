package daybreak.abilitywar.game.list.murdermystery;

import com.google.common.collect.ImmutableList;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Observer;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.interfaces.Winnable;
import daybreak.abilitywar.game.list.murdermystery.ability.Detective;
import daybreak.abilitywar.game.list.murdermystery.ability.Innocent;
import daybreak.abilitywar.game.list.murdermystery.ability.Murderer;
import daybreak.abilitywar.game.list.murdermystery.ability.extra.Police;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Predicates;
import daybreak.abilitywar.utils.base.minecraft.FireworkUtil;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.base.minecraft.compat.nms.NMSHandler;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion.Version;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.SoundLib;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

@GameManifest(name = "머더 미스터리", description = {

})
@Support(min = Version.v1_12_R1)
@Beta
public class MurderMystery extends AbstractGame implements Observer, Winnable {

	public static final List<Class<? extends AbilityBase>> JOB_ABILITIES = ImmutableList.<Class<? extends AbilityBase>>builder()
			.add(Police.class)
			.build();
	private static final Random random = new Random();
	private static final ItemStack AIR = new ItemStack(Material.AIR);
	private final Set<UUID> deadPlayers = new HashSet<>();
	private final Map<Participant, Integer> goldPoints = new HashMap<>();

	public MurderMystery() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		attachObserver(this);
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		for (Participant participant : getParticipants()) {
			goldPoints.put(participant, 0);
		}
	}

	public static Class<? extends AbilityBase> getRandomJob() {
		return JOB_ABILITIES.get(random.nextInt(JOB_ABILITIES.size()));
	}

	public boolean addGold(Participant participant) {
		final int gold = getGold(participant);
		if (gold < 64) {
			goldPoints.put(participant, gold + 1);
			updateGold(participant);
			participant.getPlayer().sendMessage("§6+ §e1 금");
			return true;
		} else return false;
	}

	public boolean consumeGold(Participant participant, int amount) {
		final int gold = getGold(participant);
		if (gold >= amount) {
			goldPoints.put(participant, gold - amount);
			updateGold(participant);
			return true;
		} else return false;
	}

	public int getGold(Participant participant) {
		return goldPoints.getOrDefault(participant, 0);
	}

	public void updateGold(Participant participant) {
		final int gold = getGold(participant);
		if (gold > 0) {
			ItemStack stack = MaterialX.GOLD_INGOT.parseItem();
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName("§6금");
			stack.setItemMeta(meta);
			stack.setAmount(getGold(participant));
			participant.getPlayer().getInventory().setItem(4, stack);
		} else {
			participant.getPlayer().getInventory().setItem(4, AIR);
		}
	}

	@Override
	protected void run(int count) {
		if (count <= 13) {
			switch (count) {
				case 1:
					List<String> lines = Messager.asList("§4==== §c게임 참가자 목록 §4====");
					int partCount = 0;
					for (Participant p : getParticipants()) {
						partCount++;
						lines.add("§c" + partCount + ". §f" + p.getPlayer().getName());
					}
					lines.add("§c총 인원수 : " + partCount + "명");
					lines.add("§4==========================");

					for (String line : lines) {
						Bukkit.broadcastMessage(line);
					}

					if (getParticipants().size() < 2) {
						stop();
						Bukkit.broadcastMessage("§c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. §8(§72명§8)");
					}
					break;
				case 3:
					for (String line : Messager.asList(
							"§4MurderMystery §f- §c머더 미스터리",
							"§e버전 §7: §f" + AbilityWar.getPlugin().getDescription().getVersion(),
							"§b개발자 §7: §fDaybreak 새벽",
							"§9디스코드 §7: §f새벽§7#5908")) {
						Bukkit.broadcastMessage(line);
					}
					break;
				case 5:
					Random random = new Random();
					List<Participant> participants = new ArrayList<>(getParticipants());
					final int div = (int) Math.max(Math.ceil(participants.size() / 7.0), 1);
					for (int i = 0; i < div; i++) {
						int index = random.nextInt(participants.size());
						Participant murderer = participants.get(index);
						participants.remove(index);
						try {
							murderer.setAbility(Murderer.class);
						} catch (IllegalAccessException | InstantiationException | InvocationTargetException ignored) {
						}
					}
					for (int i = 0; i < div; i++) {
						int index = random.nextInt(participants.size());
						Participant detective = participants.get(index);
						participants.remove(index);
						try {
							detective.setAbility(Detective.class);
						} catch (IllegalAccessException | InstantiationException | InvocationTargetException ignored) {
						}
					}
					for (Participant participant : participants) {
						try {
							participant.setAbility(Innocent.class);
						} catch (IllegalAccessException | InstantiationException | InvocationTargetException ignored) {
						}
					}
					break;
				case 6:
					Bukkit.broadcastMessage("§c잠시 후 게임이 시작됩니다.");
					break;
				case 8:
					Bukkit.broadcastMessage("§c게임이 §45§c초 후에 시작됩니다.");
					SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
					break;
				case 9:
					Bukkit.broadcastMessage("§c게임이 §44§c초 후에 시작됩니다.");
					SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
					break;
				case 10:
					Bukkit.broadcastMessage("§c게임이 §43§c초 후에 시작됩니다.");
					SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
					break;
				case 11:
					Bukkit.broadcastMessage("§c게임이 §42§c초 후에 시작됩니다.");
					SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
					break;
				case 12:
					Bukkit.broadcastMessage("§c게임이 §41§c초 후에 시작됩니다.");
					SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
					break;
				case 13:
					for (String line : Messager.asList(
							"§c■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
							"§f            §4MurderMystery §f- §c머더 미스터리",
							"§f                    게임 시작                ",
							"§c■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")) {
						Bukkit.broadcastMessage(line);
					}
					if (Settings.getSpawnEnable()) {
						Location spawn = Settings.getSpawnLocation();
						for (Participant participant : getParticipants()) {
							participant.getPlayer().teleport(spawn);
						}
					}
					startGame();
					setRestricted(false);
					break;
			}
		} else {
			for (Participant participant : getParticipants()) {
				Player player = participant.getPlayer();
				if (!player.isDead()) {
					player.setGameMode(deadPlayers.contains(player.getUniqueId()) ? GameMode.SPECTATOR : GameMode.ADVENTURE);
				}
			}
			if (count % 6 == 0) {
				for (Participant participant : getParticipants()) {
					Player player = participant.getPlayer();
					Location loc = LocationUtil.getRandomLocations(player.getLocation(), 15, 1).get(0);
					loc.setY(LocationUtil.getFloorYAt(loc.getWorld(), player.getLocation().getY(), loc.getBlockX(), loc.getBlockZ()));
					player.getWorld().dropItemNaturally(loc, Items.GOLD.getStack());
				}
			}
		}
	}

	@EventHandler
	private void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	private void onProjectileHit(ProjectileHitEvent e) {
		if (e.getEntity() instanceof Arrow) {
			Arrow arrow = (Arrow) e.getEntity();
			arrow.remove();
			if (arrow.getShooter() != null && arrow.getShooter() instanceof Entity && isParticipating(((Entity) arrow.getShooter()).getUniqueId()) && e.getHitEntity() != null && isParticipating(e.getHitEntity().getUniqueId()) && !arrow.getShooter().equals(e.getHitEntity())) {
				Participant hit = getParticipant(e.getHitEntity().getUniqueId()), shooter = getParticipant(((Entity) arrow.getShooter()).getUniqueId());
				if (hit.hasAbility() && !(hit.getAbility() instanceof Murderer) && shooter.hasAbility() && !(shooter.getAbility() instanceof Murderer)) {
					shooter.getPlayer().setHealth(0);
				}
				hit.getPlayer().setHealth(0);
			}
		}
	}

	@EventHandler
	private void onManipulate(PlayerArmorStandManipulateEvent e) {
		if (e.getRightClicked().hasMetadata("Bow")) e.setCancelled(true);
	}

	@EventHandler
	private void onPlayerDeath(PlayerDeathEvent e) {
		final Player entity = e.getEntity();
		e.setDeathMessage("§4" + entity.getName() + "§c님이 죽었습니다.");
		final Player killer = entity.getKiller();
		if (isParticipating(entity.getUniqueId())) {
			Participant entityPart = getParticipant(entity.getUniqueId());
			if (entityPart.hasAbility() && entityPart.getAbility() instanceof Detective) {
				new GameTimer(TaskType.INFINITE, -1) {
					private final Location center = entityPart.getPlayer().getLocation();
					private final Predicate<Entity> PREDICATE = Predicates.STRICT(entityPart.getPlayer()).and(new Predicate<Entity>() {
						@Override
						public boolean test(Entity entity) {
							return !(getParticipant(entity.getUniqueId()).getAbility() instanceof Murderer) && !deadPlayers.contains(entity.getUniqueId());
						}
					});
					private final ArmorStand stand = entityPart.getPlayer().getWorld().spawn(center, ArmorStand.class);

					@Override
					protected void onStart() {
						Bukkit.broadcastMessage("§eX: " + center.getBlockX() + "§f, §e" + "Y: " + center.getBlockY() + "§f, " + "§eZ: " + center.getBlockZ() + " §f위치에 §5탐정 §f활이 떨어졌습니다!");
						stand.getEquipment().setItemInMainHand(Items.DETECTIVE_BOW.getStack());
						stand.setMetadata("Bow", new FixedMetadataValue(AbilityWar.getPlugin(), null));
						stand.setInvulnerable(true);
						stand.setVisible(false);
					}

					@Override
					protected void run(int count) {
						for (Player player : LocationUtil.getNearbyEntities(Player.class, center, 1, 1, PREDICATE)) {
							try {
								getParticipant(player).setAbility(Detective.class);
							} catch (IllegalAccessException | InstantiationException | InvocationTargetException ignored) {
							}
							Bukkit.broadcastMessage("§5탐정§f이 바뀌었습니다!");
							stop(false);
							return;
						}
					}

					@Override
					protected void onEnd() {
						stand.remove();
					}

					@Override
					protected void onSilentEnd() {
						stand.remove();
					}
				}.setPeriod(TimeUnit.TICKS, 5).start();
			}
			entityPart.removeAbility();
		}
		if (killer != null && isParticipating(killer.getUniqueId()) && isParticipating(entity.getUniqueId())) {
			Participant killerPart = getParticipant(killer.getUniqueId()), entityPart = getParticipant(entity.getUniqueId());
			if (entityPart.hasAbility() && !(entityPart.getAbility() instanceof Murderer) && killerPart.hasAbility() && !(killerPart.getAbility() instanceof Murderer)) {
				killer.setHealth(0);
			}
		}
		deadPlayers.add(entity.getUniqueId());
		new BukkitRunnable() {
			@Override
			public void run() {
				NMSHandler.getNMS().respawn(entity);
				entity.setGameMode(GameMode.SPECTATOR);
			}
		}.runTaskLater(AbilityWar.getPlugin(), 3L);
		boolean innocentLeft = false;
		List<Participant> murderers = new ArrayList<>();
		for (Participant participant : getParticipants()) {
			if (deadPlayers.contains(participant.getPlayer().getUniqueId())) continue;
			if (participant.hasAbility() && participant.getAbility() instanceof Murderer) {
				murderers.add(participant);
			} else {
				innocentLeft = true;
			}
		}
		if (murderers.size() == 0) {
			Messager.clearChat();
			for (Participant participant : getParticipants()) {
				if (participant.hasAbility() && participant.getAbility() instanceof Murderer) continue;
				Player p = participant.getPlayer();
				SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(p);
				new SimpleTimer(TaskType.REVERSE, 8) {
					@Override
					protected void run(int seconds) {
						FireworkUtil.spawnWinnerFirework(p.getEyeLocation());
					}
				}.setPeriod(TimeUnit.TICKS, 8).start();
			}
			Bukkit.broadcastMessage("§e시민§5이 우승했습니다!");
			GameManager.stopGame();
		} else if (!innocentLeft) {
			Win(murderers.toArray(new Participant[0]));
		}
	}

	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if (isParticipating(e.getWhoClicked().getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	private void onPickupItem(EntityPickupItemEvent e) {
		if (isParticipating(e.getEntity().getUniqueId())) {
			e.setCancelled(true);
			Participant participant = getParticipant(e.getEntity().getUniqueId());
			if (getGold(participant) < 64 && e.getItem().getItemStack().isSimilar(Items.GOLD.getStack())) {
				e.getItem().remove();
				addGold(participant);
			}
		}
	}

	@EventHandler
	private void onEntityDamage(EntityDamageEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	private void onDropItem(PlayerDropItemEvent e) {
		if (isParticipating(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@Override
	public void update(GameUpdate update) {
		if (update == GameUpdate.END) {
			HandlerList.unregisterAll(this);
		}
	}

	@Override
	public void executeCommand(CommandType commandType, CommandSender sender, String command, String[] args, Plugin plugin) {
		sender.sendMessage(ChatColor.RED + "사용할 수 없는 명령어입니다.");
	}

}
