package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.events.participant.ParticipantDeathEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

@AbilityManifest(Name = "초신성", Rank = Rank.B, Species = Species.OTHERS)
public class SuperNova extends AbilityBase {

	public static final SettingObject<Integer> SizeConfig = new SettingObject<Integer>(SuperNova.class, "Size", 10,
			"# 초신성이 사망할 때 일어날 폭발의 크기") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public SuperNova(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f마지막 순간에 큰 폭발을 일으키고 사망합니다."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	private final int size = SizeConfig.getValue();

	private final SimpleTimer explosion = new SimpleTimer(TaskType.REVERSE, size) {

		Location center;

		@Override
		public void onStart() {
			center = getPlayer().getLocation();
		}

		@Override
		public void run(int seconds) {
			double count = ((size + 1) - seconds) / 1.2;
			for (Location location : LocationUtil.getSphere(count, 5).toLocations(center)) {
				location.getWorld().createExplosion(location, 2);
				ParticleLib.SPELL.spawnParticle(location, 0, 0, 0, 1);
			}
		}

		@Override
		public void onEnd() {
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	@SubscribeEvent
	public void onPlayerDeath(ParticipantDeathEvent e) {
		if (e.getParticipant().equals(getParticipant())) {
			explosion.start();
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
