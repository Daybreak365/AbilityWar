package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;

@AbilityManifest(name = "깃털", rank = Rank.A, species = Species.OTHERS, explain = {
		"철괴를 우클릭하면 $[DURATION_CONFIG]초간 §b비행§f할 수 있습니다. $[COOLDOWN_CONFIG]",
		"§b비행 §f중 웅크리면 바라보는 방향으로 돌진합니다.",
		"낙하 대미지를 무시합니다."
})
@Tips(tip = {
		"깃털은 모든 능력을 통틀어서 가장 강력한 기동력을 뽐내는 능력입니다.",
		"비행이라는 강력한 이동 스킬로 전투에서 우위를 점하세요."
}, strong = {
		@Description(subject = "빠른 이동", explain = {
				"빠른 이동으로 전투에 빠르게 합류하고 적을 손쉽게 쫓아가세요."
		}),
		@Description(subject = "낙하 대미지 무시", explain = {
				"낙하 대미지를 완전히 무시하기 때문에 이를 이용해 큰 대미지를 넣는",
				"능력에 강합니다."
		})
}, weak = {
		@Description(subject = "좁은 공간", explain = {
				"이동할 곳이 많지 않은 좁은 공간에서는 비행 능력이 힘을 발하기 어렵습니다.",
				"건물 내부와 같은 좁은 환경, 또는 그런 환경을 만들어내는 능력에 취약합니다.",
				"넓은 공간에서 사용하세요."
		})
}, stats = @Stats(offense = Level.ZERO, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.TEN, utility = Level.ZERO), difficulty = Difficulty.EASY)
public class Feather extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Feather.class, "cooldown", 80,
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

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Feather.class, "duration", 10,
			"# 지속시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public Feather(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Duration durationTimer = new Duration(DURATION_CONFIG.getValue() * 4, cooldownTimer) {

		@Override
		public void onDurationProcess(int seconds) {
			getPlayer().setAllowFlight(true);
			getPlayer().setFlying(true);
		}

		@Override
		public void onDurationEnd() {
			getPlayer().setFlying(false);
			GameMode mode = getPlayer().getGameMode();
			getPlayer().setAllowFlight(mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE);
		}

		@Override
		protected void onDurationSilentEnd() {
			getPlayer().setFlying(false);
			GameMode mode = getPlayer().getGameMode();
			getPlayer().setAllowFlight(mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE);
		}

	}.setPeriod(TimeUnit.TICKS, 5);

	private final AbilityTimer dash = new AbilityTimer() {
		@Override
		protected void run(int count) {
			if (getPlayer().isFlying()) {
				getPlayer().setVelocity(getPlayer().getLocation().getDirection().multiply(1.25));
				ParticleLib.CLOUD.spawnParticle(getPlayer().getLocation(), .4, .4, .4, 5, 0.001);
			} else {
				stop(false);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1).register();

	@SubscribeEvent
	private void onEntityDamage(EntityDamageEvent e) {
		if (!e.isCancelled() && getPlayer().equals(e.getEntity()) && e.getCause().equals(DamageCause.FALL)) {
			e.setCancelled(true);
			getPlayer().sendMessage("§a낙하 대미지를 받지 않습니다.");
			SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
		}
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !durationTimer.isDuration() && !cooldownTimer.isCooldown()) {
			durationTimer.start();
			return true;
		}
		return false;
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onToggleSneak(PlayerToggleSneakEvent e) {
		if (getPlayer().isFlying() && e.isSneaking()) {
			dash.start();
		} else {
			dash.stop(false);
		}
	}

}
