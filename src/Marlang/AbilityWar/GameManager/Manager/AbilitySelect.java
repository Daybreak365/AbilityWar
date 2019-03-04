package Marlang.AbilityWar.GameManager.Manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.GameManager.Object.Participant;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

/**
 * 능력 선택
 * @author _Marlang 말랑
 */
abstract public class AbilitySelect extends TimerBase {
	
	protected List<Class<? extends AbilityBase>> Abilities = setupAbilities();
	
	private HashMap<Participant, Boolean> AbilitySelect = new HashMap<Participant, Boolean>();
	
	public HashMap<Participant, Boolean> getMap() {
		return AbilitySelect;
	}

	public List<Participant> getSelectors() {
		return new ArrayList<Participant>(AbilitySelect.keySet());
	}
	
	public boolean hasDecided(Participant p) {
		return AbilitySelect.get(p);
	}
	
	private boolean setDecided(Participant p, Boolean bool) {
		return AbilitySelect.put(p, bool);
	}
	
	public AbilitySelect() {
		for(Participant p : setupPlayers()) {
			AbilitySelect.put(p, false);
		}
		
		this.drawAbility();
		this.StartTimer();
	}
	
	/**
	 * bool이 true면 일반 확정, false면 강제 확정
	 */
	public void decideAbility(Participant participant, Boolean bool) {
		Player p = participant.getPlayer();
		
		if(AbilitySelect.containsKey(participant)) {
			setDecided(participant, true);
			
			if(bool) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6능력을 확정하셨습니다. 다른 플레이어를 기다려주세요."));
				
			} else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6능력이 강제로 확정되었습니다. 다른 플레이어를 기다려주세요."));
			}
			
			Messager.broadcastStringList(Messager.getStringList(
					ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님이 능력을 확정하셨습니다."),
					ChatColor.translateAlternateColorCodes('&', "&a남은 인원 &7: &f" + getLeftPlayers() + "명")));
		}
	}

	private int getLeftPlayers() {
		int i = 0;
		for(Participant p : AbilitySelect.keySet()) {
			if(!AbilitySelect.get(p)) {
				i++;
			}
		}
		
		return i;
	}
	
	public void Skip(String admin) {
		for(Participant p : AbilitySelect.keySet()) {
			if(!AbilitySelect.get(p)) {
				decideAbility(p, false);
			}
		}

		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + admin + "&f님이 모든 플레이어의 능력을 강제로 확정시켰습니다."));
		this.StopTimer(false);
	}
	
	abstract protected void drawAbility();
	
	public abstract void changeAbility(Participant participant);
	
	abstract protected List<Participant> setupPlayers();
	
	abstract protected List<Class<? extends AbilityBase>> setupAbilities();
	
	abstract protected boolean endCondition();
	
	private boolean Ended = false;
	
	public boolean isEnded() {
		return Ended;
	}
	
	private int Count = 0;
	
	private void Warn() {
		if(Count >= 20) {
			Messager.broadcastStringList(Messager.getStringList(
					ChatColor.translateAlternateColorCodes('&', "&c아직 모든 유저가 능력을 확정하지 않았습니다."),
					ChatColor.translateAlternateColorCodes('&', "&c/ability yes나 /ability no 명령어로 능력을 확정해주세요.")));
			Count = 0;
		}
	}
	
	@Override
	public void onStart() {}
	
	@Override
	public void TimerProcess(Integer Seconds) {
		if(!endCondition()) {
			Count++;
			Warn();
		} else {
			this.StopTimer(false);
		}
	}
	
	@Override
	public void onEnd() {
		Ended = true;
	}
	
}
