package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.versioncompat.NMSUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.StringJoiner;

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
				ChatColor.translateAlternateColorCodes('&', "&f10초마다 스택을 1만큼 얻습니다. 스택은 최대 3만큼 중첩됩니다."),
				ChatColor.translateAlternateColorCodes('&', "&f활을 쏘면 스택을 1만큼 소모하여 폭발 화살을 쏩니다."),
				ChatColor.translateAlternateColorCodes('&', "&f스택이 없으면 화살이 나가지 않습니다."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		return false;
	}

	private final int maxStack = 3;
	private int stack = 0;

	private final Timer stackAdder = new Timer() {
		@Override
		protected void onProcess(int count) {
			if (stack < maxStack) {
				stack++;
			}
		}
	}.setPeriod(200);

	private final int size = SizeConfig.getValue();

	private final Timer actionbarSender = new Timer() {
		@Override
		protected void onProcess(int count) {
			StringJoiner joiner = new StringJoiner(" ");
			for (int i = 0; i < stack; i++) joiner.add(ChatColor.RED + "●");
			for (int i = 0; i < maxStack - stack; i++) joiner.add(ChatColor.RED + "○");
			NMSUtil.PlayerUtil.sendActionbar(getPlayer(), joiner.toString(), 0, 3, 0);
		}
	}.setPeriod(2);

	@SubscribeEvent
	private void onProjectileShoot(ProjectileHitEvent e) {
		if (getPlayer().equals(e.getEntity().getShooter()) && e.getEntity() instanceof Arrow) {
			Location location = e.getHitEntity() == null ? e.getHitBlock().getLocation() : e.getHitEntity().getLocation();
			location.getWorld().createExplosion(location, size, false, true);
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
			}
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onRestrictionClear(AbilityRestrictionClearEvent e) {
		stackAdder.startTimer();
		actionbarSender.startTimer();
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
