package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;

@AbilityManifest(name = "깃털", rank = Rank.A, species = Species.OTHERS, explain = {
		"철괴를 우클릭하면 $[DurationConfig]초간 §b비행§f할 수 있습니다. $[CooldownConfig]",
		"§b비행 §f중 웅크리면 바라보는 방향으로 돌진합니다.",
		"낙하 대미지를 무시합니다."
})
public class Feather extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(Feather.class, "Cooldown", 80,
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

	public static final SettingObject<Integer> DurationConfig = abilitySettings.new SettingObject<Integer>(Feather.class, "Duration", 10,
			"# 지속시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public Feather(Participant participant) {
		super(participant);
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final DurationTimer durationTimer = new DurationTimer(DurationConfig.getValue() * 4, cooldownTimer) {

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

	private final Timer dash = new Timer() {
		@Override
		protected void run(int count) {
			if (getPlayer().isFlying()) {
				getPlayer().setVelocity(getPlayer().getLocation().getDirection().multiply(1.25));
				ParticleLib.CLOUD.spawnParticle(getPlayer().getLocation(), .4, .4, .4, 5, 0.001);
			} else {
				stop(false);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@SubscribeEvent
	private void onEntityDamage(EntityDamageEvent e) {
		if (!e.isCancelled() && getPlayer().equals(e.getEntity()) && e.getCause().equals(DamageCause.FALL)) {
			e.setCancelled(true);
			getPlayer().sendMessage("§a낙하 대미지를 받지 않습니다.");
			SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
		}
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !durationTimer.isDuration() && !cooldownTimer.isCooldown()) {
			durationTimer.start();
			return true;
		}
		return false;
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onToogleSneak(PlayerToggleSneakEvent e) {
		Player player = e.getPlayer();
		if (player.isFlying() && e.isSneaking()) {
			dash.start();
		} else {
			dash.stop(false);
		}
	}

}
