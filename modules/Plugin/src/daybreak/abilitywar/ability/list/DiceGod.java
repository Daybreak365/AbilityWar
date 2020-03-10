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
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

@AbilityManifest(name = "다이스 갓", rank = Rank.A, species = Species.GOD, explain = {
		"철괴를 우클릭하면 §c재생 §f/ §b신속 §f/ §6힘 §f/ §3저항 §f/ §8구속 §f/ §7나약함 §f효과 중 하나를",
		"10초간 받습니다. $[CooldownConfig]",
		"공격을 받았을 때 1/6 확률로 대미지를 받는 대신 대미지만큼 체력을 회복합니다."
})
public class DiceGod extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(DiceGod.class, "Cooldown", 25,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Messager.formatCooldown(getValue());
		}

	};

	public DiceGod(Participant participant) {
		super(participant);
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !cooldownTimer.isCooldown()) {
			Player p = getPlayer();
			Random random = new Random();
			switch (random.nextInt(6)) {
				case 0:
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c재생 &f효과를 받았습니다."));
					PotionEffects.REGENERATION.addPotionEffect(p, 200, 2, true);
					break;
				case 1:
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b신속 &f효과를 받았습니다."));
					PotionEffects.SPEED.addPotionEffect(p, 200, 2, true);
					break;
				case 2:
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6힘 &f효과를 받았습니다."));
					PotionEffects.INCREASE_DAMAGE.addPotionEffect(p, 200, 2, true);
					break;
				case 3:
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3저항 &f효과를 받았습니다."));
					PotionEffects.DAMAGE_RESISTANCE.addPotionEffect(p, 200, 2, true);
					break;
				case 4:
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8구속 &f효과를 받았습니다."));
					PotionEffects.SLOW.addPotionEffect(p, 200, 1, true);
					break;
				case 5:
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7나약함 &f효과를 받았습니다."));
					PotionEffects.WEAKNESS.addPotionEffect(p, 200, 1, true);
					break;
			}
			cooldownTimer.start();
			return true;
		}
		return false;
	}

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			Random r = new Random();
			if (r.nextInt(6) == 0) {
				if (!getPlayer().isDead()) {
					getPlayer().setHealth(Math.min(getPlayer().getHealth() + e.getFinalDamage(), getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
				}
				e.setDamage(0);
			}
		}
	}

}
