package daybreak.abilitywar.ability.list;

import com.google.common.base.Strings;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.manager.object.WRECK;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

@AbilityManifest(name = "폭발화살", rank = Rank.S, species = Species.HUMAN, explain = {
		"$[StackPeriodConfig]초마다 스택을 1만큼 얻습니다. 스택은 최대 $[MaxStackConfig]만큼 중첩됩니다.",
		"활을 쏘면 스택을 1만큼 소모하여 폭발 화살을 쏩니다.",
		"스택이 없으면 활을 쏠 수 없습니다."
})
public class BombArrow extends AbilityBase {

	public static final SettingObject<Integer> StackPeriodConfig = abilitySettings.new SettingObject<Integer>(BombArrow.class, "StackPeriod", 7,
			"# 몇초마다 스택을 얻을지 설정합니다.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> MaxStackConfig = abilitySettings.new SettingObject<Integer>(BombArrow.class, "MaxStack", 4,
			"# 최대로 얻을 수 있는 스택 수를 설정합니다.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> SizeConfig = abilitySettings.new SettingObject<Integer>(BombArrow.class, "Size", 1,
			"# 화살을 맞췄을 때 얼마나 큰 폭발을 일으킬지 설정합니다.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public BombArrow(Participant participant) {
		super(participant);
	}

	private int stack = 0;
	private final int maxStack = MaxStackConfig.getValue();

	@Scheduled
	private final Timer stackAdder = new Timer() {
		@Override
		protected void run(int count) {
			if (stack < maxStack) {
				stack++;
				actionbarChannel.update(ChatColor.DARK_RED.toString().concat(Strings.repeat("●", stack).concat(Strings.repeat("○", Math.max(maxStack - stack, 0)))));
			}
		}
	}.setPeriod(TimeUnit.SECONDS, WRECK.isEnabled(getGame()) ? (int) (StackPeriodConfig.getValue() / (Settings.getCooldownDecrease().getPercentage() / 25.0)) : StackPeriodConfig.getValue());

	private final ActionbarChannel actionbarChannel = newActionbarChannel();

	@SubscribeEvent
	private void onProjectileShoot(ProjectileHitEvent e) {
		if (getPlayer().equals(e.getEntity().getShooter()) && e.getEntity() instanceof Arrow) {
			Location location = ServerVersion.getVersionNumber() >= 11 ? e.getHitEntity() == null ? e.getHitBlock().getLocation() : e.getHitEntity().getLocation() : e.getEntity().getLocation();
			location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), SizeConfig.getValue(), false, true);
			e.getEntity().remove();
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onEntityShootBow(EntityShootBowEvent e) {
		if (e.getProjectile() instanceof Arrow) {
			if (stack <= 0) {
				e.setCancelled(true);
				getPlayer().updateInventory();
			} else {
				stack--;
				actionbarChannel.update(Strings.repeat(ChatColor.DARK_RED + "●", stack).concat(Strings.repeat(ChatColor.DARK_RED + "○", Math.max(maxStack - stack, 0))));
			}
		}
	}

}
