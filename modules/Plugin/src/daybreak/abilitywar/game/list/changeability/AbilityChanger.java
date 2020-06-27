package daybreak.abilitywar.game.list.changeability;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.Configuration.Settings.ChangeAbilityWarSettings;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Player;

public class AbilityChanger {

	private static final Logger logger = Logger.getLogger(AbilityChanger.class.getName());

	private final Game game;
	private final int period;
	private final GameTimer timer;

	AbilityChanger(Game game) {
		this.game = game;
		this.period = ChangeAbilityWarSettings.getPeriod();
		this.timer = game.new GameTimer(TaskType.INFINITE, -1) {
			@Override
			protected void run(int count) {
				ChangeAbility();
			}
		}.setPeriod(TimeUnit.TICKS, period * 20);
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

	private List<Participant> setupParticipants() {
		List<Participant> list = new ArrayList<>();
		for (Participant p : game.getParticipants()) {
			if (!game.getDeathManager().isExcluded(p.getPlayer())) {
				list.add(p);
			}
		}

		return list;
	}

	private void Notice(Participant participant) {
		Player p = participant.getPlayer();

		game.new GameTimer(TaskType.REVERSE, 11) {
			@Override
			protected void run(int count) {
				SoundLib.ENTITY_ITEM_PICKUP.playSound(p);
				if (count == 3 || count == 7) {
					SoundLib.PIANO.playInstrument(p, Note.natural(1, Tone.D));
					SoundLib.PIANO.playInstrument(p, Note.flat(1, Tone.F));
					SoundLib.PIANO.playInstrument(p, Note.natural(1, Tone.A));
				}
			}
		}.setPeriod(TimeUnit.TICKS, 2).start();

		game.new GameTimer(TaskType.REVERSE, 11) {
			@Override
			protected void run(int count) {
				int TitleCount = 12 - count;
				String[] strings = {"", "", "능", "력", " ", "체", "인", "지", "!", "", ""};

				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < 11; i++) {
					if (i >= TitleCount - 1 && i <= TitleCount + 1) {
						builder.append(ChatColor.LIGHT_PURPLE + strings[i]);
					} else {
						builder.append(ChatColor.WHITE + strings[i]);
					}
				}

				p.sendTitle(builder.toString(), participant.getAbility().getRank().getRankName(), 0, 6, 40);
			}

			@Override
			protected void onEnd() {
				p.resetTitle();
			}
		}.setPeriod(TimeUnit.TICKS, 3).start();

		p.sendMessage("§d§l능력 §5§l체인지!");
		p.sendMessage(Formatter.formatAbilityInfo(participant.getAbility()).toArray(new String[0]));
	}

	/**
	 * 능력 체인지
	 */
	public void ChangeAbility() {
		for (Participant participant : setupParticipants()) {
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

	public void start() {
		this.timer.start();
	}

}
