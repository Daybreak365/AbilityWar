package daybreak.abilitywar.game.list.tnt;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.ParticipantStrategy;
import daybreak.abilitywar.game.list.tnt.ability.TNT;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.base.minecraft.version.NMSVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@GameManifest(name = "폭탄 돌리기", description = {

})

@Beta
public class TNTTag extends AbstractGame implements Listener {

	private final Set<TNTParticipant> tnts = new HashSet<>();

	public TNTTag() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
	}

	@Override
	protected void run(int count) {
		if (count == 1) {
			setRestricted(false);
			startGame();
			for (TNTParticipant participant : getParticipants()) {
				participant.setTNT(true);
			}
		}
	}

	@EventHandler
	private void onEntityDamage(final EntityDamageEvent e) {
		if (isParticipating(e.getEntity().getUniqueId())) {
			e.setDamage(0);
		}
	}

	@Override
	public Collection<TNTParticipant> getParticipants() {
		return ((TNTParticipantStrategy) participantStrategy).getParticipants();
	}

	@Override
	public TNTParticipant getParticipant(Player player) {
		return ((TNTParticipantStrategy) participantStrategy).getParticipant(player.getUniqueId());
	}

	@Override
	public TNTParticipant getParticipant(UUID uuid) {
		return ((TNTParticipantStrategy) participantStrategy).getParticipant(uuid);
	}

	@Override
	protected ParticipantStrategy newParticipantStrategy(Collection<Player> players) {
		return new TNTParticipantStrategy(players);
	}

	public class TNTParticipant extends Participant {

		private TNT ability;
		private final Attributes attributes = new Attributes();

		protected TNTParticipant(Player player) {
			super(player);
		}

		@Override
		public void setAbility(AbilityRegistration registration) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public TNT removeAbility() {
			return setTNT(false);
		}

		@Override
		public Attributes attributes() {
			return attributes;
		}

		public TNT setTNT(boolean tnt) {
			if (tnt) {
				removeAbility();
				final TNT ability = new TNT(this);
				ability.setRestricted(false);
				this.ability = ability;
				tnts.add(this);
				return ability;
			} else {
				final TNT ability = getAbility();
				if (ability != null) {
					ability.destroy();
					this.ability = null;
				}
				tnts.remove(this);
				return ability;
			}
		}

		public boolean isTNT() {
			return hasAbility();
		}

		@Override
		public boolean hasAbility() {
			return this.ability != null;
		}

		@Override
		public TNT getAbility() {
			return this.ability;
		}

		@Override
		public void setAbility(Class<? extends AbilityBase> abilityClass) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("능력을 임의로 부여할 수 없는 게임입니다.");
		}

	}

	protected class TNTParticipantStrategy implements ParticipantStrategy {

		private final Map<UUID, TNTParticipant> participants = new HashMap<>();

		public TNTParticipantStrategy(Collection<Player> players) {
			for (Player player : players) {
				participants.put(player.getUniqueId(), new TNTParticipant(player));
			}
		}

		@Override
		public Collection<TNTParticipant> getParticipants() {
			return Collections.unmodifiableCollection(participants.values());
		}

		@Override
		public boolean isParticipating(UUID uuid) {
			return participants.containsKey(uuid);
		}

		@Override
		public TNTParticipant getParticipant(UUID uuid) {
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
