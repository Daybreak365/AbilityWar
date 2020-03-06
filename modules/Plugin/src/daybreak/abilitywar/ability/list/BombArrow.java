package daybreak.abilitywar.ability.list;

import com.google.common.base.Strings;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.manager.object.WRECK;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

@AbilityManifest(Name = "폭발화살", Rank = Rank.S, Species = Species.HUMAN)
public class BombArrow extends AbilityBase {

	public static final SettingObject<Integer> SizeConfig = new SettingObject<Integer>(BombArrow.class, "Size", 1,
			"# 화살을 맞췄을 때 얼마나 큰 폭발을 일으킬지 설정합니다.") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public BombArrow(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f7초마다 스택을 1만큼 얻습니다. 스택은 최대 4만큼 중첩됩니다."),
				ChatColor.translateAlternateColorCodes('&', "&f활을 쏘면 스택을 1만큼 소모하여 폭발 화살을 쏩니다."),
				ChatColor.translateAlternateColorCodes('&', "&f스택이 없으면 화살이 나가지 않습니다."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		return false;
	}

	private final int maxStack = 4;
	private int stack = 0;

	@Scheduled
	private final Timer stackAdder = new Timer() {
		@Override
		protected void run(int count) {
			if (stack < maxStack) {
				stack++;
				actionbarChannel.update(ChatColor.DARK_RED.toString().concat(Strings.repeat("●", stack).concat(Strings.repeat("○", maxStack - stack))));
			}
		}
	}.setPeriod(TimeUnit.TICKS, WRECK.isEnabled(getGame()) ? 60 : 140);

	private final float size = SizeConfig.getValue();

	private final ActionbarChannel actionbarChannel = newActionbarChannel();

	@SubscribeEvent
	private void onProjectileShoot(ProjectileHitEvent e) {
		if (getPlayer().equals(e.getEntity().getShooter()) && e.getEntity() instanceof Arrow) {
			Location location = ServerVersion.getVersionNumber() >= 11 ? e.getHitEntity() == null ? e.getHitBlock().getLocation() : e.getHitEntity().getLocation() : e.getEntity().getLocation();
			location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), size, false, true);
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
				actionbarChannel.update(Strings.repeat(ChatColor.DARK_RED + "●", stack).concat(Strings.repeat(ChatColor.DARK_RED + "○", maxStack - stack)));
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
