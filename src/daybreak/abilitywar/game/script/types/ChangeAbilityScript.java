package daybreak.abilitywar.game.script.types;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.games.standard.Game;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.script.objects.AbstractScript;
import daybreak.abilitywar.utils.Messager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChangeAbilityScript extends AbstractScript {

	private final ChangeTarget target;

	public ChangeAbilityScript(String ScriptName, int Time, int LoopCount, String PreRunMessage, String RunMessage, ChangeTarget target) {
		super(ScriptName, Time, LoopCount, PreRunMessage, RunMessage);
		this.target = target;
	}

	public enum ChangeTarget implements Serializable {

		모든_플레이어 {
			@Override
			public Collection<Participant> getParticipant(Game game) {
				return game.getParticipants();
			}
		},
		랜덤_플레이어 {
			@Override
			public Collection<Participant> getParticipant(Game game) {
				Random random = new Random();
				List<Participant> participants = new ArrayList<>(game.getParticipants());
				return Collections.singletonList(participants.get(random.nextInt(participants.size())));
			}
		};

		public abstract Collection<Participant> getParticipant(Game game);

	}

	private List<Class<? extends AbilityBase>> setupAbilities() {
		List<Class<? extends AbilityBase>> list = new ArrayList<>();
		for (String abilityName : AbilityList.nameValues()) {
			if (!Settings.isBlackListed(abilityName)) {
				list.add(AbilityList.getByString(abilityName));
			}
		}

		return list;
	}

	@Override
	protected void Execute(Game game) {
		for (Participant participant : target.getParticipant(game)) {
			Random random = new Random();
			Player p = participant.getPlayer();

			List<Class<? extends AbilityBase>> Abilities = setupAbilities();

			Class<? extends AbilityBase> abilityClass = Abilities.get(random.nextInt(Abilities.size()));
			try {
				participant.setAbility(abilityClass);
				Abilities.remove(abilityClass);

				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a당신의 능력이 변경되었습니다. &e/ability check&f로 확인 할 수 있습니다."));
			} catch (Exception e) {
				Messager.sendConsoleErrorMessage(
						ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님에게 능력을 할당하는 도중 오류가 발생하였습니다."),
						ChatColor.translateAlternateColorCodes('&', "&f문제가 발생한 능력: &b" + abilityClass.getName()));
			}
		}
	}

}
