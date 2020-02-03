package daybreak.abilitywar.game.games.squirtgunfight;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.DeathSettings;
import daybreak.abilitywar.config.Configuration.Settings.SummerVacationSettings;
import daybreak.abilitywar.game.events.GameCreditEvent;
import daybreak.abilitywar.game.events.participant.ParticipantDeathEvent;
import daybreak.abilitywar.game.games.mode.GameManifest;
import daybreak.abilitywar.game.games.mode.decorator.Winnable;
import daybreak.abilitywar.game.games.standard.Game;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.manager.object.InfiniteDurability;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.message.KoreanUtil;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 체인지 능력 전쟁
 *
 * @author Daybreak 새벽
 */
@GameManifest(Name = "신나는 여름 휴가", Description = {"§f신나는 물총싸움 뿌슝빠슝! 지금 바로 즐겨보세요!", "", "§a● §f스크립트가 적용되지 않습니다.",
		"§a● §f일부 콘피그가 임의로 변경될 수 있습니다.", "", "§6● §f신나는 여름 휴가 전용 콘피그가 있습니다. Config.yml을 확인해보세요."})
public class SummerVacation extends Game implements Winnable, DefaultKitHandler {

	public SummerVacation() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		boolean invincible = Settings.InvincibilitySettings.isEnabled();
		setRestricted(invincible);
		this.MaxKill = SummerVacationSettings.getMaxKill();
	}

	@SuppressWarnings("deprecation")
	private final Objective killObjective = ServerVersion.getVersion() >= 13 ?
			getScoreboardManager().getScoreboard().registerNewObjective("킬 횟수", "dummy", ChatColor.translateAlternateColorCodes('&', "&c킬 횟수"))
			: getScoreboardManager().getScoreboard().registerNewObjective("킬 횟수", "dummy");

	private final InfiniteDurability infiniteDurability = new InfiniteDurability();

	private final TimerBase NoHunger = new TimerBase() {

		@Override
		public void onStart() {
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a배고픔 무제한이 적용됩니다."));
		}

		@Override
		public void onProcess(int count) {
			for (Participant p : getParticipants()) {
				p.getPlayer().setFoodLevel(19);
			}
		}

		@Override
		public void onEnd() {
		}
	};

	@Override
	protected void progressGame(int Seconds) {
		switch (Seconds) {
			case 1:
				broadcastPlayerList();
				if (getParticipants().size() < 1) {
					AbilityWarThread.StopGame();
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. &8(&71명&8)"));
				}
				break;
			case 5:
				broadcastPluginDescription();
				break;
			case 10:
				broadcastAbilityReady();
				try {
					for (Participant p : getParticipants()) {
						p.setAbility(SquirtGun.class);
					}
				} catch (InstantiationException | InvocationTargetException | IllegalAccessException ignored) {
				}
				break;
			case 13:
				scoreboardSetup();
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7스코어보드 &f설정중..."));
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e잠시 후 &f게임이 시작됩니다."));
				break;
			case 16:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &e5&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 17:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &e4&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 18:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &e3&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 19:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &e2&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 20:
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f게임이 &e1&f초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 21:
				GameStart();
				break;
		}
	}

	private void scoreboardSetup() {
		killObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		if (ServerVersion.getVersion() >= 13)
			killObjective.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c킬 횟수"));
		for (Participant p : getParticipants()) {
			Score score = killObjective.getScore(p.getPlayer().getName());
			score.setScore(0);
		}
	}

	private final List<Participant> Killers = new ArrayList<Participant>();

	private final TimerBase Glow = new TimerBase() {

		@Override
		protected void onStart() {
		}

		@Override
		protected void onEnd() {
		}

		@Override
		protected void onProcess(int count) {
			for (Participant p : Killers) {
				PotionEffects.GLOWING.addPotionEffect(p.getPlayer(), 20, 0, true);
			}
		}
	}.setPeriod(10);

	private final int MaxKill;

	@Override
	public DeathManager newDeathManager() {
		return new DeathManager(this) {
			@EventHandler
			protected void onDeath(PlayerDeathEvent e) {
				Player victimPlayer = e.getEntity();
				Player killerPlayer = victimPlayer.getKiller();
				if (victimPlayer.getLastDamageCause() != null) {
					DamageCause Cause = victimPlayer.getLastDamageCause().getCause();

					if (killerPlayer != null) {
						e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&a" + killerPlayer.getName() + "&f님이 &c" + victimPlayer.getName() + "&f님을 죽였습니다."));
					} else {
						if (Cause.equals(DamageCause.CONTACT)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 찔려 죽었습니다."));
						} else if (Cause.equals(DamageCause.FALL)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 떨어져 죽었습니다."));
						} else if (Cause.equals(DamageCause.FALLING_BLOCK)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 떨어지는 블록에 맞아 죽었습니다."));
						} else if (Cause.equals(DamageCause.SUFFOCATION)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 끼여 죽었습니다."));
						} else if (Cause.equals(DamageCause.DROWNING)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 익사했습니다."));
						} else if (Cause.equals(DamageCause.ENTITY_EXPLOSION)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 폭발했습니다."));
						} else if (Cause.equals(DamageCause.LAVA)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 용암에 빠져 죽었습니다."));
						} else if (Cause.equals(DamageCause.FIRE) || Cause.equals(DamageCause.FIRE_TICK)) {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 노릇노릇하게 구워졌습니다."));
						} else {
							e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 죽었습니다."));
						}
					}
				} else {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + victimPlayer.getName() + "&f님이 죽었습니다."));
				}

				if (isParticipating(victimPlayer)) {
					Participant victim = getParticipant(victimPlayer);

					Bukkit.getPluginManager().callEvent(new ParticipantDeathEvent(victim));

					if (DeathSettings.getAbilityReveal()) {
						if (victim.hasAbility()) {
							String name = victim.getAbility().getName();
							if (name != null) {
								Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f[&c능력&f] &c" + victimPlayer.getName() + "&f님의 능력은 &e" + name + "&f" + KoreanUtil.getNeededJosa(name, KoreanUtil.Josa.이었였) + "습니다."));
							}
						} else {
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f[&c능력&f] &c" + victimPlayer.getName() + "&f?님은 능력이 없습니다."));
						}
					}
				}

				if (victimPlayer.getKiller() != null) {
					Participant VictimPart = getParticipant(victimPlayer);
					if (VictimPart != null && Killers.contains(VictimPart)) Killers.remove(VictimPart);
					Participant Killer = getParticipant(victimPlayer.getKiller());
					if (Killer != null && !Killer.getPlayer().equals(victimPlayer)) {
						if (!Killers.contains(Killer)) Killers.add(Killer);
						Score score = killObjective.getScore(Killer.getPlayer().getName());
						if (score.isScoreSet()) {
							score.setScore(score.getScore() + 1);
							if (score.getScore() >= MaxKill) {
								Win(Killer);
							}
						}
					}
				}
			}
		};
	}

	public void broadcastPlayerList() {
		int Count = 0;

		ArrayList<String> msg = new ArrayList<String>();

		msg.add(ChatColor.translateAlternateColorCodes('&', "&6==== &f게임 참여자 목록 &6===="));
		for (Participant p : getParticipants()) {
			Count++;
			msg.add(ChatColor.translateAlternateColorCodes('&', "&c" + Count + ". &f" + p.getPlayer().getName()));
		}
		msg.add(ChatColor.translateAlternateColorCodes('&', "&f총 인원수 &c: &e" + Count + "명"));
		msg.add(ChatColor.translateAlternateColorCodes('&', "&6=========================="));

		for (String m : msg) {
			Bukkit.broadcastMessage(m);
		}
	}

	public void broadcastPluginDescription() {
		List<String> msg = Messager.asList(
				ChatColor.translateAlternateColorCodes('&', "&eSummer Vacation &f- &c여름 휴가"),
				ChatColor.translateAlternateColorCodes('&', "&e플러그인 버전 &7: &f" + AbilityWar.getPlugin().getDescription().getVersion()),
				ChatColor.translateAlternateColorCodes('&', "&b모드 개발자 &7: &fDaybreak 새벽"),
				ChatColor.translateAlternateColorCodes('&', "&9디스코드 &7: &fDayBreak&7#5908"));

		GameCreditEvent event = new GameCreditEvent();
		Bukkit.getPluginManager().callEvent(event);

		for (String str : event.getCreditList()) {
			msg.add(str);
		}

		for (String m : msg) {
			Bukkit.broadcastMessage(m);
		}
	}

	public void broadcastAbilityReady() {
		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e신나는 여름 휴가 &f모드에서는 모든 플레이어의 능력이 물총으로 고정됩니다."));
	}

	public void GameStart() {
		for (String m : Messager.asList(
				ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"),
				ChatColor.translateAlternateColorCodes('&', "&f           &eSummer Vacation &f- &c여름 휴가 "),
				ChatColor.translateAlternateColorCodes('&', "&f                    게임 시작                "),
				ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"))) {
			Bukkit.broadcastMessage(m);
		}
		SoundLib.ENTITY_PLAYER_SPLASH.broadcastSound();

		giveDefaultKit(getParticipants());

		for (Participant p : getParticipants()) {
			if (Settings.getSpawnEnable()) {
				p.getPlayer().teleport(Settings.getSpawnLocation());
			}
		}

		NoHunger.setPeriod(1);
		NoHunger.startTimer();

		Glow.startTimer();

		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4초반 무적&c이 적용되지 않습니다."));
		for (Participant participant : this.getParticipants()) {
			if (participant.hasAbility()) {
				participant.getAbility().setRestricted(false);
			}
		}

		attachObserver(infiniteDurability);

		for (World w : Bukkit.getWorlds()) {
			if (Settings.getClearWeather()) {
				w.setStorm(false);
			}
		}

		startGame();
	}

	/**
	 * 기본 킷 유저 지급
	 */
	@Override
	public void giveDefaultKit(Player player) {
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta bowMeta = bow.getItemMeta();
		bowMeta.setUnbreakable(true);
		bowMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b물총"));
		bow.setItemMeta(bowMeta);
		List<ItemStack> DefaultKit = Arrays.asList(bow, new ItemStack(Material.ARROW, 64), new ItemStack(Material.IRON_INGOT, 64));

		if (Settings.getInventoryClear()) {
			player.getInventory().clear();
		}

		for (ItemStack is : DefaultKit) {
			player.getInventory().addItem(is);
		}

		ItemStack boots = new ItemStack(Material.IRON_BOOTS);
		ItemMeta bootsMeta = boots.getItemMeta();
		bootsMeta.setUnbreakable(true);
		bootsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e오리발"));
		boots.setItemMeta(bootsMeta);
		boots.addEnchantment(Enchantment.BINDING_CURSE, 1);
		boots.addEnchantment(Enchantment.DEPTH_STRIDER, 3);

		ItemStack helmet = new ItemStack(Material.IRON_HELMET);
		ItemMeta helmetMeta = helmet.getItemMeta();
		helmetMeta.setUnbreakable(true);
		helmetMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b안경"));
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

}
