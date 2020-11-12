package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
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
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AbilityManifest(name = "버서커", rank = Rank.B, species = Species.HUMAN, explain = {
		"철괴를 우클릭한 후 4초 안에 하는 다음 근접 공격이 강화됩니다. $[COOLDOWN_CONFIG]",
		"강화된 공격은 $[STRENGTH]배의 대미지를 내며, 강화된 공격을 사용한 후",
		"$[GROGGY_DURATION]초간 대미지를 입힐 수 없습니다.",
		"지속시간 내에 공격하지 못한 경우, 쿨타임을 1/3만 갖습니다."
})
@Tips(tip = {
		"한 번에 폭발적인 대미지를 낸 후 몇 초간 공격을 할 수 없기 때문에,",
		"적재적소에 사용하는 것이 매우 중요합니다."
}, strong = {
		@Description(subject = "폭발적인 대미지", explain = {
				"한 번에 폭발적인 대미지를 주어 상대를 깜짝 놀래키세요!"
		})
}, weak = {
		@Description(subject = "큰 리스크", explain = {
				"능력 사용 후 몇 초간 아예 공격을 할 수 없기 때문에, 이 동안",
				"위험에 빠질 수 있습니다. 조심해서 사용하세요."
		})
}, stats = @Stats(offense = Level.SEVEN, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.NORMAL)
public class Berserker extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Berserker.class, "cooldown", 60,
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

	public static final SettingObject<Double> STRENGTH = abilitySettings.new SettingObject<Double>(Berserker.class, "strength", 2.2,
			"# 공격 강화 배수") {

		@Override
		public boolean condition(Double value) {
			return value >= 2;
		}

	};

	public static final SettingObject<Integer> GROGGY_DURATION = abilitySettings.new SettingObject<Integer>(Berserker.class, "groggy-duration", 4,
			"# 능력 사용 후 공격 불가 지속 시간",
			"# 단위 : 초") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Berserker(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

	private class BerserkerTimer extends Duration {

		private BerserkerTimer() {
			super(5);
		}

		@Override
		protected void onDurationProcess(int count) {
		}

		@Override
		protected void onDurationEnd() {
			cooldownTimer.start();
			cooldownTimer.setCount(cooldownTimer.getCooldown() / 3);
		}

		public boolean stop() {
			final boolean bool = super.stop(true);
			cooldownTimer.start();
			return bool;
		}

	}

	private final BerserkerTimer skill = new BerserkerTimer();

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
			if (!skill.isDuration() && !cooldownTimer.isCooldown()) {
				skill.start();
				return true;
			}
		}

		return false;
	}

	private final double strength = STRENGTH.getValue();
	private final int debuff = GROGGY_DURATION.getValue();

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer()) && !e.isCancelled()) {
			if (skill.isRunning()) {
				skill.stop();
				e.setDamage(e.getDamage() * strength);
				SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
				ParticleLib.SWEEP_ATTACK.spawnParticle(e.getEntity().getLocation(), 1, 1, 1, 5);
				new NoAttack(debuff * 5).start();
			}
		}
	}

	@Nullable
	private static Entity getDamager(final Entity damager) {
		if (damager instanceof Projectile) {
			final ProjectileSource shooter = ((Projectile) damager).getShooter();
			return shooter instanceof Entity ? (Entity) shooter : null;
		} else return damager;
	}

	private class NoAttack extends AbilityTimer implements Listener {

		private final ActionbarChannel actionbarChannel = newActionbarChannel();

		private NoAttack(final int ticks) {
			super(ticks);
			setPeriod(TimeUnit.TICKS, 4);
		}

		@Override
		protected void onStart() {
			Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		}

		@EventHandler
		private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
			if (getPlayer().equals(getDamager(e.getDamager()))) {
				e.setCancelled(true);
			}
		}

		@Override
		protected void run(int count) {
			actionbarChannel.update("§c공격 불능§f: " + (getCount() / 5.0) + "초");
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			actionbarChannel.unregister();
			HandlerList.unregisterAll(this);
		}
	}

}
