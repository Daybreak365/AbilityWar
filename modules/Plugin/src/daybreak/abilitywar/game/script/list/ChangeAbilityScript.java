package daybreak.abilitywar.game.script.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.script.AbstractScript;
import daybreak.abilitywar.utils.base.logging.Logger;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChangeAbilityScript extends AbstractScript {

	private static final Logger logger = Logger.getLogger(ChangeAbilityScript.class);

	private final ChangeTarget target;

	public ChangeAbilityScript(String scriptName, int time, int loopCount, String preRunMessage, String runMessage, ChangeTarget target) {
		super(scriptName, time, loopCount, preRunMessage, runMessage);
		this.target = target;
	}

	@Override
	protected void execute(Game game) {
		List<Class<? extends AbilityBase>> abilities = setupAbilities();
		for (Participant participant : target.getParticipant(game)) {
			Random random = new Random();
			Player p = participant.getPlayer();

			Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
			try {
				participant.setAbility(abilityClass);
				p.sendMessage("§a당신의 능력이 변경되었습니다. §e/ability check§f로 확인 할 수 있습니다.");
			} catch (Exception e) {
				logger.error(ChatColor.YELLOW + participant.getPlayer().getName() + ChatColor.WHITE + "님에게 능력을 할당하는 도중 오류가 발생하였습니다.");
				logger.error("문제가 발생한 능력: " + ChatColor.AQUA + abilityClass.getName());
			}
		}
	}

	private List<Class<? extends AbilityBase>> setupAbilities() {
		List<Class<? extends AbilityBase>> list = new ArrayList<>();
		for (String abilityName : AbilityList.nameValues()) {
			if (!Settings.isBlacklisted(abilityName)) {
				list.add(AbilityList.getByString(abilityName));
			}
		}
		return list;
	}

	public enum ChangeTarget implements Serializable {

		모든_플레이어 {
			@Override
			public Collection<? extends Participant> getParticipant(Game game) {
				return game.getParticipants();
			}
		},
		랜덤_플레이어 {
			@Override
			public Collection<? extends Participant> getParticipant(Game game) {
				Random random = new Random();
				List<Participant> participants = new ArrayList<>(game.getParticipants());
				return Collections.singletonList(participants.get(random.nextInt(participants.size())));
			}
		};

		public abstract Collection<? extends Participant> getParticipant(Game game);

	}

}
