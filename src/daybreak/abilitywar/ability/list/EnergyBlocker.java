package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Scheduled;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

@AbilityManifest(Name = "에너지 블로커", Rank = Rank.A, Species = Species.HUMAN)
public class EnergyBlocker extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(EnergyBlocker.class, "Cooldown", 3,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	private boolean projectileBlocking = true;

	public EnergyBlocker(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f원거리 공격 피해를 1/3로, 근거리 공격 피해를 두 배로 받거나"),
				ChatColor.translateAlternateColorCodes('&', "&f원거리 공격 피해를 두 배로, 근거리 공격 피해를 1/3로 받을 수 있습니다."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 각각의 피해 정도를 뒤바꿉니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 좌클릭하면 현재 상태를 확인할 수 있습니다."));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!cooldownTimer.isCooldown()) {
					projectileBlocking = !projectileBlocking;
					Player p = getPlayer();
					if (projectileBlocking) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b원거리 &f1/3&7, &a근거리 &f두 배로 변경되었습니다."));
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b원거리 &f두 배&7, &a근거리 &f1/3로 변경되었습니다."));
					}

					cooldownTimer.start();
				}
			} else if (clickType.equals(ClickType.LEFT_CLICK)) {
				if (projectileBlocking) {
					getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6현재 상태&f: &b원거리 &f1/3&7, &a근거리 &f두 배"));
				} else {
					getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6현재 상태&f: &b원거리 &f두 배&7, &a근거리 &f1/3"));
				}
			}
		}

		return false;
	}

	@Scheduled
	private final Timer particle = new Timer() {

		@Override
		public void onStart() {
		}

		@Override
		public void run(int count) {
			if (projectileBlocking) {
				ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().add(0, 2.2, 0), new RGB(116, 237, 167));
			} else {
				ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().add(0, 2.2, 0), new RGB(85, 237, 242));
			}
		}

		@Override
		public void onEnd() {
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			DamageCause cause = e.getCause();
			if (cause.equals(DamageCause.PROJECTILE)) {
				if (projectileBlocking) {
					e.setDamage(e.getDamage() / 3);
				} else {
					e.setDamage(e.getDamage() * 2);
				}
			} else if (cause.equals(DamageCause.ENTITY_ATTACK)) {
				if (projectileBlocking) {
					e.setDamage(e.getDamage() * 2);
				} else {
					e.setDamage(e.getDamage() / 3);
				}
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
