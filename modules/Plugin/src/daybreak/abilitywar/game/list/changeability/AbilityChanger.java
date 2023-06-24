package daybreak.abilitywar.game.list.changeability;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Flag;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.module.Module;
import daybreak.abilitywar.game.module.ModuleBase;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.logging.Logger;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.ChatColor;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

@ModuleBase(AbilityChanger.class)
public class AbilityChanger implements Module {

	private static final Logger logger = Logger.getLogger(AbilityChanger.class.getName());
	private static final Random random = new Random();

	private static final Note NOTE_D = Note.natural(1, Tone.D), NOTE_FLAT_F = Note.flat(1, Tone.F), NOTE_A = Note.natural(1, Tone.A);

	private final Game game;
	private final int period;
	private final GameTimer timer;
	private List<AbilityRegistration> abilities = setupAbilities();
	private int blacklistSize = Settings.getBlackList().size();

	AbilityChanger(Game game) {
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
			if (!Settings.isBlacklisted(registration.getManifest().name()) && (Settings.isUsingBetaAbility() || !registration.hasFlag(Flag.BETA))) {
				list.add(registration);
			}
		}
		return list;
	}

	/**
	 * 전체 플레이어 능력 변경
	 */
	public void changeAbility() {
		{
			final int blacklistSize = Settings.getBlackList().size();
			if (abilities == null || this.blacklistSize != blacklistSize) {
				this.blacklistSize = blacklistSize;
				this.abilities = setupAbilities();
			}
		}
		for (Participant participant : game.getParticipants()) {
			if (!game.getDeathManager().isExcluded(participant.getPlayer())) {

				final AbilityRegistration ability = abilities.get(random.nextInt(abilities.size()));
				try {
					participant.setAbility(ability.getAbilityClass());
				} catch (Exception e) {
					logger.log(Level.SEVERE, participant.getPlayer().getName() + "님에게 능력을 할당하는 도중 오류가 발생하였습니다: " + ability.getAbilityClass().getName());
				}
			}

			final AbilityBase ability = participant.getAbility();
			if (ability != null) {
				final Player player = participant.getPlayer();
				game.new GameTimer(TaskType.REVERSE, 11) {
					@Override
					protected void run(int count) {
						SoundLib.ENTITY_ITEM_PICKUP.playSound(player);
						if (count == 3 || count == 7) {
							SoundLib.PIANO.playInstrument(player, NOTE_D);
							SoundLib.PIANO.playInstrument(player, NOTE_FLAT_F);
							SoundLib.PIANO.playInstrument(player, NOTE_A);
						}
					}
				}.setPeriod(TimeUnit.TICKS, 2).start();

				game.new GameTimer(TaskType.REVERSE, 11) {
					@Override
					protected void run(int count) {
						final int titleCount = 12 - count;
						String[] strings = {"", "", "능", "력", " ", "체", "인", "지", "!", "", ""};

						final StringBuilder builder = new StringBuilder();
						for (int i = 0; i < 11; i++) {
							if (i >= titleCount - 1 && i <= titleCount + 1) {
								builder.append(ChatColor.LIGHT_PURPLE).append(strings[i]);
							} else {
								builder.append(ChatColor.WHITE).append(strings[i]);
							}
						}

						NMS.sendTitle(player, builder.toString(), ability.getRank().getRankName(), 0, 6, 40);
					}

					@Override
					protected void onEnd() {
						player.resetTitle();
					}
				}.setPeriod(TimeUnit.TICKS, 3).start();

				player.sendMessage("§d§l능력 §5§l체인지!");
				player.sendMessage(Formatter.formatAbilityInfo(ability).toArray(new String[0]));
			}
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

	@Override
	public void register() {}

	@Override
	public void unregister() {}

}
