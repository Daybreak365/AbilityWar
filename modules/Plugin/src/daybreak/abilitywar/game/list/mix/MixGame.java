package daybreak.abilitywar.game.list.mix;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings.InvincibilitySettings;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.manager.object.InfiniteDurability;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.library.SoundLib;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import javax.naming.OperationNotSupportedException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

@GameManifest(name = "믹스 능력자 전쟁", description = {
		"§f두 능력이 섞이면 어떻게 될까?",
		"§f지금 바로 믹스!",
		"",
		"§f두가지의 능력으로 펼치는 능력자 전쟁입니다."
})
public class MixGame extends AbstractMix implements DefaultKitHandler {

	private static final Logger logger = Logger.getLogger(MixGame.class);
	private final boolean invincible = InvincibilitySettings.isEnabled();

	public MixGame() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
	}

	@Override
	protected void progressGame(int seconds) {
		switch (seconds) {
			case 1:
				List<String> lines = Messager.asList("§5==== §d게임 참여자 목록 §5====");
				int count = 0;
				for (Participant p : getParticipants()) {
					count++;
					lines.add("§d" + count + ". §f" + p.getPlayer().getName());
				}
				lines.add("§5총 인원수 : " + count + "명");
				lines.add("§5==========================");

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}

				if (getParticipants().size() < 1) {
					stop();
					Bukkit.broadcastMessage("§c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. §8(§71명§8)");
				}
				break;
			case 3:
				lines = Messager.asList(
						"§5MixAbility §f- §d믹스 능력자 전쟁",
						"§e버전 §7: §f" + AbilityWar.getPlugin().getDescription().getVersion(),
						"§b모드 개발자 §7: §fDaybreak 새벽",
						"§9디스코드 §7: §f새벽§7#5908"
				);

				GameCreditEvent event = new GameCreditEvent();
				Bukkit.getPluginManager().callEvent(event);
				lines.addAll(event.getCreditList());

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}
				break;
			case 5:
				if (Configuration.Settings.getDrawAbility()) {
					try {
						startAbilitySelect();
					} catch (OperationNotSupportedException ignored) {
					}
				}
				break;
			case 6:
				Bukkit.broadcastMessage("§e잠시 후 게임이 시작됩니다.");
				break;
			case 8:
				Bukkit.broadcastMessage("§e게임이 §c5§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 9:
				Bukkit.broadcastMessage("§e게임이 §c4§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 10:
				Bukkit.broadcastMessage("§e게임이 §c3§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 11:
				Bukkit.broadcastMessage("§e게임이 §c2§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 12:
				Bukkit.broadcastMessage("§e게임이 §c1§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 13:
				for (String line : Messager.asList(
						"§d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
						"§f            §5MixAbility §f- §d믹스 능력자 전쟁  ",
						"§f                    게임 시작                ",
						"§d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")) {
					Bukkit.broadcastMessage(line);
				}

				giveDefaultKit(getParticipants());

				if (Configuration.Settings.getSpawnEnable()) {
					Location spawn = Configuration.Settings.getSpawnLocation();
					for (Participant participant : getParticipants()) {
						participant.getPlayer().teleport(spawn);
					}
				}

				if (Configuration.Settings.getNoHunger()) {
					Bukkit.broadcastMessage("§2배고픔 무제한§a이 적용됩니다.");
				} else {
					Bukkit.broadcastMessage("§4배고픔 무제한§c이 적용되지 않습니다.");
				}

				if (Configuration.Settings.getInfiniteDurability()) {
					attachObserver(new InfiniteDurability());
				} else {
					Bukkit.broadcastMessage("§4내구도 무제한§c이 적용되지 않습니다.");
				}

				if (Configuration.Settings.getClearWeather()) {
					for (World w : Bukkit.getWorlds()) {
						w.setStorm(false);
					}
				}

				if (invincible) {
					getInvincibility().start(false);
				} else {
					Bukkit.broadcastMessage("§4초반 무적§c이 적용되지 않습니다.");
					setRestricted(false);
				}

				ScriptManager.runAll(this);

				startGame();
				break;
		}
	}

	@Override
	public AbilitySelect newAbilitySelect() {
		return new AbilitySelect(this, getParticipants(), 1) {

			private List<Class<? extends AbilityBase>> abilities;

			@Override
			protected void drawAbility(Collection<? extends Participant> selectors) {
				abilities = AbilitySelectStrategy.EVERY_ABILITY_EXCLUDING_BLACKLISTED.getAbilities();
				if (abilities.size() > 0) {
					Random random = new Random();

					for (Participant participant : selectors) {
						Player p = participant.getPlayer();

						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						Class<? extends AbilityBase> secondAbilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							((Mix) participant.getAbility()).setAbility(abilityClass, secondAbilityClass);

							p.sendMessage(new String[]{
									"§a능력이 할당되었습니다. §e/aw check§f로 확인 할 수 있습니다.",
									"§e/aw yes §f명령어를 사용하여 능력을 확정합니다.",
									"§e/aw no §f명령어를 사용하여 능력을 변경합니다."
							});
						} catch (IllegalAccessException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException e) {
							logger.error(ChatColor.YELLOW + participant.getPlayer().getName() + ChatColor.WHITE + "님에게 능력을 할당하는 도중 오류가 발생하였습니다.");
							logger.error("문제가 발생한 능력: " + ChatColor.AQUA + abilityClass.getName());
						}
					}
				} else {
					Messager.broadcastErrorMessage("사용 가능한 능력이 없습니다.");
					GameManager.stopGame();
				}
			}

			@Override
			protected boolean changeAbility(Participant participant) {
				Player p = participant.getPlayer();

				if (abilities.size() > 0) {
					Random random = new Random();

					if (participant.hasAbility()) {
						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						Class<? extends AbilityBase> secondAbilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							((Mix) participant.getAbility()).setAbility(abilityClass, secondAbilityClass);
							return true;
						} catch (Exception e) {
							logger.error(ChatColor.YELLOW + p.getName() + ChatColor.WHITE + "님의 능력을 변경하는 도중 오류가 발생하였습니다.");
							logger.error(ChatColor.WHITE + "문제가 발생한 능력: " + ChatColor.AQUA + abilityClass.getName());
						}
					}
				} else {
					Messager.sendErrorMessage(p, "능력을 변경할 수 없습니다.");
				}

				return false;
			}
		};
	}

}
