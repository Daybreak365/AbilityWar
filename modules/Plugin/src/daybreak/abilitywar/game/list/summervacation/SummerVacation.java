package daybreak.abilitywar.game.list.summervacation;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.DeathSettings;
import daybreak.abilitywar.config.game.GameSettings.Setting;
import daybreak.abilitywar.game.Category;
import daybreak.abilitywar.game.Category.GameCategory;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameAliases;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.interfaces.Winnable;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.InfiniteDurability;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.base.minecraft.item.builder.ItemBuilder;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 신나는 여름 휴가
 *
 * @author Daybreak 새벽
 */
@GameManifest(name = "신나는 여름 휴가", description = {"§f신나는 물총싸움 뿌슝빠슝! 지금 바로 즐겨보세요!", "", "§a● §f스크립트가 적용되지 않습니다.",
		"§a● §f일부 콘피그가 임의로 변경될 수 있습니다.", "", "§6● §f신나는 여름 휴가 전용 콘피그가 있습니다."})
@GameAliases("여름휴가")
@Category(GameCategory.MINIGAME)
public class SummerVacation extends Game implements Winnable, DefaultKitHandler {

	public static final Setting<Integer> KILLS_TO_WIN = gameSettings.new Setting<Integer>(SummerVacation.class, "kills-to-win", 10, "# 우승을 위해 필요한 킬 횟수") {
		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}
	};

	public SummerVacation() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		this.killsToWin = KILLS_TO_WIN.getValue();
	}

	private final Objective killObjective = getScoreboardManager().registerNewObjective("킬 횟수", "dummy", "§c킬 횟수");

	@Override
	protected void progressGame(int seconds) {
		switch (seconds) {
			case 1: {
				int count = 0;

				Bukkit.broadcastMessage("§6==== §f게임 참여자 목록 §6====");
				for (Participant p : getParticipants()) {
					count++;
					Bukkit.broadcastMessage("§c" + count + ". §f" + p.getPlayer().getName());
				}
				Bukkit.broadcastMessage("§f총 인원수 §c: §e" + count + "명");
				Bukkit.broadcastMessage("§6===========================");
				if (getParticipants().size() < 1) {
					stop();
					Bukkit.broadcastMessage("§c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. §8(§71명§8)");
				}
				break;
			}
			case 5:
				for (String msg : Arrays.asList("§eSummer Vacation §f- §c여름 휴가",
						"§e플러그인 버전 §7: §f" + AbilityWar.getPlugin().getDescription().getVersion(),
						"§b모드 개발자 §7: §fDaybreak 새벽",
						"§9디스코드 §7: §fsaebyeog")) {
					Bukkit.broadcastMessage(msg);
				}
				break;
			case 10:
				Bukkit.broadcastMessage("§e신나는 여름 휴가 §f모드에서는 모든 플레이어의 능력이 물총으로 고정됩니다.");
				try {
					for (Participant participant : getParticipants()) {
						participant.setAbility(SquirtGun.class);
					}
				} catch (ReflectiveOperationException ignored) {}
				break;
			case 13:
				Bukkit.broadcastMessage("§7스코어보드 §f설정 중...");
				killObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
				for (Participant participant : getParticipants()) {
					Score score = killObjective.getScore(participant.getPlayer().getName());
					score.setScore(0);
				}
				Bukkit.broadcastMessage("§e잠시 후 §f게임이 시작됩니다.");
				break;
			case 16:
				Bukkit.broadcastMessage("§f게임이 §e5§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 17:
				Bukkit.broadcastMessage("§f게임이 §e4§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 18:
				Bukkit.broadcastMessage("§f게임이 §e3§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 19:
				Bukkit.broadcastMessage("§f게임이 §e2§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 20:
				Bukkit.broadcastMessage("§f게임이 §e1§f초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 21:
				for (String m : Messager.asList(
						"§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
						"§f           §eSummer Vacation §f- §c여름 휴가 ",
						"§f                    게임 시작                ",
						"§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")) {
					Bukkit.broadcastMessage(m);
				}
				SoundLib.ENTITY_PLAYER_SPLASH.broadcastSound();

				giveDefaultKit(getParticipants());

				for (Participant p : getParticipants()) {
					if (Settings.getSpawnEnable()) {
						p.getPlayer().teleport(Settings.getSpawnLocation().toBukkitLocation());
					}
				}

				Bukkit.broadcastMessage("§2배고픔 무제한§a이 적용됩니다.");

				glow.start();

				Bukkit.broadcastMessage("§4초반 무적§c이 적용되지 않습니다.");
				for (Participant participant : this.getParticipants()) {
					if (participant.hasAbility()) {
						participant.getAbility().setRestricted(false);
					}
				}

				addModule(new InfiniteDurability());

				for (World w : Bukkit.getWorlds()) {
					if (Settings.getClearWeather()) {
						w.setStorm(false);
					}
				}

				getDeathManager().setAbilityRemoval(false);
				startGame();
				setRestricted(false);
				break;
		}
	}

	private final Set<Participant> killers = new HashSet<>();

	private final GameTimer glow = new GameTimer(TaskType.INFINITE, -1) {
		@Override
		protected void run(int count) {
			for (Participant p : killers) {
				PotionEffects.GLOWING.addPotionEffect(p.getPlayer(), 20, 0, true);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 10);

	private final int killsToWin;

	@Override
	protected @NotNull DeathManager newDeathManager() {
		return new DeathManager(this) {
			@Override
			public void Operation(Participant victim) {
				Player victimPlayer = victim.getPlayer();
				if (victimPlayer.getKiller() != null) {
					Participant victimParticipant = getParticipant(victimPlayer);
					if (victimParticipant != null) killers.remove(victimParticipant);
					Participant killer = getParticipant(victimPlayer.getKiller());
					if (killer != null && !killer.getPlayer().equals(victimPlayer)) {
						killers.add(killer);
						Score score = killObjective.getScore(killer.getPlayer().getName());
						if (score.isScoreSet()) {
							score.setScore(score.getScore() + 1);
							if (score.getScore() >= killsToWin) {
								Win(killer);
							}
						}
					}
				}
				if (DeathSettings.getAutoRespawn()) {
					new BukkitRunnable() {
						@Override
						public void run() {
							NMS.respawn(victim.getPlayer());
						}
					}.runTaskLater(AbilityWar.getPlugin(), 2L);
				}
			}
		};
	}

	private static final ItemStack WATER_GUN_ITEMSTACK = new ItemBuilder(MaterialX.BOW)
			.displayName("§b물총")
			.unbreakable(true)
			.build();


	/**
	 * 기본 킷 유저 지급
	 */
	@Override
	public void giveDefaultKit(Participant participant) {
		final Player player = participant.getPlayer();
		final List<ItemStack> defaultKit = Arrays.asList(WATER_GUN_ITEMSTACK, new ItemStack(Material.ARROW, 64), new ItemStack(Material.IRON_INGOT, 64));

		if (Settings.getInventoryClear()) {
			player.getInventory().clear();
		}

		for (ItemStack stack : defaultKit) {
			player.getInventory().addItem(stack);
		}

		final ItemStack boots = new ItemStack(Material.IRON_BOOTS);
		final ItemMeta bootsMeta = boots.getItemMeta();
		bootsMeta.setUnbreakable(true);
		bootsMeta.setDisplayName("§e오리발");
		boots.setItemMeta(bootsMeta);
		boots.addEnchantment(Enchantment.BINDING_CURSE, 1);
		boots.addEnchantment(Enchantment.DEPTH_STRIDER, 3);

		final ItemStack helmet = new ItemStack(Material.IRON_HELMET);
		final ItemMeta helmetMeta = helmet.getItemMeta();
		helmetMeta.setUnbreakable(true);
		helmetMeta.setDisplayName("§b안경");
		helmet.setItemMeta(helmetMeta);
		helmet.addEnchantment(Enchantment.BINDING_CURSE, 1);

		player.getInventory().setHelmet(helmet);
		player.getInventory().setBoots(boots);
	}

	@Override
	public AbilitySelect newAbilitySelect() {
		return null;
	}

	@Override
	protected void onEnd() {
		super.onEnd();
		killObjective.unregister();
	}

	@Override
	public void executeCommand(CommandType commandType, CommandSender sender, String command, String[] args, Plugin plugin) {
		if (commandType == CommandType.ABI) {
			sender.sendMessage(ChatColor.RED + "이 게임모드에서 사용할 수 없는 명령어입니다.");
		} else {
			super.executeCommand(commandType, sender, command, args, plugin);
		}
	}

	@Override
	public List<String> tabComplete(CommandType commandType, CommandSender sender, String command, String[] args, Plugin plugin) {
		return null;
	}

}
