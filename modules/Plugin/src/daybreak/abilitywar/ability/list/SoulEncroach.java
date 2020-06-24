package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.LocationUtil.Predicates;
import daybreak.abilitywar.utils.base.math.geometry.Points;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.function.Predicate;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;

@AbilityManifest(name = "영혼 잠식", rank = Rank.S, species = Species.GOD, explain = {
		"철괴를 우클릭하면 마지막으로 타격했던 플레이어가 $[DistanceConfig]칸 이내에 있는 경우에 한하여",
		"3초간 대상의 영혼에 잠식하여 타겟팅할 수 없는 상태로 변합니다. $[CooldownConfig]",
		"영혼 잠식이 끝나면 영혼에서 빠져나오며 바라보는 방향으로 짧게 돌진하고",
		"대상에게 대미지를 줍니다. 대상의 체력이 적을수록 더욱 큰 피해를 입히며,",
		"잠식 도중 웅크리면 즉시 빠져나올 수 있습니다. 영혼에서 빠져나오며 입힌 피해로",
		"대상을 죽일 경우 잃은 체력의 절반을 회복하고 능력 쿨타임이 즉시 초기화되며,",
		"주변 $[DistanceConfig]칸 이내의 가장 가까운 플레이어가 자동으로 대상으로 설정되고",
		"다음 스킬이 강화됩니다."
})
public class SoulEncroach extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(SoulEncroach.class, "Cooldown", 120,
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
	public static final SettingObject<Integer> DistanceConfig = abilitySettings.new SettingObject<Integer>(SoulEncroach.class, "Distance", 7,
			"# 능력 거리 설정") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};
	private static final Points PARTICLES_WHITE_LAYER = Points.of(0.06, new boolean[][]{
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, true, true, true, true, true, true, false, false, true, true, true, true, true, true, true, true, false, false, true, true, true, true, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, true, true, true, true, true, true, false, false, false, true, true, true, true, true, true, true, false, false, false, true, true, true, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, true, true, true, true, true, false, false, false, false, true, true, true, true, true, true, false, false, false, false, true, true, true, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, true, true, true, true, true, false, false, false, false, true, true, true, true, true, true, false, false, false, false, true, true, true, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, true, true, true, true, true, false, false, false, false, true, true, true, true, true, true, false, false, false, false, true, true, true, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, true, true, true, true, true, true, false, false, false, true, true, true, true, true, true, true, false, false, false, true, true, true, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
	});
	private static final Points PARTICLES_BLACK_LAYER = Points.of(0.06, new boolean[][]{
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, false, false, false, false, false, false, false, false, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, true, true, true, false, false, false, true, true, true, true, false, false, false, false, false, false, true, true, true, true, false, false, false, true, true, true, false, false, false, false, false, false},
			{false, false, false, false, false, false, true, true, false, false, false, true, true, true, true, true, true, false, false, false, false, true, true, true, true, true, true, false, false, false, true, true, false, false, false, false, false, false},
			{false, false, false, false, false, false, true, true, false, false, false, true, true, true, true, true, true, false, false, false, false, true, true, true, true, true, true, false, false, false, true, true, false, false, false, false, false, false},
			{false, false, false, false, false, false, true, true, false, false, false, true, true, true, true, true, true, false, false, false, false, true, true, true, true, true, true, false, false, false, true, true, false, false, false, false, false, false},
			{false, false, false, false, false, false, true, true, false, false, false, true, true, true, true, true, true, false, false, false, false, true, true, true, true, true, true, false, false, false, true, true, false, false, false, false, false, false},
			{false, false, false, false, false, false, true, true, false, false, false, true, true, true, true, true, true, false, false, false, false, true, true, true, true, true, true, false, false, false, true, true, false, false, false, false, false, false},
			{false, false, false, false, false, false, true, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, true, true, false, false, false, false, false, false},
			{false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, true, true, true, true, false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, true, true, false, false, true, true, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, true, true, false, true, true, false, true, true, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, false, true, true, false, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
	});
	private static final RGB BLACK = RGB.of(1, 1, 1), WHITE = RGB.of(250, 250, 250);

	private final int distance = DistanceConfig.getValue(), distanceSquared = distance * distance;
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final Predicate<Entity> STRICT = Predicates.STRICT(getPlayer());
	private final ActionbarChannel noticeChannel = newActionbarChannel();
	private int killCount = 0;
	private Player lastVictim = null;
	@Scheduled
	private final Timer notice = new Timer() {
		private Player last;

		@Override
		protected void run(int count) {
			if (last != null) {
				if (lastVictim != null) {
					noticeChannel.update(lastVictim.getHealth() <= 6 ? "§4마지막으로 때린 상대의 체력이 적습니다." : null);
				} else {
					noticeChannel.update(null);
				}
			}
			this.last = lastVictim;
		}
	}.setPeriod(TimeUnit.TICKS, 5);
	private final DurationTimer skillTimer = new DurationTimer(60) {
		private GameMode originalMode;
		private float originalSpeed;

		@Override
		protected void onDurationStart() {
			this.originalMode = getPlayer().getGameMode();
			getParticipant().attributes().TARGETABLE.setValue(false);
			getPlayer().setGameMode(GameMode.SPECTATOR);
			this.originalSpeed = getPlayer().getFlySpeed();
		}

		@Override
		protected void onDurationProcess(int count) {
			getPlayer().setFlySpeed(0f);
			if (getPlayer().getGameMode() == GameMode.SPECTATOR) {
				getPlayer().setSpectatorTarget(null);
			}
			final Location headLocation = lastVictim.getEyeLocation().clone().add(0, 1.5, 0);
			final double distanceSquared = getPlayer().getWorld().equals(headLocation.getWorld()) ? getPlayer().getLocation().distanceSquared(headLocation) : Double.MAX_VALUE;
			if (distanceSquared > 49) {
				getPlayer().teleport(headLocation, TeleportCause.PLUGIN);
			} else {
				try {
					getPlayer().setVelocity(headLocation.toVector().subtract(getPlayer().getLocation().toVector()).normalize().multiply(1.4 * (distanceSquared / 25)));
				} catch (IllegalArgumentException ignored) {
				}
			}
			if (count % 2 == 0) {
				final Location baseLocation = headLocation.clone().subtract(0, 1.4, 0);
				final float yaw = lastVictim.getLocation().getYaw();
				for (Location loc : PARTICLES_WHITE_LAYER.rotateAroundAxisY(-yaw).toLocations(baseLocation)) {
					ParticleLib.REDSTONE.spawnParticle(loc, WHITE);
				}
				PARTICLES_WHITE_LAYER.rotateAroundAxisY(yaw);
				for (Location loc : PARTICLES_BLACK_LAYER.rotateAroundAxisY(-yaw).toLocations(baseLocation)) {
					ParticleLib.REDSTONE.spawnParticle(loc, BLACK);
				}
				PARTICLES_BLACK_LAYER.rotateAroundAxisY(yaw);
			}
		}

		@Override
		protected void onDurationEnd() {
			onDurationSilentEnd();
			getPlayer().setVelocity(getPlayer().getLocation().getDirection().setY(-1));
			lastVictim.damage(Math.max(1, (21.5 * (1 - (lastVictim.getHealth() / lastVictim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()))) * (1 + (killCount * 0.15))), getPlayer());
			if (lastVictim.isDead()) {
				killCount++;
				new Timer(Math.min(3, killCount)) {
					@Override
					protected void run(int count) {
						SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.A));
						SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.C));
						SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.E));
						SoundLib.BASS_DRUM.playInstrument(getPlayer(), Note.natural(0, Tone.A));
					}
				}.setPeriod(TimeUnit.TICKS, 2).start();
				gainHealth((getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - getPlayer().getHealth()) / 2.0);
				Player nearest = LocationUtil.getNearestEntity(Player.class, getPlayer().getLocation(), STRICT.and(new Predicate<Entity>() {
					private final Entity criterion = lastVictim;

					@Override
					public boolean test(Entity entity) {
						return !entity.equals(criterion);
					}
				}));
				if (nearest == null || nearest.getLocation().distanceSquared(getPlayer().getLocation()) <= distanceSquared) {
					lastVictim = nearest;
				}
			} else {
				killCount = 0;
				cooldownTimer.start();
			}
		}

		@Override
		protected void onDurationSilentEnd() {
			getParticipant().attributes().TARGETABLE.setValue(true);
			getPlayer().setGameMode(originalMode);
			getPlayer().setAllowFlight(originalMode != GameMode.SURVIVAL && originalMode != GameMode.ADVENTURE);
			getPlayer().setFlying(false);
			getPlayer().setFlySpeed(originalSpeed);
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	public SoulEncroach(AbstractGame.Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !cooldownTimer.isCooldown() && !skillTimer.isDuration()) {
			if (lastVictim != null) {
				if (getGame().getParticipant(lastVictim).attributes().TARGETABLE.getValue()) {
					final double distanceSquared = getPlayer().getWorld().equals(lastVictim.getWorld()) ? getPlayer().getLocation().distanceSquared(lastVictim.getLocation()) : Double.MAX_VALUE;
					if (distanceSquared <= this.distanceSquared) {
						skillTimer.start();
						return true;
					} else getPlayer().sendMessage("§c대상이 너무 멀리 있습니다. §7(§f" + distance + "칸 이내에서 사용§7)");
				} else getPlayer().sendMessage("§4대상이 타게팅 가능한 상태가 아닙니다.");
			} else getPlayer().sendMessage("§4마지막으로 때렸던 플레이어가 존재하지 않습니다.");
		}
		return false;
	}

	@SubscribeEvent
	private void onAttack(EntityDamageByEntityEvent e) {
		if (skillTimer.isRunning()) return;
		Entity damager = e.getDamager();
		if ((damager.equals(getPlayer()) || (damager instanceof Projectile && getPlayer().equals(((Projectile) damager).getShooter()))) && e.getEntity() instanceof Player && !getPlayer().equals(e.getEntity())) {
			Player victim = (Player) e.getEntity();
			if (getGame().isParticipating(victim) && getGame().getParticipant(victim).attributes().TARGETABLE.getValue()) {
				this.lastVictim = victim;
			}
		}
	}

	private void gainHealth(double amount) {
		if (!getPlayer().isDead()) {
			getPlayer().setHealth(Math.min(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), getPlayer().getHealth() + amount));
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onSneak(PlayerToggleSneakEvent e) {
		if (skillTimer.isRunning()) {
			skillTimer.stop(false);
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onGameModeChange(PlayerGameModeChangeEvent e) {
		if (skillTimer.isRunning() && getPlayer().getGameMode() == GameMode.SPECTATOR) e.setCancelled(true);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerTeleport(PlayerTeleportEvent e) {
		if (e.getCause() == TeleportCause.PLUGIN) return;
		if (skillTimer.isRunning() && getPlayer().getGameMode() == GameMode.SPECTATOR) e.setCancelled(true);
	}

}
