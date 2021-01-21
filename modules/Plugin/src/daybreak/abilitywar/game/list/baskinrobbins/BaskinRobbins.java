package daybreak.abilitywar.game.list.baskinrobbins;

import com.google.common.base.Strings;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.Category;
import daybreak.abilitywar.game.Category.GameCategory;
import daybreak.abilitywar.game.GameAliases;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.ParticipantStrategy;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

@GameManifest(name = "배스킨 라빈스 31", description = {
		"§f어디선가 많이 본 게임!"
})
@GameAliases({
		"배라"
})
@Category(GameCategory.MINIGAME)
@Beta
public class BaskinRobbins extends AbstractGame {

	private static final Random random = new Random();
	private boolean tutorial = true;
	private final GameManager turnManager;

	public BaskinRobbins(final String[] args) throws IllegalArgumentException {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		if (args.length != 0 && "speed".equals(args[0])) this.tutorial = false;
		this.turnManager = new GameManager();
	}

	@Override
	protected void run(int count) {
		switch (count) {
			case 1: {
				Bukkit.broadcastMessage(Formatter.formatTitle(36, ChatColor.DARK_PURPLE, ChatColor.WHITE, "게임 참가자"));
				final Collection<? extends Participant> participants = getParticipants();
				{
					int i = 0;
					final StringJoiner joiner = new StringJoiner(", ");
					for (Participant participant : participants) {
						joiner.add(participant.getPlayer().getName());
						if (++i > 10) {
							joiner.add("...");
						}
					}
					Bukkit.broadcastMessage(joiner.toString());
				}
				Bukkit.broadcastMessage("§5총 인원수§f: §d" + participants.size() + "명");
				Bukkit.broadcastMessage("§5=====================================");
				if (getParticipants().size() < 2) {
					stop();
					Bukkit.broadcastMessage("§c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. §8(§72명§8)");
					return;
				}
				Bukkit.broadcastMessage("§5BaskinRobbins §f- §d배스킨 라빈스 31");
				Bukkit.broadcastMessage("§e버전 §7: §f" + AbilityWar.getPlugin().getDescription().getVersion());
				Bukkit.broadcastMessage("§b개발자 §7: §fDaybreak 새벽");
				Bukkit.broadcastMessage("§9디스코드 §7: §f새벽§7#5908");
				break;
			}
			case 2: {
				if (!tutorial) {
					setCount(11);
				}
				break;
			}
			case 3: {
				Bukkit.broadcastMessage("§d간단§f하게 게임 설명해드리겠습니다!");
				Bukkit.broadcastMessage("§8(§f/aw start speed §7명령어로 게임을 시작해 스킵할 수 있습니다.§8)");
				break;
			}
			case 4: {
				Bukkit.broadcastMessage("§f게임이 시작되면 §5플레이어 턴 순서§f가 §d랜덤§f으로 정해집니다.");
				break;
			}
			case 5: {
				Bukkit.broadcastMessage("§f자신의 §5턴§f이 오면");
				break;
			}
			case 6: {
				Bukkit.broadcastMessage("§d한 번 §f웅크리거나,");
				for (BaskinParticipant participant : getParticipants()) {
					final Player player = participant.getPlayer();
					player.setSneaking(true);
					NMS.sendTitle(player, "1", "카운트 §8(§7목표 숫자§f: X§8)", 0, 40, 10);
					SoundLib.UI_BUTTON_CLICK.playSound(player);
				}
				new BukkitRunnable() {
					@Override
					public void run() {
						for (BaskinParticipant participant : getParticipants()) {
							participant.getPlayer().setSneaking(false);
						}
					}
				}.runTaskLater(AbilityWar.getPlugin(), 2);
				break;
			}
			case 7: {
				Bukkit.broadcastMessage("§d두 번 §f웅크리거나,");
				new GameTimer(TaskType.NORMAL, 2) {
					@Override
					protected void run(int count) {
						for (BaskinParticipant participant : getParticipants()) {
							final Player player = participant.getPlayer();
							player.setSneaking(true);
							NMS.sendTitle(player, String.valueOf(1 + count), "카운트 §8(§7목표 숫자§f: X§8)", 0, 40, 10);
							SoundLib.UI_BUTTON_CLICK.playSound(player);
						}
						new BukkitRunnable() {
							@Override
							public void run() {
								for (BaskinParticipant participant : getParticipants()) {
									participant.getPlayer().setSneaking(false);
								}
							}
						}.runTaskLater(AbilityWar.getPlugin(), 2);
					}
				}.setPeriod(TimeUnit.TICKS, 7).start();
				break;
			}
			case 8: {
				Bukkit.broadcastMessage("§d세 번 §f웅크려서 §5카운트§f를 올릴 수 있습니다.");
				new GameTimer(TaskType.NORMAL, 3) {
					@Override
					protected void run(int count) {
						for (BaskinParticipant participant : getParticipants()) {
							final Player player = participant.getPlayer();
							player.setSneaking(true);
							NMS.sendTitle(player, String.valueOf(3 + count), "카운트 §8(§7목표 숫자§f: X§8)", 0, 40, 10);
							SoundLib.UI_BUTTON_CLICK.playSound(player);
						}
						new BukkitRunnable() {
							@Override
							public void run() {
								for (BaskinParticipant participant : getParticipants()) {
									participant.getPlayer().setSneaking(false);
								}
							}
						}.runTaskLater(AbilityWar.getPlugin(), 2);
					}
				}.setPeriod(TimeUnit.TICKS, 7).start();
				break;
			}
			case 10: {
				Bukkit.broadcastMessage("§f자신의 §d턴§f에 §d목표 숫자§f에 도달하거나, 자신의 §d턴§f이 시작됐을 때");
				Bukkit.broadcastMessage("§d목표 숫자§f까지 §f하나의 카운트밖에 남지 않은 경우 §5탈락§f합니다.");
				new GameTimer(TaskType.NORMAL, 31) {
					@Override
					protected void run(int count) {
						for (BaskinParticipant participant : getParticipants()) {
							final Player player = participant.getPlayer();
							NMS.sendTitle(player, String.valueOf(count), "카운트 §8(§7목표 숫자§f: 31§8)", 0, 40, 10);
							SoundLib.UI_BUTTON_CLICK.playSound(player);
						}
					}
					@Override
					protected void onEnd() {
						for (BaskinParticipant participant : getParticipants()) {
							final Player player = participant.getPlayer();
							NMS.sendTitle(player, "31", "§c이러면 탈락입니다!", 0, 40, 10);
							SoundLib.ENTITY_GENERIC_EXPLODE.playSound(player);
						}
					}
				}.setPeriod(TimeUnit.TICKS, 1).start();
				break;
			}
			case 12: {
				startGame();
				turnManager.reset();
				turnManager.start();
				break;
			}
		}
	}

	public void eliminate(final BaskinParticipant target) {
		final Player targetPlayer = target.getPlayer();
		targetPlayer.setHealth(0);
		for (BaskinParticipant participant : getParticipants()) {
			final Player player = participant.getPlayer();
			NMS.sendTitle(player,  "§c" + targetPlayer.getName(), "탈락했습니다!", 0, Integer.MAX_VALUE, 0);
			SoundLib.ENTITY_GENERIC_EXPLODE.playSound(player);
		}
		turnManager.turn.remove(target);
		this.turnManager.currentTurn++;
	}

	public class GameManager extends GameTimer implements Listener {

		private final int maxCount = 31;
		private int count = 0, currentTurn = -1;
		private BaskinParticipant current = null, next;
		private final List<BaskinParticipant> turn;

		private int streak = 0;

		public BaskinParticipant getTurn(final int turn) {
			if (turn < 0) throw new IllegalArgumentException();
			if (this.turn.isEmpty()) throw new IllegalStateException();
			return this.turn.get(turn % this.turn.size());
		}

		public void nextTurn() {
			currentTurn++;
			final BaskinParticipant next = getTurn(currentTurn + 1), participant = this.next;
			if (participant != null) {
				this.current = participant;
			} else {
				this.current = getTurn(currentTurn);
			}
			this.next = next;
		}

		private GameManager() {
			super(TaskType.REVERSE, 15);
			setPeriod(TimeUnit.TICKS, 4);
			this.turn = new ArrayList<>(getParticipants());
		}

		public void count() {
			if (++count >= maxCount) {
				eliminate(current);
				stop(true);
				return;
			}
			streak++;
			if (count == current.deathNumber) {
				eliminate(current);
				this.current = null;
			}
		}

		@EventHandler
		private void onPlayerToggleSneak(final PlayerToggleSneakEvent e) {
			if (!e.isSneaking()) return;
			final BaskinParticipant current = this.current;
			if (current != null && e.getPlayer().equals(current.getPlayer())) {
				if (streak >= 3) {
					return;
				}
				setCount(10);
				count();
				updateTitle();
			}
		}

		public void updateTitle() {
			for (BaskinParticipant participant : getParticipants()) {
				final Player player = participant.getPlayer();
				final String streak = "§2" + Strings.repeat("|", this.streak);
				NMS.sendTitle(player,  streak + " " + (participant.equals(current) ? "§a" : "§7") + count + " " + streak, "§8(§7목표 숫자§f: " + maxCount + "§8) §4(§c죽음의 숫자§f: " + participant.deathNumber + "§4)", 0, Integer.MAX_VALUE, 0);
				SoundLib.UI_BUTTON_CLICK.playSound(player);
			}
		}

		public void reset() {
			this.currentTurn = -1;
			this.current = null;
			Collections.shuffle(turn);
			for (BaskinParticipant participant : getParticipants()) {
				participant.reset(this);
			}
		}

		@Override
		protected void onStart() {
			nextTurn();
			this.streak = 0;
			if (count >= maxCount) {
				stop(true);
				return;
			}
			if (count == current.deathNumber) {
				eliminate(current);
				this.current = null;
				return;
			}
			updateTitle();
			final StringJoiner joiner = new StringJoiner(" §f-> ");
			joiner.add("§2" + this.current.getPlayer().getName());
			for (int i = 1; i <= 2; i++) {
				joiner.add("§7" + getTurn(currentTurn + i).getPlayer().getName());
			}
			Bukkit.broadcastMessage(joiner.toString());
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@Override
		protected void run(int count) {
			if (current != null) {
				current.timeChannel.update("§d" + (count / 5.0) + "§f초 남음");
			}
		}

		@Override
		protected void onCountSet() {
			if (current != null) {
				current.timeChannel.update("§d" + (getCount() / 5.0) + "§f초 남음");
			}
		}

		@Override
		protected void onEnd() {
			HandlerList.unregisterAll(this);
			if (current != null) {
				current.timeChannel.update(null);
				if (streak == 0) {
					count();
				}
			}
			start();
		}

		@Override
		protected void onSilentEnd() {
			HandlerList.unregisterAll(this);
			if (current != null) {
				current.timeChannel.update(null);
			}
		}
	}

	@Override
	public Collection<BaskinParticipant> getParticipants() {
		return ((BaskinParticipantStrategy) participantStrategy).getParticipants();
	}

	@Override
	public BaskinParticipant getParticipant(Player player) {
		return ((BaskinParticipantStrategy) participantStrategy).getParticipant(player.getUniqueId());
	}

	@Override
	public BaskinParticipant getParticipant(UUID uuid) {
		return ((BaskinParticipantStrategy) participantStrategy).getParticipant(uuid);
	}

	@Override
	protected ParticipantStrategy newParticipantStrategy(Collection<Player> players) {
		return new BaskinParticipantStrategy(players);
	}

	public class BaskinParticipant extends Participant {

		private final Attributes attributes = new Attributes();
		private final ActionbarChannel timeChannel = actionbar().newChannel();
		private int deathNumber;

		protected BaskinParticipant(Player player) {
			super(player);
		}

		public void reset(final GameManager turnManager) {// 31 -> 2 ~ 30
			this.deathNumber = random.nextInt(turnManager.maxCount - 2) + 2;
		}

		@Override
		public void setAbility(AbilityRegistration registration) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public AbilityBase removeAbility() {
			return null;
		}

		@Override
		public Attributes attributes() {
			return attributes;
		}

		@Override
		public boolean hasAbility() {
			return false;
		}

		@Override
		public AbilityBase getAbility() {
			return null;
		}

		@Override
		public void setAbility(Class<? extends AbilityBase> abilityClass) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("능력을 임의로 부여할 수 없는 게임입니다.");
		}

	}

	protected class BaskinParticipantStrategy implements ParticipantStrategy {

		private final Map<UUID, BaskinParticipant> participants = new HashMap<>();

		public BaskinParticipantStrategy(Collection<Player> players) {
			for (Player player : players) {
				participants.put(player.getUniqueId(), new BaskinParticipant(player));
			}
		}

		@Override
		public Collection<BaskinParticipant> getParticipants() {
			return Collections.unmodifiableCollection(participants.values());
		}

		@Override
		public boolean isParticipating(UUID uuid) {
			return participants.containsKey(uuid);
		}

		@Override
		public BaskinParticipant getParticipant(UUID uuid) {
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

	@Override
	public void executeCommand(CommandType commandType, CommandSender sender, String command, String[] args, Plugin plugin) {
		if (commandType == CommandType.ABI) {
			sender.sendMessage(ChatColor.RED + "이 게임모드에서 사용할 수 없는 명령어입니다.");
		} else {
			super.executeCommand(commandType, sender, command, args, plugin);
		}
	}

	@Override
	protected void onEnd() {
		HandlerList.unregisterAll(this);
		super.onEnd();
	}
}
