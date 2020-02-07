package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

@AbilityManifest(Name = "깃털", Rank = Rank.A, Species = Species.OTHERS)
public class Feather extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Feather.class, "Cooldown", 80,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Feather.class, "Duration", 10,
			"# 지속시간") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Feather(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 " + DurationConfig.getValue() + "초간 비행할 수 있습니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f낙하 대미지를 무시합니다."));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private final DurationTimer Duration = new DurationTimer(DurationConfig.getValue(), cooldownTimer) {

		@Override
		public void onDurationStart() {
		}

		@Override
		public void onDurationProcess(int seconds) {
			getPlayer().setAllowFlight(true);
			getPlayer().setFlying(true);
		}

		@Override
		public void onDurationEnd() {
			getPlayer().setAllowFlight(false);
		}

		@Override
		protected void onDurationSilentEnd() {
			getPlayer().setAllowFlight(false);
		}

	};

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!Duration.isDuration() && !cooldownTimer.isCooldown()) {
					Duration.start();

					return true;
				}
			}
		}

		return false;
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (!e.isCancelled() && getPlayer().equals(e.getEntity()) && e.getCause().equals(DamageCause.FALL)) {
			e.setCancelled(true);
			getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a낙하 대미지를 받지 않습니다."));
			SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
