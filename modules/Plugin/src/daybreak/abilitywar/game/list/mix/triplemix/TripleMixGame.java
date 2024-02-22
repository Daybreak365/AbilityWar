package daybreak.abilitywar.game.list.mix.triplemix;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.InvincibilitySettings;
import daybreak.abilitywar.game.GameAliases;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.module.InfiniteDurability;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.Collection;
import java.util.List;
import java.util.Random;

@GameManifest(name = "트리플 믹스", description = {
		"§f두 개도 모자라서 능력 세 개를 동시에?",
		"§f지금 바로 믹스!",
		"",
		"§f세가지의 능력으로 펼치는 능력자 전쟁입니다."
})
@Beta
@GameAliases({"트믹"})
public class TripleMixGame extends AbstractTripleMix implements DefaultKitHandler {

	private static final Logger logger = Logger.getLogger(TripleMixGame.class);
	private final boolean invincible = InvincibilitySettings.isEnabled();

	public TripleMixGame() {
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
						"§5TripleMix §f- §d트리플 믹스",
						"§e버전 §7: §f" + AbilityWar.getPlugin().getDescription().getVersion(),
						"§b모드 개발자 §7: §fDaybreak 새벽",
						"§9디스코드 §7: §fsaebyeog"
				);

				GameCreditEvent event = new GameCreditEvent(this);
				Bukkit.getPluginManager().callEvent(event);
				lines.addAll(event.getCredits());

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
						"§f            §5TripleMix §f- §d트리플 믹스  ",
						"§f                    게임 시작                ",
						"§d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")) {
					Bukkit.broadcastMessage(line);
				}

				giveDefaultKit(getParticipants());

				if (Configuration.Settings.getSpawnEnable()) {
					Location spawn = Configuration.Settings.getSpawnLocation().toBukkitLocation();
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
					addModule(new InfiniteDurability());
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
		return new AbilitySelect(this, getParticipants(), Settings.getAbilityChangeCount()) {

			private List<Class<? extends AbilityBase>> abilities;

			@Override
			protected void drawAbility(Collection<? extends Participant> selectors) {
				abilities = AbilityCollector.EVERY_ABILITY_EXCLUDING_BLACKLISTED.collect(TripleMixGame.this.getClass());
				if (abilities.size() > 0) {
					Random random = new Random();

					for (Participant participant : selectors) {
						Player p = participant.getPlayer();

						final Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size())), secondAbilityClass = abilities.get(random.nextInt(abilities.size())), thirdAbilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							((TripleMix) participant.getAbility()).setAbility(abilityClass, secondAbilityClass, thirdAbilityClass);

							final Player player = participant.getPlayer();
							player.sendMessage("§a능력이 할당되었습니다. §e/aw check§f로 확인하세요.");
							if (!hasDecided(participant)) {
								player.sendMessage("§e/aw yes §f명령어로 능력을 확정하거나, §e/aw no §f명령어로 능력을 변경하세요.");
							}
						} catch (ReflectiveOperationException e) {
							logger.error(ChatColor.YELLOW + participant.getPlayer().getName() + ChatColor.WHITE + "님에게 능력을 할당하는 도중 오류가 발생하였습니다.");
							logger.error("문제가 발생한 능력: §b" + abilityClass.getName() + " §f또는 §b" + secondAbilityClass.getName() + " §f또는 §b" + thirdAbilityClass.getName());
						}
					}
				} else {
					Messager.broadcastErrorMessage("사용 가능한 능력이 없습니다.");
					GameManager.stopGame();
				}
			}

			@Override
			protected boolean changeAbility(Participant participant) {
				final Player p = participant.getPlayer();

				if (abilities.size() > 0) {
					final Random random = new Random();

					if (participant.hasAbility()) {
						final Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size())), secondAbilityClass = abilities.get(random.nextInt(abilities.size())), thirdAbilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							((TripleMix) participant.getAbility()).setAbility(abilityClass, secondAbilityClass, thirdAbilityClass);
							return true;
						} catch (Exception e) {
							logger.error(ChatColor.YELLOW + p.getName() + ChatColor.WHITE + "님의 능력을 변경하는 도중 오류가 발생하였습니다.");
							logger.error("문제가 발생한 능력: §b" + abilityClass.getName() + " §f또는 §b" + secondAbilityClass.getName() + " §f또는 §b" + thirdAbilityClass.getName());
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
