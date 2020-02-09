package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.version.VersionUtil;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@AbilityManifest(Name = "빠른 회복", Rank = Rank.A, Species = Species.HUMAN)
public class FastRegeneration extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(FastRegeneration.class, "Cooldown", 25,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(FastRegeneration.class, "Duration", 10,
			"# 지속 시간 (단위: 초)") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public FastRegeneration(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 빠른 회복 능력을 사용합니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f능력 사용 중 체력을 빠르게 회복하며, 체력이 적을 수록"),
				ChatColor.translateAlternateColorCodes('&', "&f더 빠른 속도로 회복합니다."));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private final DurationTimer healthGain = new DurationTimer(DurationConfig.getValue() * 2, cooldownTimer) {

		@Override
		public void onDurationStart() {
			sound.start();
		}

		@Override
		public void onDurationProcess(int count) {
			Player player = getPlayer();
			if (!player.isDead()) {
				final double playerHealth = player.getHealth();
				final double maxHealth = VersionUtil.getMaxHealth(player);
				if (playerHealth < maxHealth) {
					final double gain;
					if (playerHealth <= 2) {
						gain = 1.2;
					} else if (playerHealth <= 5) {
						gain = 1;
					} else if (playerHealth <= 10) {
						gain = 0.8;
					} else if (playerHealth <= 15) {
						gain = 0.6;
					} else {
						gain = 0.4;
					}

					player.setHealth(Math.min(player.getHealth() + gain, maxHealth));
				}
			}
		}

		@Override
		public void onDurationEnd() {
			sound.stop(false);
		}

		@Override
		public void onDurationSilentEnd() {
			sound.stop(false);
		}

	}.setPeriod(TimeUnit.TICKS, 10);

	private final Timer sound = new Timer() {

		private int tick;

		@Override
		public void onStart() {
			tick = 0;
		}

		@Override
		public void run(int count) {
			tick++;
			Player player = getPlayer();
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
		}

	}.setPeriod(TimeUnit.TICKS, 5);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !healthGain.isDuration() && !cooldownTimer.isCooldown()) {
			healthGain.start();
			return true;
		}
		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
