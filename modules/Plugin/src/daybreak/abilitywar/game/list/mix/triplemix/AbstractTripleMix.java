package daybreak.abilitywar.game.list.mix.triplemix;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.Configuration.Settings.DeveloperSettings;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.ParticipantStrategy;
import daybreak.abilitywar.game.event.participant.ParticipantAbilitySetEvent;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class AbstractTripleMix extends Game {

	public AbstractTripleMix(Collection<Player> players) {
		super(players);
	}

	@Override
	public void executeCommand(CommandType commandType, CommandSender sender, String command, String[] args, Plugin plugin) {
		switch (commandType) {
			case ABI: {
				if (args.length == 0) {
					Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util abi <대상/@a> [능력], [능력], [능력]");
					return;
				}
				final String[] names = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).split(",");
				if (names.length != 3) {
					Messager.sendErrorMessage(sender, "능력이 세 개 보다 많이 입력되었거나 적게 입력되었습니다.");
					Messager.sendErrorMessage(sender, "사용법 §7: §f/" + command + " util abi <대상/@a> [능력], [능력]");
					return;
				}
				names[0] = names[0].trim();
				names[1] = names[1].trim();
				names[2] = names[2].trim();
				if (AbilityList.isRegistered(names[0])) {
					if (AbilityList.isRegistered(names[1])) {
						if (AbilityList.isRegistered(names[2])) {
							if (args[0].equalsIgnoreCase("@a")) {
								try {
									for (MixParticipant participant : getParticipants()) {
										participant.getAbility().setAbility(AbilityList.getByString(names[0]), AbilityList.getByString(names[1]), AbilityList.getByString(names[2]));
									}
									Bukkit.broadcastMessage("§e" + sender.getName() + "§a님이 §f모든 참가자§a에게 능력을 임의로 부여하였습니다.");
								} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
									Messager.sendErrorMessage(sender, "능력 설정 도중 오류가 발생하였습니다.");
									if (DeveloperSettings.isEnabled()) e.printStackTrace();
								}
							} else {
								final Player targetPlayer = Bukkit.getPlayerExact(args[0]);
								if (targetPlayer != null) {
									if (isParticipating(targetPlayer)) {
										try {
											getParticipant(targetPlayer).getAbility().setAbility(AbilityList.getByString(names[0]), AbilityList.getByString(names[1]), AbilityList.getByString(names[2]));
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
							Messager.sendErrorMessage(sender, names[1] + KoreanUtil.getJosa(names[2], Josa.은는) + " 존재하지 않는 능력입니다.");
					} else
						Messager.sendErrorMessage(sender, names[1] + KoreanUtil.getJosa(names[1], Josa.은는) + " 존재하지 않는 능력입니다.");
				} else
					Messager.sendErrorMessage(sender, names[0] + KoreanUtil.getJosa(names[0], Josa.은는) + " 존재하지 않는 능력입니다.");
			}
			break;
			case ABLIST: {
				sender.sendMessage("§2===== §a능력자 목록 §2=====");
				int count = 0;
				for (Participant participant : GameManager.getGame().getParticipants()) {
					TripleMix mix = (TripleMix) participant.getAbility();
					if (mix.hasAbility()) {
						count++;
						sender.sendMessage("§e" + count + ". §f" + participant.getPlayer().getName() + " §7: §c" + mix.getFirst().getName() + " §f+ §c" + mix.getSecond().getName() + " §f+ §c" + mix.getThird().getName());
					}
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

	@Override
	public DeathManager newDeathManager() {
		return new DeathManager(this) {
			@Override
			protected String getRevealMessage(Participant victim) {
				final TripleMix mix = (TripleMix) victim.getAbility();
				if (mix.hasAbility()) {
					final String name = mix.getFirst().getName() + " + " + mix.getSecond().getName() + " + " + mix.getThird().getName();
					return "§f[§c능력§f] §c" + victim.getPlayer().getName() + "§f님의 능력은 §e" + name + "§f" + KoreanUtil.getJosa(name, Josa.이었였) + "습니다.";
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
			final TripleMix ability = getAbility();
			if (ability != null) {
				ability.removeAbility();
			}
			return null;
		}

		@Override
		public TripleMix getAbility() {
			if (!(this.ability instanceof TripleMix)) {
				try {
					if (this.ability != null) {
						this.ability.destroy();
					}
					this.ability = AbilityBase.create(TripleMix.class, this);
				} catch (IllegalAccessException | InvocationTargetException | InstantiationException ignored) {
				}
			}
			return (TripleMix) ability;
		}

		@Override
		public void setAbility(Class<? extends AbilityBase> abilityClass) throws IllegalAccessException, InstantiationException, InvocationTargetException {
			TripleMix mix = (TripleMix) this.ability;
			mix.setAbility(abilityClass, abilityClass, abilityClass);
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
			return Collections.unmodifiableCollection(participants.values());
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
