package DayBreak.AbilityWar.Game.Games.ChangeAbility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Player;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Config.AbilityWarSettings;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Game.Manager.AbilityList;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Library.Packet.TitlePacket;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

public class AbilityChanger {

	private AbstractGame game;
	
	private final int period;

	private final TimerBase timer;
	
	public AbilityChanger(AbstractGame game) {
		this.game = game;
		this.period = AbilityWarSettings.ChangeAbilityWar_getPeriod();
		this.timer = new TimerBase() {
			
			@Override
			protected void onStart() {}
			
			@Override
			protected void onEnd() {}
			
			@Override
			protected void TimerProcess(Integer Seconds) {
				ChangeAbility();
			}
		}.setPeriod(period * 20);
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
	
	private List<Participant> setupParticipants() {
		List<Participant> list = new ArrayList<>();
		for(Participant p : game.getParticipants()) {
			if(!game.getDeathManager().isEliminated(p.getPlayer())) {
				list.add(p);
			}
		}
		
		return list;
	}
	
	private void Notice(Participant participant) {
		Player p = participant.getPlayer();
		
		new TimerBase(11) {
			
			@Override
			protected void onStart() {}
			
			@Override
			protected void onEnd() {}
			
			@Override
			protected void TimerProcess(Integer Seconds) {
				SoundLib.ENTITY_ITEM_PICKUP.playSound(p);
				if(Seconds == 3 || Seconds == 7) {
					SoundLib.PIANO.playInstrument(p, Note.natural(1, Tone.D));
					SoundLib.PIANO.playInstrument(p, Note.flat(1, Tone.F));
					SoundLib.PIANO.playInstrument(p, Note.natural(1, Tone.A));
				}
			}
		}.setPeriod(2).StartTimer();
		
		new TimerBase(11) {
			
			@Override
			protected void onStart() {}
			
			@Override
			protected void onEnd() {
				TitlePacket packet = new TitlePacket("", "", 0, 1, 0);
				packet.Send(p);
			}
			
			@Override
			protected void TimerProcess(Integer Seconds) {
				int TitleCount = 12 - Seconds;
				String[] strs = {"", "", "능", "력", " ", "체", "인", "지", "!", "", ""};
				
				StringBuilder builder = new StringBuilder();
				for(int i = 0; i < 11; i++) {
					if(i == (TitleCount - 1) || i == TitleCount || i == (TitleCount + 1)) {
						builder.append(ChatColor.LIGHT_PURPLE + strs[i]);
					} else {
						builder.append(ChatColor.WHITE + strs[i]);
					}
				}
				
				TitlePacket packet = new TitlePacket(builder.toString(), participant.getAbility().getRank().getRankName(), 0, 6, 40);
				packet.Send(p);
			}
		}.setPeriod(3).StartTimer();
		
		Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&d&l능력 &5&l체인지!"));
		Messager.sendStringList(p, Messager.formatAbility(participant.getAbility()));
	}
	
	/**
	 * 능력 체인지
	 */
	public void ChangeAbility() {
		for(Participant participant : setupParticipants()) {
			Random random = new Random();
			Player p = participant.getPlayer();
			
			List<Class<? extends AbilityBase>> abilities = setupAbilities();
			
			Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
			try {
				participant.setAbility(abilityClass);
				abilities.remove(abilityClass);
				
				Notice(participant);
			} catch (Exception e) {
				Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님에게 능력을 할당하는 도중 오류가 발생하였습니다."));
				Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f문제가 발생한 능력: &b" + abilityClass.getName()));
			}
		}
	}
	
	public int getPeriod() {
		return period;
	}
	
	public void StartTimer() {
		this.timer.StartTimer();
	}
	
}
