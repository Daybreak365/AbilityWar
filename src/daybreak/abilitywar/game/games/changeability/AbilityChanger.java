package daybreak.abilitywar.game.games.changeability;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Player;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.AbilityWarSettings.Settings;
import daybreak.abilitywar.config.AbilityWarSettings.Settings.ChangeAbilityWarSettings;
import daybreak.abilitywar.game.games.defaultgame.Game;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.tItle.Title;
import daybreak.abilitywar.utils.thread.TimerBase;

public class AbilityChanger {

	private static final Logger logger = Logger.getLogger(AbilityChanger.class.getName());

	private final Game game;
	private final int period;
	private final TimerBase timer;
	
	AbilityChanger(Game game) {
		this.game = game;
		this.period = ChangeAbilityWarSettings.getPeriod();
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
			if(!Settings.isBlackListed(abilityName)) {
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
				Title packet = new Title("", "", 0, 1, 0);
				packet.sendTo(p);
			}
			
			@Override
			protected void TimerProcess(Integer Seconds) {
				int TitleCount = 12 - Seconds;
				String[] strings = {"", "", "능", "력", " ", "체", "인", "지", "!", "", ""};
				
				StringBuilder builder = new StringBuilder();
				for(int i = 0; i < 11; i++) {
					if(i >= TitleCount - 1 && i <= TitleCount + 1) {
						builder.append(ChatColor.LIGHT_PURPLE + strings[i]);
					} else {
						builder.append(ChatColor.WHITE + strings[i]);
					}
				}
				
				Title packet = new Title(builder.toString(), participant.getAbility().getRank().getRankName(), 0, 6, 40);
				packet.sendTo(p);
			}
		}.setPeriod(3).StartTimer();
		
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&l능력 &5&l체인지!"));
		p.sendMessage(Messager.formatAbilityInfo(participant.getAbility()));
	}
	
	/**
	 * 능력 체인지
	 */
	public void ChangeAbility() {
		for(Participant participant : setupParticipants()) {
			Random random = new Random();
			List<Class<? extends AbilityBase>> abilities = setupAbilities();
			
			Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
			try {
				participant.setAbility(abilityClass);
				abilities.remove(abilityClass);
				
				Notice(participant);
			} catch (Exception e) {
			    logger.log(Level.SEVERE, participant.getPlayer().getName() + "님에게 능력을 할당하는 도중 오류가 발생하였습니다: " + abilityClass.getName());
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
