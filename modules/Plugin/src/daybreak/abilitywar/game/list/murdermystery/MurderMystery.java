package daybreak.abilitywar.game.list.murdermystery;

import com.google.common.collect.ImmutableList;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.DeveloperSettings;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Observer;
import daybreak.abilitywar.game.Category;
import daybreak.abilitywar.game.Category.GameCategory;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.ParticipantStrategy;
import daybreak.abilitywar.game.event.participant.ParticipantAbilitySetEvent;
import daybreak.abilitywar.game.interfaces.Winnable;
import daybreak.abilitywar.game.list.murdermystery.ability.AbstractJob;
import daybreak.abilitywar.game.list.murdermystery.ability.AbstractMurderer;
import daybreak.abilitywar.game.list.murdermystery.ability.Detective;
import daybreak.abilitywar.game.list.murdermystery.ability.Innocent;
import daybreak.abilitywar.game.list.murdermystery.ability.Murderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.innocent.Doctor;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.innocent.Police;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.AssassinMurderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.BlackMurderer;
import daybreak.abilitywar.game.list.murdermystery.ability.jobs.murderer.SniperMurderer;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.FireworkUtil;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.SoundLib;
import kotlin.ranges.RangesKt;
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
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@GameManifest(name = "머더 미스터리", description = {

})
@Category(GameCategory.MINIGAME)
@Support.Version(min = NMSVersion.v1_12_R1)
@Beta
public class MurderMystery extends AbstractGame implements Observer, Winnable {

	public static final List<Class<? extends AbilityBase>> JOB_ABILITIES = ImmutableList.<Class<? extends AbilityBase>>builder()
			.add(Police.class)
			.add(Doctor.class)
			.build();

	public static final List<Class<? extends AbilityBase>> MURDER_JOB_ABILITIES = ImmutableList.<Class<? extends AbilityBase>>builder()
			.add(AssassinMurderer.class)
			.add(BlackMurderer.class)
			.add(SniperMurderer.class)
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

	public boolean isDead(final UUID uniqueId) {
		return deadPlayers.contains(uniqueId);
	}

	public static Class<? extends AbilityBase> getRandomJob() {
		return JOB_ABILITIES.get(random.nextInt(JOB_ABILITIES.size()));
	}

	public static Class<? extends AbilityBase> getRandomMurderJob() {
		return MURDER_JOB_ABILITIES.get(random.nextInt(MURDER_JOB_ABILITIES.size()));
	}

	public boolean addGold(final Participant participant) {
		return addGold(participant, true);
	}

	private boolean addGold(final Participant participant, final boolean message) {
		final int gold = getGold(participant);
		if (gold < 64) {
			goldPoints.put(participant, gold + 1);
			updateGold(participant);
			if (message) participant.getPlayer().sendMessage("§6+ §e1 금");
			return true;
		} else return false;
	}

	public int addGold(final Participant participant, final int amount) {
		final int gold = getGold(participant);
		if (gold != 64) {
			final int earn = RangesKt.coerceAtMost(amount, 64 - gold);
			goldPoints.put(participant, gold + earn);
			updateGold(participant);
			participant.getPlayer().sendMessage("§6+ §e" + earn + " 금");
			return amount - earn;
		} else return amount;
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
			final ItemStack stack = MaterialX.GOLD_INGOT.createItem();
			final ItemMeta meta = stack.getItemMeta();
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
					final Random random = new Random();
					final List<Participant> participants = new ArrayList<>(getParticipants());
					final int div = (int) Math.max(Math.ceil(participants.size() / 9.0), 1);
					for (int i = 0; i < div; i++) {
						final int index = random.nextInt(participants.size());
						final Participant murderer = participants.get(index);
						participants.remove(index);
						try {
							murderer.setAbility(Murderer.class);
						} catch (ReflectiveOperationException ignored) {
						}
					}
					for (int i = 0; i < div; i++) {
						final int index = random.nextInt(participants.size());
						final Participant detective = participants.get(index);
						participants.remove(index);
						try {
							detective.setAbility(Detective.class);
						} catch (ReflectiveOperationException ignored) {
						}
					}
					for (Participant participant : participants) {
						try {
							participant.setAbility(Innocent.class);
						} catch (ReflectiveOperationException ignored) {
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
						Location spawn = Settings.getSpawnLocation().toBukkitLocation();
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
	private void onFoodLevelChange(FoodLevelChangeEvent e) {
		if (Settings.getNoHunger()) {
			e.setFoodLevel(20);
		}
	}

	@EventHandler
	private void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	private void onProjectileHit(ProjectileHitEvent e) {
		if (e.getEntity() instanceof Arrow) {
			final Arrow arrow = (Arrow) e.getEntity();
			arrow.remove();
			if (arrow.getShooter() instanceof Entity) {
				fireArrowHitEvent((Entity) arrow.getShooter(), e.getHitEntity());
			}
		}
	}

	public boolean fireArrowHitEvent(final Entity shoot, final Entity hit) {
		if (isParticipating(shoot.getUniqueId()) && hit != null && isParticipating(hit.getUniqueId()) && !shoot.equals(hit)) {
			final Participant victim = getParticipant(hit.getUniqueId()), shooter = getParticipant(shoot.getUniqueId());
			if (victim.getAbility() instanceof AbstractMurderer && shooter.getAbility() instanceof AbstractMurderer) {
				shooter.getPlayer().sendMessage("§c머더 팀을 죽일 수 없습니다.");
			} else {
				final ArrowKillEvent event = new ArrowKillEvent(shooter, victim);
				Bukkit.getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					if (victim.hasAbility() && !(victim.getAbility() instanceof AbstractMurderer) && shooter.hasAbility() && !(shooter.getAbility() instanceof AbstractMurderer)) {
						shooter.getPlayer().setHealth(0);
					}
					victim.getPlayer().setHealth(0);
					return true;
				}
			}
		}
		return false;
	}

	@EventHandler
	private void onManipulate(PlayerArmorStandManipulateEvent e) {
		if (e.getRightClicked().hasMetadata("Bow")) e.setCancelled(true);
	}

	@EventHandler
	private void onPlayerDeath(PlayerDeathEvent e) {
		final Player entity = e.getEntity();
		e.setDeathMessage("§4" + entity.getName() + "§c" + KoreanUtil.getJosa(entity.getName(), Josa.이가) + " 죽었습니다.");
		final Player killer = entity.getKiller();
		if (isParticipating(entity.getUniqueId())) {
			Participant entityPart = getParticipant(entity.getUniqueId());
			if (entityPart.hasAbility() && entityPart.getAbility() instanceof Detective) {
				new GameTimer(TaskType.INFINITE, -1) {
					private final Location center = entityPart.getPlayer().getLocation();
					private final Predicate<Entity> predicate = new Predicate<Entity>() {
						@Override
						public boolean test(Entity entity) {
							return isParticipating(entity.getUniqueId()) && !deadPlayers.contains(entity.getUniqueId()) && !(getParticipant(entity.getUniqueId()).getAbility() instanceof AbstractMurderer);
						}
					};
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
						for (Player player : LocationUtil.getNearbyEntities(Player.class, center, 1, 1, predicate)) {
							try {
								getParticipant(player).setAbility(Detective.class);
							} catch (ReflectiveOperationException ignored) {
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
			if (entityPart.hasAbility() && !(entityPart.getAbility() instanceof AbstractMurderer) && killerPart.hasAbility() && !(killerPart.getAbility() instanceof AbstractMurderer)) {
				killer.setHealth(0);
			}
		}
		deadPlayers.add(entity.getUniqueId());
		new BukkitRunnable() {
			@Override
			public void run() {
				NMS.respawn(entity);
				entity.setGameMode(GameMode.SPECTATOR);
			}
		}.runTaskLater(AbilityWar.getPlugin(), 3L);
		boolean innocentLeft = false;
		List<Participant> murderers = new ArrayList<>();
		for (Participant participant : getParticipants()) {
			if (deadPlayers.contains(participant.getPlayer().getUniqueId())) continue;
			if (participant.hasAbility() && participant.getAbility() instanceof AbstractMurderer) {
				murderers.add(participant);
			} else {
				innocentLeft = true;
			}
		}
		if (murderers.size() == 0) {
			Messager.clearChat();
			for (Participant participant : getParticipants()) {
				if (participant.hasAbility() && participant.getAbility() instanceof AbstractMurderer) continue;
				Player p = participant.getPlayer();
				SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(p);
				new SimpleTimer(TaskType.REVERSE, 8) {
					@Override
					protected void run(int seconds) {
						FireworkUtil.spawnWinnerFirework(p.getEyeLocation());
					}
				}.setPeriod(TimeUnit.TICKS, 4).start();
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

	@EventHandler
	private void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
		if (e.getMessage().equals("/금나와라뚝딱")) {
			e.setCancelled(true);
			if (e.getPlayer().isOp()) {
				if (isParticipating(e.getPlayer())) {
					Bukkit.broadcastMessage("§f" + e.getPlayer().getName() + "§a님이 디버그 명령어로 §e금 §a64개를 얻으셨습니다.");
					final Participant participant = getParticipant(e.getPlayer());
					new GameTimer(TaskType.NORMAL, 64) {
						@Override
						protected void run(int count) {
							if (getGold(participant) == 64) stop(true);
							addGold(participant, false);
						}
					}.setPeriod(TimeUnit.TICKS, 1).start();
				} else Messager.sendErrorMessage(e.getPlayer(), "게임에 참여중이지 않습니다.");
			} else Messager.sendErrorMessage(e.getPlayer(), "OP 권한이 있어야 합니다.");
		}
	}

	public static List<String> formatAbilityInfo(AbilityBase ability) {
		List<String> list = Messager.asList(
				Formatter.formatTitle(32, ChatColor.DARK_RED, ChatColor.RED, "능력 정보"),
				"§b" + ability.getName() + " " + (ability.isRestricted() ? "§f[§7능력 비활성화됨§f]" : "§f[§c능력 활성화됨§f]") + " " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName());
		for (Iterator<String> iterator = ability.getExplanation(); iterator.hasNext(); ) {
			list.add(iterator.next());
		}
		list.add("§4---------------------------------");
		return list;
	}

	@Override
	public void executeCommand(CommandType commandType, CommandSender sender, String command, String[] args, Plugin plugin) {
		switch (commandType) {
			case ABILITY_CHECK: {
				final Player player = (Player) sender;
				if (GameManager.isGameRunning()) {
					final AbstractGame game = GameManager.getGame();
					if (game.isParticipating(player)) {
						final Participant participant = game.getParticipant(player);
						if (participant.hasAbility()) {
							for (String line : formatAbilityInfo(participant.getAbility())) {
								player.sendMessage(line);
							}
						} else {
							Messager.sendErrorMessage(sender, "능력이 할당되지 않았습니다.");
						}
					} else {
						Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
					}
				} else {
					Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다.");
				}
			}
			break;
			case TIP_CHECK: {
				super.executeCommand(commandType, sender, command, args, plugin);
			}
			break;
			case ABI: {
				if (args.length < 2) {
					Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util abi <대상/@a> [직업]");
					return;
				}
				final String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

				if (JobList.isRegistered(name)) {
					if (args[0].equalsIgnoreCase("@a")) {
						try {
							for (Participant participant : GameManager.getGame().getParticipants()) {
								participant.setAbility(JobList.getByString(name));
							}
							Bukkit.broadcastMessage("§e" + sender.getName() + "§a님이 §f모든 참가자§a에게 직업을 임의로 부여하였습니다.");
						} catch (ReflectiveOperationException e) {
							Messager.sendErrorMessage(sender, "직업 설정 도중 오류가 발생하였습니다.");
							if (DeveloperSettings.isEnabled()) e.printStackTrace();
						}
					} else {
						final Player targetPlayer = Bukkit.getPlayerExact(args[0]);
						if (targetPlayer != null) {
							final AbstractGame game = GameManager.getGame();
							if (game.isParticipating(targetPlayer)) {
								try {
									game.getParticipant(targetPlayer).setAbility(JobList.getByString(name));
									Bukkit.broadcastMessage("§e" + sender.getName() + "§a님이 §f" + targetPlayer.getName() + "§a님에게 직업을 임의로 부여하였습니다.");
								} catch (ReflectiveOperationException e) {
									Messager.sendErrorMessage(sender, "직업 설정 도중 오류가 발생하였습니다.");
									if (DeveloperSettings.isEnabled()) e.printStackTrace();
								}
							} else Messager.sendErrorMessage(sender, targetPlayer.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
						} else Messager.sendErrorMessage(sender, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
					}
				} else Messager.sendErrorMessage(sender, name + KoreanUtil.getJosa(name, Josa.은는) + " 존재하지 않는 직업입니다.");
			}
			break;
			default: {
				sender.sendMessage(ChatColor.RED + "사용할 수 없는 명령어입니다.");
			}
			break;
		}
	}

	@Override
	public List<String> tabComplete(CommandType commandType, CommandSender sender, String command, String[] args, Plugin plugin) {
		return null;
	}

	@Override
	public Collection<MysteryParticipant> getParticipants() {
		return ((MysteryParticipantStrategy) this.participantStrategy).getParticipants();
	}

	@Override
	public MysteryParticipant getParticipant(Player player) {
		return ((MysteryParticipantStrategy) this.participantStrategy).getParticipant(player.getUniqueId());
	}

	@Override
	public MysteryParticipant getParticipant(UUID uuid) {
		return ((MysteryParticipantStrategy) this.participantStrategy).getParticipant(uuid);
	}

	@Override
	protected ParticipantStrategy newParticipantStrategy(Collection<Player> players) {
		return new MysteryParticipantStrategy(players);
	}

	public class MysteryParticipant extends Participant {

		private AbstractJob ability = null;
		private final MysteryAttributes attributes = new MysteryAttributes();

		protected MysteryParticipant(@NotNull Player player) {
			super(player);
		}

		@Override
		public void setAbility(AbilityRegistration registration) throws ReflectiveOperationException {
			if (!AbstractJob.class.isAssignableFrom(registration.getAbilityClass())) throw new IllegalArgumentException("ability must be instance of AbstractJob");
			final AbstractJob oldAbility = removeAbility();
			final AbstractJob ability = (AbstractJob) AbilityBase.create(registration, this);
			ability.setRestricted(false);
			this.ability = ability;
			Bukkit.getPluginManager().callEvent(new ParticipantAbilitySetEvent(this, oldAbility, ability));
		}

		@Override
		public boolean hasAbility() {
			return ability != null;
		}

		@Override
		@Nullable
		public AbstractJob getAbility() {
			return ability;
		}

		@Override
		@Nullable
		public AbstractJob removeAbility() {
			final AbstractJob ability = this.ability;
			if (ability != null) {
				ability.destroy();
				this.ability = null;
			}
			return ability;
		}

		@Override
		public MysteryAttributes attributes() {
			return attributes;
		}

		public class MysteryAttributes extends Attributes {

		}

	}

	protected class MysteryParticipantStrategy implements ParticipantStrategy {

		private final Map<UUID, MysteryParticipant> participants = new HashMap<>();

		protected MysteryParticipantStrategy(Collection<Player> players) {
			for (Player player : players) {
				participants.put(player.getUniqueId(), new MysteryParticipant(player));
			}
		}

		@Override
		public Collection<MysteryParticipant> getParticipants() {
			return Collections.unmodifiableCollection(participants.values());
		}

		@Override
		public boolean isParticipating(UUID uuid) {
			return participants.containsKey(uuid);
		}

		@Override
		public MysteryParticipant getParticipant(UUID uuid) {
			return participants.get(uuid);
		}

		@Override
		public void addParticipant(Player player) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("참가자를 추가할 수 없습니다.");
		}

		@Override
		public void removeParticipant(UUID uuid) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("참가자를 제거할 수 없습니다.");
		}

	}

	public static class ArrowKillEvent extends Event implements Cancellable {

		private static final HandlerList handlers = new HandlerList();

		@NotNull
		@Override
		public HandlerList getHandlers() {
			return handlers;
		}

		public static HandlerList getHandlerList() {
			return handlers;
		}

		private boolean cancelled;
		private final Participant shooter;
		private final Participant target;

		private ArrowKillEvent(final Participant shooter, final Participant target) {
			this.shooter = shooter;
			this.target = target;
		}

		public Participant getShooter() {
			return shooter;
		}

		public Participant getTarget() {
			return target;
		}

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public void setCancelled(final boolean cancelled) {
			this.cancelled = cancelled;
		}
	}

}
