package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

@AbilityManifest(Name = "보이드", Rank = Rank.A, Species = Species.OTHERS)
public class Void extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Void.class, "Cooldown", 100,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Void(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f공허의 존재 보이드. 철괴를 우클릭하면 보이드가 공허를 통하여"),
				ChatColor.translateAlternateColorCodes('&', "제일 가까이 있는 플레이어에게 텔레포트합니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f텔레포트를 하고 난 후 2초간 무적 상태에 돌입합니다."));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private final Timer invincibilityTimer = new Timer(2) {

		@Override
		public void run(int count) {
		}

	};

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!cooldownTimer.isCooldown()) {
					Player target = LocationUtil.getNearestPlayer(getPlayer());

					if (target != null) {
						getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + target.getName() + "&f님에게 텔레포트합니다."));
						getPlayer().teleport(target);
						ParticleLib.DRAGON_BREATH.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 20);

						invincibilityTimer.start();

						cooldownTimer.start();
					} else {
						getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a가장 가까운 플레이어&f가 존재하지 않습니다."));
					}
				}
			}
		}

		return false;
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			if (invincibilityTimer.isRunning()) {
				e.setCancelled(true);
				ParticleLib.DRAGON_BREATH.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 20);
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			if (invincibilityTimer.isRunning()) {
				e.setCancelled(true);
				ParticleLib.DRAGON_BREATH.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 20);
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByBlockEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			if (invincibilityTimer.isRunning()) {
				e.setCancelled(true);
				ParticleLib.DRAGON_BREATH.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 20);
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
