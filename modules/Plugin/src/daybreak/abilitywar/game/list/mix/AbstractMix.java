package daybreak.abilitywar.game.list.mix;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.Configuration.Settings.AprilSettings;
import daybreak.abilitywar.config.kitpreset.KitConfiguration.KitSettings;
import daybreak.abilitywar.config.serializable.AbilityKit;
import daybreak.abilitywar.config.serializable.KitPreset;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.ParticipantStrategy;
import daybreak.abilitywar.game.event.participant.ParticipantAbilitySetEvent;
import daybreak.abilitywar.game.list.mix.gui.MixTipGUI;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.list.mix.synergy.SynergyFactory;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.gui.tip.AbilityTipGUI;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractMix extends Game implements DefaultKitHandler {

	public AbstractMix(Collection<Player> players) {
		super(players);
	}

	@Override
	public void giveDefaultKit(Participant participant) {
		if (!participant.getGame().equals(this)) return;
		final Player player = participant.getPlayer();
		player.setLevel(0);
		if (Configuration.Settings.getStartLevel() > 0) {
			player.giveExpLevels(Configuration.Settings.getStartLevel());
			SoundLib.ENTITY_PLAYER_LEVELUP.playSound(player);
		}
		final PlayerInventory inventory = player.getInventory();
		if (Configuration.Settings.getInventoryClear()) {
			inventory.clear();
		}

		final Mix mix = (Mix) participant.getAbility();
		if (mix.hasAbility()) {
			final AbilityKit abilityKit = KitSettings.getAbilityKit();
			if (mix.hasSynergy()) {
				final KitPreset kit = abilityKit.getKits(mix.getSynergy().getClass().getName());
				for (ItemStack stack : kit.getItems()) {
					inventory.addItem(stack);
				}
				inventory.setHelmet(kit.getHelmet());
				inventory.setChestplate(kit.getChestplate());
				inventory.setLeggings(kit.getLeggings());
				inventory.setBoots(kit.getBoots());
			} else {
				final String firstName = mix.getFirst().getClass().getName(), secondName = mix.getSecond().getClass().getName();
				final boolean hasFirst = abilityKit.hasKits(firstName), hasSecond = abilityKit.hasKits(secondName);
				if (hasFirst || hasSecond) {
					if (hasFirst && hasSecond) {
						final KitPreset first = abilityKit.getKits(firstName), second = abilityKit.getKits(secondName);
						final Map<Integer, Integer> hashCodes = new HashMap<>();
						for (final ItemStack stack : first.getItems()) {
							inventory.addItem(stack);
							final int hashCode = ItemLib.hashCode(stack);
							hashCodes.put(hashCode, hashCodes.getOrDefault(hashCode, 0) + 1);
						}
						for (final ItemStack stack : second.getItems()) {
							final int hashCode = ItemLib.hashCode(stack);
							if (hashCodes.containsKey(hashCode)) {
								hashCodes.put(hashCode, hashCodes.get(hashCode) - 1);
								if (hashCodes.get(hashCode) > 0) {
									continue;
								} else {
									hashCodes.remove(hashCode);
								}
							}
							inventory.addItem(stack);
						}
						inventory.setHelmet(first.getHelmet());
						inventory.setChestplate(first.getChestplate());
						inventory.setLeggings(first.getLeggings());
						inventory.setBoots(first.getBoots());
					} else {
						if (hasFirst) {
							giveKit(inventory, abilityKit.getKits(firstName));
						} else {
							giveKit(inventory, abilityKit.getKits(secondName));
						}
					}
				} else {
					giveKit(inventory, KitSettings.getKit());
				}
			}
		} else {
			giveKit(inventory, KitSettings.getKit());
		}
	}

	private void giveKit(PlayerInventory inventory, KitPreset kit) {
		for (ItemStack stack : kit.getItems()) {
			inventory.addItem(stack);
		}
		inventory.setHelmet(kit.getHelmet());
		inventory.setChestplate(kit.getChestplate());
		inventory.setLeggings(kit.getLeggings());
		inventory.setBoots(kit.getBoots());
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
							new MixAbilityGUI(player, this, plugin).openGUI(1);
						} else {
							Player targetPlayer = Bukkit.getPlayerExact(args[0]);
							if (targetPlayer != null) {
								AbstractGame game = GameManager.getGame();
								if (game.isParticipating(targetPlayer)) {
									new MixAbilityGUI(player, game.getParticipant(targetPlayer), this, plugin).openGUI(1);
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
								} catch (ReflectiveOperationException e) {
									Messager.sendErrorMessage(sender, "능력 설정 도중 오류가 발생하였습니다.");
									if (AprilSettings.isEnabled()) e.printStackTrace();
								}
							} else {
								Player targetPlayer = Bukkit.getPlayerExact(args[0]);
								if (targetPlayer != null) {
									if (isParticipating(targetPlayer)) {
										try {
											getParticipant(targetPlayer).getAbility().setAbility(AbilityList.getByString(names[0]), AbilityList.getByString(names[1]));
											Bukkit.broadcastMessage("§e" + sender.getName() + "§a님이 §f" + targetPlayer.getName() + "§a님에게 능력을 임의로 부여하였습니다.");
										} catch (ReflectiveOperationException e) {
											Messager.sendErrorMessage(sender, "능력 설정 도중 오류가 발생하였습니다.");
											if (AprilSettings.isEnabled()) e.printStackTrace();
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
			case TIP_CHECK: {
				final Player player = (Player) sender;
				if (GameManager.isGameRunning()) {
					final AbstractGame game = GameManager.getGame();
					if (game.isParticipating(player)) {
						final MixParticipant participant = getParticipant(player);
						final Mix mix = participant.getAbility();
						if (mix.hasAbility()) {
							if (mix.hasSynergy()) {
								new AbilityTipGUI(player, mix.getSynergy().getRegistration(), plugin).openGUI(1);
							} else {
								new MixTipGUI(player, mix, plugin).openGUI();
							}
						} else {
							Messager.sendErrorMessage(sender, "능력이 할당되지 않았습니다. §8(§7/aw abtip <능력> 명령어를 사용하세요.§8)");
						}
					} else {
						Messager.sendErrorMessage(sender, "게임에 참가하고 있지 않습니다. §8(§7/aw abtip <능력> 명령어를 사용하세요.§8)");
					}
				} else {
					Messager.sendErrorMessage(sender, "게임이 진행되고 있지 않습니다. §8(§7/aw abtip <능력> 명령어를 사용하세요.§8)");
				}
			}
			break;
			default:
				super.executeCommand(commandType, sender, command, args, plugin);
				break;
		}
	}

	@Override
	protected @NotNull DeathManager newDeathManager() {
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
	protected ParticipantStrategy newParticipantStrategy(Collection<Player> players) {
		return new MixParticipantStrategy(players);
	}

	public class MixParticipant extends Participant {

		private final Mix ability = new Mix(this);
		private final Attributes attributes = new Attributes();

		protected MixParticipant(Player player) {
			super(player);
		}

		@Override
		public void setAbility(AbilityRegistration registration) throws ReflectiveOperationException {
			ability.setAbility(registration, registration);
			Bukkit.getPluginManager().callEvent(new ParticipantAbilitySetEvent(this, ability, ability));
		}

		@Override
		public AbilityBase removeAbility() {
			ability.removeAbility();
			return null;
		}

		@Override
		public Attributes attributes() {
			return attributes;
		}

		@Override
		public Mix getAbility() {
			return ability;
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
			participants.putIfAbsent(player.getUniqueId().toString(), new MixParticipant(player));
		}

		@Override
		public void removeParticipant(UUID uuid) throws UnsupportedOperationException {
			participants.remove(uuid.toString());
		}

	}

}
