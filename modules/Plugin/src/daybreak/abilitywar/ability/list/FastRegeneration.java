package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.SoundLib;
import kotlin.ranges.RangesKt;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.jetbrains.annotations.NotNull;

@AbilityManifest(name = "빠른 회복", rank = Rank.A, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 빠른 회복 능력을 사용합니다. $[COOLDOWN_CONFIG]",
		"능력 사용 중 체력을 빠르게 회복하며, 체력이 적을 수록 더 빨리 회복합니다."
})
@Tips(tip = {
		"전투가 시작하기 전에, 전투 중에, 전투가 끝난 후에 등 언제",
		"써도 좋은 능력입니다. 전투 전에 사용할 경우 체력이 상대보다",
		"더 많은 것과 같은 효과를 얻을 수 있기 때문에, 전투를 앞두고 있다면",
		"바로 사용해주는 것이 좋습니다."
}, strong = {}, weak = {}, stats = @Stats(offense = Level.ZERO, survival = Level.EIGHT, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.EASY)
public class FastRegeneration extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(FastRegeneration.class, "cooldown", 25,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(FastRegeneration.class, "duration", 10,
			"# 지속 시간 (단위: 초)") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public FastRegeneration(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

	private final Duration healthGain = new Duration(DURATION_CONFIG.getValue() * 2, cooldownTimer) {

		private int tick;

		@Override
		public void onDurationStart() {
			tick = 0;
		}

		@Override
		public void onDurationProcess(int count) {
			final Player player = getPlayer();
			tick++;
			if (!player.isDead()) {
				switch (tick) {
					case 1:
					case 5:
						SoundLib.BASS_DRUM.playInstrument(getPlayer(), Note.natural(0, Tone.A));
						break;
					case 2:
					case 6:
						SoundLib.BASS_DRUM.playInstrument(getPlayer(), Note.natural(0, Tone.E));
						break;
				}
			}
			if (tick >= 8) {
				tick = 0;
			}
			if (count % 2 == 0) {
				if (!player.isDead()) {
					final double playerHealth = player.getHealth();
					final double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
					if (playerHealth < maxHealth) {
						final double gain;
						if (playerHealth <= 5) {
							gain = .9;
						} else if (playerHealth <= 10) {
							gain = .7;
						} else {
							gain = .5;
						}

						final EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, gain, RegainReason.CUSTOM);
						Bukkit.getPluginManager().callEvent(event);
						if (!event.isCancelled()) {
							player.setHealth(RangesKt.coerceIn(player.getHealth() + event.getAmount(), 0, maxHealth));
						}
					}
				}
			}
		}

	}.setPeriod(TimeUnit.TICKS, 5);

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !healthGain.isDuration() && !cooldownTimer.isCooldown()) {
			healthGain.start();
			return true;
		}
		return false;
	}

}
