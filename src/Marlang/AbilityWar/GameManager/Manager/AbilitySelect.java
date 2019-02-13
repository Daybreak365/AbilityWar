package Marlang.AbilityWar.GameManager.Manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.TimerBase;

/**
 * 능력 선택
 * @author _Marlang 말랑
 */
abstract public class AbilitySelect extends TimerBase {
	
	protected ArrayList<Class<? extends AbilityBase>> Abilities = new ArrayList<Class<? extends AbilityBase>>();
	
	private HashMap<Player, Boolean> AbilitySelect = new HashMap<Player, Boolean>();
	
	public HashMap<Player, Boolean> getMap() {
		return AbilitySelect;
	}
	
	public boolean hasDecided(Player p) {
		return AbilitySelect.get(p);
	}
	
	private boolean setDecided(Player p, Boolean bool) {
		return AbilitySelect.put(p, bool);
	}
	
	public AbilitySelect() {
		for(Player p : setupPlayers()) {
			AbilitySelect.put(p, false);
		}
		
		this.drawAbility();
		this.StartTimer();
	}
	
	/**
	 * bool이 true면 일반 확정, false면 강제 확정
	 */
	public void decideAbility(Player p, Boolean bool) {
		if(AbilitySelect.containsKey(p)) {
			setDecided(p, true);
			
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
		for(Player p : AbilitySelect.keySet()) {
			if(!AbilitySelect.get(p)) {
				i++;
			}
		}
		
		return i;
	}
	
	public void Skip(String admin) {
		for(Player p : AbilitySelect.keySet()) {
			if(!AbilitySelect.get(p)) {
				decideAbility(p, false);
			}
		}

		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + admin + "&f님이 모든 플레이어의 능력을 강제로 확정시켰습니다."));
		this.StopTimer(false);
	}
	
	abstract protected void drawAbility();
	
	public abstract void changeAbility(Player p);
	
	abstract protected List<Player> setupPlayers();
	
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
	public void TimerStart(Data<?>... args) {}
	
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
	public void TimerEnd() {
		Ended = true;
	}
	
}
