package daybreak.abilitywar.game.list.mix.synergy.game;

import com.google.common.base.Strings;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.Provider;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Tip;
import daybreak.abilitywar.addon.Addon;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.InvincibilitySettings;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.ParticipantStrategy;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.event.participant.ParticipantAbilitySetEvent;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.list.mix.synergy.SynergyFactory;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.module.InfiniteDurability;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.Seasons;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@GameManifest(name = "시너지 능력자", description = {
		""
})
@Beta
public class SynergyGame extends Game implements DefaultKitHandler {

	private static final Logger logger = Logger.getLogger(SynergyGame.class);

	public SynergyGame() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
	}

	private static List<String> formatSynergyInfo(Synergy synergy) {
		final Provider provider = synergy.getRegistration().getProvider();
		final String providerName = provider instanceof Addon ? ((Addon) provider).getDisplayName() : null;
		final List<String> list = Messager.asList(
				Formatter.formatTitle(32, ChatColor.GREEN, ChatColor.YELLOW, "능력 정보"),
				"§b" + synergy.getName() + " " + (synergy.isRestricted() ? "§f[§7능력 비활성화됨§f]" : "§f[§a능력 활성화됨§f]") + " " + synergy.getRank().getRankName() + " " + synergy.getSpecies().getSpeciesName() + (providerName != null ? " §7| §f" + providerName : ""));

		list.add("§a---------------------------------");
		final Pair<AbilityRegistration, AbilityRegistration> base = SynergyFactory.getSynergyBase(synergy.getRegistration());
		list.add("§f시너지: §a" + base.getLeft().getManifest().name() + " §f+ §a" + base.getRight().getManifest().name());
		list.add("§a---------------------------------");
		list.add("§b" + synergy.getName() + " " + synergy.getRank().getRankName() + " " + synergy.getSpecies().getSpeciesName());
		for (Iterator<String> iterator = synergy.getExplanation(); iterator.hasNext(); ) {
			list.add(iterator.next());
		}
		list.add("§a---------------------------------");
		return list;
	}

	@Override
	public Collection<SynergyParticipant> getParticipants() {
		return ((SynergyParticipantStrategy) participantStrategy).getParticipants();
	}

	@Override
	public SynergyParticipant getParticipant(Player player) {
		return ((SynergyParticipantStrategy) participantStrategy).getParticipant(player.getUniqueId());
	}

	@Override
	public SynergyParticipant getParticipant(UUID uuid) {
		return ((SynergyParticipantStrategy) participantStrategy).getParticipant(uuid);
	}

	@Override
	protected void progressGame(int seconds) {
		switch (seconds) {
			case 1:
				List<String> lines = Messager.asList("§5==== §d게임 참여자 목록 §5====");
				int count = 0;
				for (Participant participant : getParticipants()) {
					count++;
					lines.add("§d" + count + ". §f" + participant.getPlayer().getName());
				}
				lines.add("§5총 인원수 : " + count + "명");
				lines.add("§5==========================");

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}
				break;
			case 3:
				lines = Messager.asList(
						"§5SynergyGame §f- §d시너지 능력자 전쟁",
						"§e버전 §7: §f" + AbilityWar.getPlugin().getDescription().getVersion(),
						"§b모드 개발자 §7: §fDaybreak 새벽",
						"§9디스코드 §7: §f새벽§7#5908"
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
				if (Seasons.isChristmas()) {
					final String blocks = Strings.repeat("§c■§2■", 22);
					Bukkit.broadcastMessage(blocks);
					Bukkit.broadcastMessage("§f            §cSynergyGame §f- §2시너지 능력자 전쟁  ");
					Bukkit.broadcastMessage("§f                   게임 시작                ");
					Bukkit.broadcastMessage(blocks);
				} else {
					for (String line : Messager.asList(
							"§d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
							"§f            §5SynergyGame §f- §d시너지 능력자 전쟁  ",
							"§f                    게임 시작                ",
							"§d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")) {
						Bukkit.broadcastMessage(line);
					}
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

				if (InvincibilitySettings.isEnabled()) {
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

			public List<Class<? extends AbilityBase>> collect(Class<? extends AbstractGame> game) {
				final List<Class<? extends AbilityBase>> abilities = new ArrayList<>();
				for (AbilityRegistration registration : SynergyFactory.getSynergies()) {
					if (!Settings.isBlacklisted(registration.getManifest().name()) && registration.isAvailable(game)) {
						abilities.add(registration.getAbilityClass());
					}
				}
				return abilities;
			}

			@Override
			protected void drawAbility(Collection<? extends Participant> selectors) {
				abilities = collect(SynergyGame.this.getClass());
				if (!abilities.isEmpty()) {
					final Random random = new Random();
					for (Participant participant : selectors) {
						final Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							participant.setAbility(abilityClass);
							final AbilityBase ability = participant.getAbility();
							final Player player = participant.getPlayer();
							player.sendMessage("§a능력이 할당되었습니다. §e/aw check§f로 확인하세요.");
							if (!hasDecided(participant)) {
								player.sendMessage("§e/aw yes §f명령어로 능력을 확정하거나, §e/aw no §f명령어로 능력을 변경하세요.");
							}
							final Tip tip = ability.getRegistration().getTip();
							if (tip != null) {
								player.sendMessage("§e/aw abtip§f으로 능력 팁을 확인하세요.");
							}
						} catch (SecurityException | ReflectiveOperationException | IllegalArgumentException e) {
							e.printStackTrace();
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
				if (!abilities.isEmpty()) {
					final Random random = new Random();

					if (participant.hasAbility()) {
						final Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							participant.setAbility(abilityClass);

							return true;
						} catch (Exception e) {
							logger.error(ChatColor.YELLOW + participant.getPlayer().getName() + ChatColor.WHITE + "님의 능력을 변경하는 도중 오류가 발생하였습니다.");
							logger.error(ChatColor.WHITE + "문제가 발생한 능력: " + ChatColor.AQUA + abilityClass.getName());
						}
					}
				} else {
					Messager.sendErrorMessage(participant.getPlayer(), "능력을 변경할 수 없습니다.");
				}

				return false;
			}
		};
	}

	@Override
	protected ParticipantStrategy newParticipantStrategy(Collection<Player> players) {
		return new SynergyParticipantStrategy(players);
	}

	@Override
	public void executeCommand(CommandType commandType, CommandSender sender, String command, String[] args, Plugin plugin) {
		switch (commandType) {
			case ABILITY_CHECK: {
				final Player player = (Player) sender;
				if (isParticipating(player)) {
					final SynergyParticipant participant = getParticipant(player);
					if (participant.hasAbility()) {
						for (String line : formatSynergyInfo(participant.getAbility())) {
							player.sendMessage(line);
						}
					} else {
						Messager.sendErrorMessage(sender, "능력이 할당되지 않았습니다.");
					}
				} else {
					Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다.");
				}
			}
			break;
			case ABI: {
				sender.sendMessage("아몰라 쓰지마");
			}
			break;
			case ABLIST: {
				sender.sendMessage("§2===== §a능력자 목록 §2=====");
				int count = 0;
				for (AbstractGame.Participant participant : GameManager.getGame().getParticipants()) {
					final AbilityBase ability = participant.getAbility();
					if (ability == null) continue;
					count++;
					final Pair<AbilityRegistration, AbilityRegistration> base = SynergyFactory.getSynergyBase(ability.getRegistration());
					String name = "§e" + ability.getName() + " §f(§c" + base.getLeft().getManifest().name() + " §f+ §c" + base.getRight().getManifest().name() + "§f)";
					sender.sendMessage("§e" + count + ". §f" + participant.getPlayer().getName() + " §7: " + name);
				}
				if (count == 0) sender.sendMessage("§f능력자가 발견되지 않았습니다.");
				sender.sendMessage("§2========================");
				Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 참가자들의 능력을 확인하였습니다.");
			}
			break;
			default:
				super.executeCommand(commandType, sender, command, args, plugin);
				break;
		}
	}

	public class SynergyParticipant extends Participant {

		private final Attributes attributes = new Attributes();
		private Synergy ability = null;

		protected SynergyParticipant(@NotNull Player player) {
			super(player);
		}

		@Override
		public boolean hasAbility() {
			return ability != null;
		}

		@Override
		@Nullable
		public Synergy getAbility() {
			return ability;
		}

		@Override
		public void setAbility(AbilityRegistration registration) throws ReflectiveOperationException {
			if (!Synergy.class.isAssignableFrom(registration.getAbilityClass()))
				throw new IllegalArgumentException("ability must be instance of Synergy");
			final Synergy oldAbility = removeAbility();
			final Synergy ability = (Synergy) AbilityBase.create(registration, this);
			ability.setRestricted(false);
			this.ability = ability;
			Bukkit.getPluginManager().callEvent(new ParticipantAbilitySetEvent(this, oldAbility, ability));
		}

		@Override
		@Nullable
		public Synergy removeAbility() {
			final Synergy ability = this.ability;
			if (ability != null) {
				ability.destroy();
				this.ability = null;
			}
			return ability;
		}

		@Override
		public Attributes attributes() {
			return attributes;
		}
	}

	protected class SynergyParticipantStrategy implements ParticipantStrategy {

		private final Map<UUID, SynergyParticipant> participants = new HashMap<>();

		public SynergyParticipantStrategy(Collection<Player> players) {
			for (Player player : players) {
				participants.put(player.getUniqueId(), new SynergyParticipant(player));
			}
		}

		@Override
		public Collection<SynergyParticipant> getParticipants() {
			return Collections.unmodifiableCollection(participants.values());
		}

		@Override
		public boolean isParticipating(UUID uuid) {
			return participants.containsKey(uuid);
		}

		@Override
		public SynergyParticipant getParticipant(UUID uuid) {
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

}
