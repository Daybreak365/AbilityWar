package daybreak.abilitywar.game.list.mix;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.config.Configuration.Settings.DeveloperSettings;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.ParticipantStrategy;
import daybreak.abilitywar.game.event.participant.ParticipantAbilitySetEvent;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.list.mix.synergy.SynergyFactory;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class AbstractMix extends Game {

	public AbstractMix(Collection<Player> players) {
		super(players);
	}

	@Override
	public void executeCommand(CommandType commandType, CommandSender sender, String command, String[] args, Plugin plugin) {
		switch (commandType) {
			case ABI: {
				if (args.length == 0) {
					if (sender instanceof Player)
						Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util abi <대상/@a> §7또는 §f/" + command + " util abi <대상/@a> [능력], [능력]");
					else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util abi <대상/@a> [능력], [능력]");
					return;
				}
				if (args.length == 1) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						if (args[0].equalsIgnoreCase("@a")) {
							new MixAbilityGUI(player, plugin).openGUI(1);
						} else {
							Player targetPlayer = Bukkit.getPlayerExact(args[0]);
							if (targetPlayer != null) {
								AbstractGame game = GameManager.getGame();
								if (game.isParticipating(targetPlayer)) {
									new MixAbilityGUI(player, game.getParticipant(targetPlayer), plugin).openGUI(1);
								} else
									Messager.sendErrorMessage(player, targetPlayer.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
							} else
								Messager.sendErrorMessage(player, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
						}
					} else Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util abi <대상/@a> [능력], [능력]");
				} else {
					final String[] names = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).split(",");
					if (names.length != 2) {
						Messager.sendErrorMessage(sender, "능력이 두 개 보다 많이 입력되었거나 적게 입력되었습니다.");
						return;
					}
					names[0] = names[0].trim();
					names[1] = names[1].trim();
					if (AbilityList.isRegistered(names[0])) {
						if (AbilityList.isRegistered(names[1])) {
							if (args[0].equalsIgnoreCase("@a")) {
								try {
									for (MixParticipant participant : getParticipants()) {
										participant.getAbility().setAbility(AbilityList.getByString(names[0]), AbilityList.getByString(names[1]));
									}
									Bukkit.broadcastMessage("§e" + sender.getName() + "§a님이 §f모든 참가자§a에게 능력을 임의로 부여하였습니다.");
								} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
									Messager.sendErrorMessage(sender, "능력 설정 도중 오류가 발생하였습니다.");
									if (DeveloperSettings.isEnabled()) e.printStackTrace();
								}
							} else {
								Player targetPlayer = Bukkit.getPlayerExact(args[0]);
								if (targetPlayer != null) {
									if (isParticipating(targetPlayer)) {
										try {
											getParticipant(targetPlayer).getAbility().setAbility(AbilityList.getByString(names[0]), AbilityList.getByString(names[1]));
											Bukkit.broadcastMessage("§e" + sender.getName() + "§a님이 §f" + targetPlayer.getName() + "§a님에게 능력을 임의로 부여하였습니다.");
										} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
											Messager.sendErrorMessage(sender, "능력 설정 도중 오류가 발생하였습니다.");
											if (DeveloperSettings.isEnabled()) e.printStackTrace();
										}
									} else
										Messager.sendErrorMessage(sender, targetPlayer.getName() + "님은 탈락했거나 게임에 참여하지 않았습니다.");
								} else
									Messager.sendErrorMessage(sender, args[0] + KoreanUtil.getJosa(args[0], Josa.은는) + " 존재하지 않는 플레이어입니다.");
							}
						} else
							Messager.sendErrorMessage(sender, names[1] + KoreanUtil.getJosa(names[1], Josa.은는) + " 존재하지 않는 능력입니다.");
					} else
						Messager.sendErrorMessage(sender, names[0] + KoreanUtil.getJosa(names[0], Josa.은는) + " 존재하지 않는 능력입니다.");
				}
			}
			break;
			case ABLIST: {
				sender.sendMessage("§2===== §a능력자 목록 §2=====");
				int count = 0;
				for (AbstractGame.Participant participant : GameManager.getGame().getParticipants()) {
					Mix mix = (Mix) participant.getAbility();
					if (mix.hasAbility()) {
						count++;
						if (mix.hasSynergy()) {
							Synergy synergy = mix.getSynergy();
							Pair<AbilityRegistration, AbilityRegistration> base = SynergyFactory.getSynergyBase(synergy.getRegistration());
							String name = "§e" + synergy.getName() + " §f(§c" + base.getLeft().getManifest().name() + " §f+ §c" + base.getRight().getManifest().name() + "§f)";
							sender.sendMessage("§e" + count + ". §f" + participant.getPlayer().getName() + " §7: " + name);
						} else {
							sender.sendMessage("§e" + count + ". §f" + participant.getPlayer().getName() + " §7: §c" + mix.getFirst().getName() + " §f+ §c" + mix.getSecond().getName());
						}
					}
				}
				if (count == 0) sender.sendMessage("§f능력자가 발견되지 않았습니다.");
				sender.sendMessage("§2========================");
				Bukkit.broadcastMessage("§f" + sender.getName() + "§a님이 참가자들의 능력을 확인하였습니다.");
			}
			break;
		}
	}

	@Override
	public DeathManager newDeathManager() {
		return new DeathManager(this) {
			@Override
			protected String getRevealMessage(Participant victim) {
				final Mix mix = (Mix) victim.getAbility();
				if (mix.hasAbility()) {
					if (mix.hasSynergy()) {
						final Synergy synergy = mix.getSynergy();
						final Pair<AbilityRegistration, AbilityRegistration> base = SynergyFactory.getSynergyBase(synergy.getRegistration());
						final String name = synergy.getName() + " (" + base.getLeft().getManifest().name() + " + " + base.getRight().getManifest().name() + ")";
						return "§f[§c능력§f] §c" + victim.getPlayer().getName() + "§f님의 능력은 §e" + name + "§f" + KoreanUtil.getJosa(name, KoreanUtil.Josa.이었였) + "습니다.";
					} else {
						final String name = mix.getFirst().getName() + " + " + mix.getSecond().getName();
						return "§f[§c능력§f] §c" + victim.getPlayer().getName() + "§f님의 능력은 §e" + name + "§f" + KoreanUtil.getJosa(name, KoreanUtil.Josa.이었였) + "습니다.";
					}
				} else {
					return "§f[§c능력§f] §c" + victim.getPlayer().getName() + "§f님은 능력이 없습니다.";
				}
			}
		};
	}

	@Override
	public Collection<MixParticipant> getParticipants() {
		return ((MixParticipantStrategy) participantStrategy).getParticipants();
	}

	@Override
	public MixParticipant getParticipant(Player player) {
		return ((MixParticipantStrategy) participantStrategy).getParticipant(player.getUniqueId());
	}

	@Override
	public MixParticipant getParticipant(UUID uuid) {
		return ((MixParticipantStrategy) participantStrategy).getParticipant(uuid);
	}

	@Override
	public ParticipantStrategy newParticipantStrategy(Collection<Player> players) {
		return new MixParticipantStrategy(players);
	}

	public class MixParticipant extends Participant {

		private MixParticipant(Player player) {
			super(player);
		}

		@Override
		public AbilityBase removeAbility() {
			AbilityBase ability = getAbility();
			if (ability != null) {
				((Mix) ability).removeAbility();
			}
			return null;
		}

		@Override
		public Mix getAbility() {
			if (!(this.ability instanceof Mix)) {
				try {
					if (this.ability != null) {
						this.ability.destroy();
					}
					AbilityBase mix = AbilityBase.create(Mix.class, this);
					mix.setRestricted(isRestricted() || !isGameStarted());
					this.ability = mix;
				} catch (IllegalAccessException | InvocationTargetException | InstantiationException ignored) {
				}
			}
			return (Mix) ability;
		}

		@Override
		public void setAbility(Class<? extends AbilityBase> abilityClass) throws IllegalAccessException, InstantiationException, InvocationTargetException {
			Mix mix = (Mix) this.ability;
			mix.setAbility(abilityClass, abilityClass);
			Bukkit.getPluginManager().callEvent(new ParticipantAbilitySetEvent(this, mix, mix));
		}

		@Override
		public boolean hasAbility() {
			return true;
		}

	}

	protected class MixParticipantStrategy implements ParticipantStrategy {

		private final Map<String, MixParticipant> participants = new HashMap<>();

		public MixParticipantStrategy(Collection<Player> players) {
			for (Player player : players) {
				participants.put(player.getUniqueId().toString(), new MixParticipant(player));
			}
		}

		@Override
		public Collection<MixParticipant> getParticipants() {
			return participants.values();
		}

		@Override
		public boolean isParticipating(UUID uuid) {
			return participants.containsKey(uuid.toString());
		}

		@Override
		public MixParticipant getParticipant(UUID uuid) {
			return participants.get(uuid.toString());
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
