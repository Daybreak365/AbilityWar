package Marlang.AbilityWar.GameManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityList;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.Utils.AbilityWarThread;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.TimerBase;

/**
 * 능력 선택
 * @author _Marlang 말랑
 */
public class AbilitySelect extends TimerBase {
	
	public HashMap<Player, Boolean> AbilitySelect = new HashMap<Player, Boolean>();

	ArrayList<AbilityBase> IdleAbilities = new ArrayList<AbilityBase>();
	
	public boolean getAbilitySelect(Player p) {
		return AbilitySelect.get(p);
	}
	
	public boolean setAbilitySelect(Player p, Boolean bool) {
		return AbilitySelect.put(p, bool);
	}
	
	private Game game;
	
	public AbilitySelect(ArrayList<Player> Players, Game game) {
		for(Player p : Players) {
			AbilitySelect.put(p, false);
		}
		
		this.game = game;
	}
	
	int AbilitySelectTime = 0;
	
	/**
	 * bool이 true면 일반 확정, false면 강제 확정
	 */
	public void decideAbility(Player p, Boolean bool) {
		if(AbilitySelect.containsKey(p)) {
			setAbilitySelect(p, true);
			
			if(bool) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6능력을 확정하셨습니다. 다른 플레이어를 기다려주세요."));
				
				Messager.broadcastStringList(Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님이 능력을 확정하셨습니다."),
						ChatColor.translateAlternateColorCodes('&', "&a남은 인원 &7: &f" + getLeftPlayers() + "명")));
			} else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6능력이 강제로 확정되었습니다. 다른 플레이어를 기다려주세요."));
			}
		}
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
	
	/**
	 * 전체 능력 추첨
	 */
	public void randomAbilityToAll() {
		IdleAbilities.clear();
		
		for(String name : AbilityList.values()) {
			try {
				if(!AbilityWarSettings.getBlackList().contains(name)) {
					IdleAbilities.add(AbilityList.getByString(name).newInstance());
				}
			} catch (InstantiationException | IllegalAccessException e) {}
		}
		
		if(AbilitySelect.keySet().size() <= IdleAbilities.size()) {
			Random random = new Random();
			
			for(Player p : AbilitySelect.keySet()) {
				AbilityBase Ability = IdleAbilities.get(random.nextInt(IdleAbilities.size()));
				IdleAbilities.remove(Ability);
				
				Ability.setPlayer(p);
				game.addAbility(Ability);
				
				Messager.sendStringList(p, Messager.getStringList(
						ChatColor.translateAlternateColorCodes('&', "&a당신에게 능력이 할당되었습니다. &e/ability check&f로 확인 할 수 있습니다."),
						ChatColor.translateAlternateColorCodes('&', "&e/ability yes &f명령어를 사용하면 능력을 확정합니다."),
						ChatColor.translateAlternateColorCodes('&', "&e/ability no &f명령어를 사용하면 1회에 한해 능력을 변경할 수 있습니다.")));
			}
		} else {
			Messager.broadcastErrorMessage("사용 가능한 능력의 수가 플레이어의 수보다 적어 게임을 종료합니다.");
			this.StopTimer(true);
			AbilityWarThread.toggleGameTask(false);
			AbilityWarThread.setGame(null);
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임이 초기화되었습니다."));
		}
	}
	
	/**
	 * 유저 능력 재추첨
	 */
	public void changeAbility(Player p) {
		if(IdleAbilities.size() > 0) {
			Random random = new Random();
			
			AbilityBase oldAbility = game.getAbilities().get(p);
			if(oldAbility != null) {
				AbilityBase Ability = IdleAbilities.get(random.nextInt(IdleAbilities.size()));
				IdleAbilities.remove(Ability);
				Ability.setPlayer(p);
				
				game.removeAbility(p);
				game.addAbility(Ability);
				
				Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&a당신의 능력이 변경되었습니다. &e/ability check&f로 확인 할 수 있습니다."));
				
				decideAbility(p, false);

				try {
					IdleAbilities.add(oldAbility.getClass().newInstance());
				} catch(Exception ex) {}
			}
		} else {
			Messager.sendErrorMessage(p, "능력을 변경할 수 없습니다.");
		}
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
			Messager.broadcastStringList(Messager.getStringList(
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
	
	boolean AbilitySelectFinished = false;
	
	public boolean isAbilitySelectFinished() {
		return AbilitySelectFinished;
	}
	
	@Override
	public void TimerStart() {
		
	}

	@Override
	public void TimerProcess(Integer Seconds) {
		if(!isEveryoneReady()) {
			AbilitySelectTime++;
			AbilitySelectWarning(getAbilitySelectTime());
		} else {
			this.StopTimer(false);
		}
	}

	@Override
	public void TimerEnd() {
		AbilitySelectFinished = true;
	}
	
}
