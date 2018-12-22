package Marlang.AbilityWar.GameManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityList;
import Marlang.AbilityWar.Utils.AbilityWarThread;
import Marlang.AbilityWar.Utils.Messager;

/**
 * 능력 선택
 * @author _Marlang 말랑
 */
public class AbilitySelect extends Thread {
	
	HashMap<Player, Boolean> AbilitySelect = new HashMap<Player, Boolean>();

	ArrayList<AbilityBase> IdleAbilities = new ArrayList<AbilityBase>();
	
	public boolean getAbilitySelect(Player p) {
		return AbilitySelect.get(p);
	}
	
	public boolean setAbilitySelect(Player p, Boolean bool) {
		return AbilitySelect.put(p, bool);
	}
	
	public AbilitySelect(ArrayList<Player> Players) {
		for(Player p : Players) {
			AbilitySelect.put(p, false);
		}

		for(AbilityList l : AbilityList.values()) {
			IdleAbilities.add(l.getAbility());
		}
	}
	
	int AbilitySelectTime = 0;

	@Override
	public void run() {
		if(!isEveryoneReady()) {
			AbilitySelectTime++;
			AbilitySelectWarning(getAbilitySelectTime());
		} else {
			AbilityWarThread.toggleAbilitySelectTask(false);
		}
	}
	
	/**
	 * bool이 true면 일반 확정, false면 강제 확정
	 */
	public void decideAbility(Player p, Boolean bool) {
		if(AbilitySelect.containsKey(p)) {
			setAbilitySelect(p, true);
			
			if(bool) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6능력을 확정하셨습니다. 다른 플레이어를 기다려주세요."));
			} else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6능력이 강제로 확정되었습니다. 다른 플레이어를 기다려주세요."));
			}
			
			Messager.broadcastStringList(Messager.getArrayList(
					ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님이 능력을 확정하셨습니다."),
					ChatColor.translateAlternateColorCodes('&', "&a남은 인원 &7: &f" + getLeftPlayers() + "명")));
		}
	}
	
	public void Skip(Player admin) {
		for(Player p : AbilitySelect.keySet()) {
			if(!AbilitySelect.get(p)) {
				decideAbility(p, false);
			}
		}

		Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f관리자 &e" + admin.getName() + "&f님이 모든 플레이어의 능력을 강제로 확정시켰습니다."));
	}
	
	public void randomAbilityToAll() {
		Random random = new Random();
		
		for(Player p : AbilitySelect.keySet()) {
			AbilityBase Ability = IdleAbilities.get(random.nextInt(IdleAbilities.size()));
			IdleAbilities.remove(Ability);
			Ability.setPlayer(p);
			AbilityWarThread.getGame().getAbilities().put(p, Ability);
			Messager.sendStringList(p, Messager.getArrayList(
					ChatColor.translateAlternateColorCodes('&', "&a당신에게 능력이 할당되었습니다. &e/ability check&f로 확인 할 수 있습니다."),
					ChatColor.translateAlternateColorCodes('&', "&e/ability yes &f명령어를 사용하면 능력을 확정합니다."),
					ChatColor.translateAlternateColorCodes('&', "&e/ability no &f명령어를 사용하면 1회에 한해 능력을 변경할 수 있습니다.")));
		}
	}
	
	public void changeAbility(Player p) {
		Random random = new Random();
		
		AbilityBase oldAbility = AbilityWarThread.getGame().getAbilities().get(p);
		oldAbility.setPlayer(null);
		AbilityBase Ability = IdleAbilities.get(random.nextInt(IdleAbilities.size()));
		Ability.setPlayer(p);
		
		AbilityWarThread.getGame().getAbilities().put(p, Ability);
		
		Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&a당신의 능력이 변경되었습니다. &e/ability check&f로 확인 할 수 있습니다."));
		
		decideAbility(p, false);
		
		IdleAbilities.add(oldAbility);
		
	}
	
	public int getLeftPlayers() {
		int i = 0;
		for(Player p : AbilitySelect.keySet()) {
			if(!AbilitySelect.get(p)) {
				i++;
			}
		}
		
		return i;
	}
	
	public boolean isEveryoneReady() {
		boolean bool = true;
		for(Player Key : AbilitySelect.keySet()) {
			if(!AbilitySelect.get(Key)) {
				bool = false;
			}
		}
		
		return bool;
	}
	
	public void AbilitySelectWarning(Integer Time) {
		if(Time == 20) {
			Messager.broadcastStringList(Messager.getArrayList(
					ChatColor.translateAlternateColorCodes('&', "&c아직 모든 유저가 능력을 확정하지 않았습니다."),
					ChatColor.translateAlternateColorCodes('&', "&c/ability yes나 /ability no 명령어로 능력을 확정해주세요.")));
			setAbilitySelectTime(0);
		}
	}

	public void setAbilitySelectTime(int abilitySelectTime) {
		AbilitySelectTime = abilitySelectTime;
	}

	public int getAbilitySelectTime() {
		return AbilitySelectTime;
	}
	
}
