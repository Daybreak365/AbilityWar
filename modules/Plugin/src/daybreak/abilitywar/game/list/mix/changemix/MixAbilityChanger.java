package daybreak.abilitywar.game.list.mix.changemix;

import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.changeability.AbilityChanger;
import daybreak.abilitywar.game.list.changeability.ChangeAbilityWar;
import daybreak.abilitywar.game.list.mix.AbstractMix;
import daybreak.abilitywar.game.list.mix.AbstractMix.MixParticipant;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Player;

public class MixAbilityChanger {

	private static final Logger logger = Logger.getLogger(AbilityChanger.class.getName());

	private static final Random random = new Random();

	private final AbstractMix game;
	private final int period;
	private final GameTimer timer;

	MixAbilityChanger(AbstractMix game) {
		this.game = game;
		this.period = ChangeAbilityWar.CHANGE_PERIOD.getValue();
		this.timer = game.new GameTimer(TaskType.INFINITE, -1) {
			@Override
			protected void run(int count) {
				changeAbility();
			}
		}.setPeriod(TimeUnit.TICKS, period * 20);
	}

	private List<AbilityRegistration> setupAbilities() {
		final List<AbilityRegistration> list = new ArrayList<>();
		for (final AbilityRegistration registration : AbilityList.values()) {
			if (!Settings.isBlacklisted(registration.getManifest().name())) {
				list.add(registration);
			}
		}
		return list;
	}

	private void notice(final Participant participant) {
		final Player player = participant.getPlayer();
		game.new GameTimer(TaskType.REVERSE, 11) {
			@Override
			protected void run(int count) {
				SoundLib.ENTITY_ITEM_PICKUP.playSound(player);
				if (count == 3 || count == 7) {
					SoundLib.PIANO.playInstrument(player, Note.natural(1, Tone.D));
					SoundLib.PIANO.playInstrument(player, Note.flat(1, Tone.F));
					SoundLib.PIANO.playInstrument(player, Note.natural(1, Tone.A));
				}
			}
		}.setPeriod(TimeUnit.TICKS, 2).start();

		game.new GameTimer(TaskType.REVERSE, 11) {
			@Override
			protected void run(int count) {
				int titleCount = 12 - count;
				String[] strings = {"", "", "능", "력", " ", "체", "인", "지", "!", "", ""};

				final StringBuilder builder = new StringBuilder();
				for (int i = 0; i < 11; i++) {
					if (i >= titleCount - 1 && i <= titleCount + 1) {
						builder.append(ChatColor.LIGHT_PURPLE + strings[i]);
					} else {
						builder.append(ChatColor.WHITE + strings[i]);
					}
				}

				NMS.sendTitle(player, builder.toString(), participant.getAbility().getRank().getRankName(), 0, 6, 40);
			}

			@Override
			protected void onEnd() {
				player.resetTitle();
			}
		}.setPeriod(TimeUnit.TICKS, 3).start();

		player.sendMessage("§d§l능력 §5§l체인지!");
		player.sendMessage(Formatter.formatAbilityInfo(participant.getAbility()).toArray(new String[0]));
	}

	/**
	 * 능력 체인지
	 */
	public void changeAbility() {
		final List<AbilityRegistration> abilities = setupAbilities();
		for (MixParticipant participant : game.getParticipants()) {
			if (!game.getDeathManager().isExcluded(participant.getPlayer())) {

				final AbilityRegistration first = abilities.get(random.nextInt(abilities.size())), second = abilities.get(random.nextInt(abilities.size()));
				try {
					participant.getAbility().setAbility(first.getAbilityClass(), second.getAbilityClass());
				} catch (Exception e) {
					logger.log(Level.SEVERE, participant.getPlayer().getName() + "님에게 능력을 할당하는 도중 오류가 발생하였습니다: " + first.getAbilityClass().getName() + " 또는 " + second.getAbilityClass().getName());
				}
			}
			notice(participant);
		}
	}

	public int getPeriod() {
		return period;
	}

	public void start() {
		this.timer.start();
	}

	public void stop() {
		this.timer.stop(false);
	}

}
