package DayBreak.AbilityWar.Game.Script.Types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Config.AbilityWarSettings;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Game.Manager.AbilityList;
import DayBreak.AbilityWar.Game.Script.Objects.AbstractScript;
import DayBreak.AbilityWar.Utils.Messager;

public class ChangeAbilityScript extends AbstractScript {

	private static final long serialVersionUID = 2520425818021196083L;

	private final ChangeTarget target;
	
	public ChangeAbilityScript(String ScriptName, int Time, boolean Loop, int LoopCount, String PreRunMessage, String RunMessage, ChangeTarget target) {
		super(ScriptName, Time, Loop, LoopCount, PreRunMessage, RunMessage);
		this.target = target;
	}

	public enum ChangeTarget implements Serializable {
		
		모든_플레이어 {
			@Override
			public List<Participant> getParticipant(AbstractGame game) {
				return game.getParticipants();
			}
		},
		랜덤_플레이어 {
			@Override
			public List<Participant> getParticipant(AbstractGame game) {
				Random random = new Random();
				List<Participant> participants = game.getParticipants();
				return Arrays.asList(participants.get(random.nextInt(participants.size())));
			}
		};
		
		public abstract List<Participant> getParticipant(AbstractGame game);
		
	}

	private List<Class<? extends AbilityBase>> setupAbilities() {
		List<Class<? extends AbilityBase>> list = new ArrayList<>();
		for(String abilityName : AbilityList.nameValues()) {
			if(!AbilityWarSettings.isBlackListed(abilityName)) {
				list.add(AbilityList.getByString(abilityName));
			}
		}
		
		return list;
	}
	
	@Override
	protected void Execute(AbstractGame game) {
		for(Participant participant : target.getParticipant(game)) {
			Random random = new Random();
			Player p = participant.getPlayer();
			
			List<Class<? extends AbilityBase>> Abilities = setupAbilities();
			
			Class<? extends AbilityBase> abilityClass = Abilities.get(random.nextInt(Abilities.size()));
			try {
				participant.setAbility(abilityClass);
				Abilities.remove(abilityClass);
				
				Messager.sendStringList(p, Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&a당신의 능력이 변경되었습니다. &e/ability check&f로 확인 할 수 있습니다.")));
			} catch (Exception e) {
				Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님에게 능력을 할당하는 도중 오류가 발생하였습니다."));
				Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f문제가 발생한 능력: &b" + abilityClass.getName()));
			}
		}
	}
	
}
