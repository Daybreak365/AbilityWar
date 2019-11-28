package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.versioncompat.VersionUtil;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(Name = "다이스 갓", Rank = Rank.A, Species = Species.GOD)
public class DiceGod extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(DiceGod.class, "Cooldown", 25,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public DiceGod(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 &c재생 &f/ &b신속 &f/ &6힘 &f/ &3저항 &f/ &8구속 &f/ &7나약함 &f효과 중 하나를"),
				ChatColor.translateAlternateColorCodes('&', "&f10초간 받습니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f공격을 받았을 때 1/6 확률로 데미지를 받는 대신 데미지만큼 체력을 회복합니다."));
	}
	
	private final CooldownTimer Cool = new CooldownTimer(CooldownConfig.getValue());
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.IRON_INGOT)) {
			if(ct.equals(ClickType.RIGHT_CLICK)) {
				if(!Cool.isCooldown()) {
					Player p = getPlayer();
					
					Random r = new Random();
					Integer random = r.nextInt(6);
					
					if(random.equals(0)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c재생 &f효과를 받았습니다."));
						PotionEffects.REGENERATION.addPotionEffect(p, 200, 2, true);
					} else if(random.equals(1)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b신속 &f효과를 받았습니다."));
						PotionEffects.SPEED.addPotionEffect(p, 200, 2, true);
					} else if(random.equals(2)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6힘 &f효과를 받았습니다."));
						PotionEffects.INCREASE_DAMAGE.addPotionEffect(p, 200, 2, true);
					} else if(random.equals(3)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3저항 &f효과를 받았습니다."));
						PotionEffects.DAMAGE_RESISTANCE.addPotionEffect(p, 200, 2, true);
					} else if(random.equals(4)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8구속 &f효과를 받았습니다."));
						PotionEffects.SLOW.addPotionEffect(p, 200, 1, true);
					} else if(random.equals(5)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7나약함 &f효과를 받았습니다."));
						PotionEffects.WEAKNESS.addPotionEffect(p, 200, 1, true);
					}
					
					Cool.startTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			Random r = new Random();
			if(r.nextInt(6) == 0) {
				double damage = e.getDamage();
				e.setDamage(0);
				
				double health = getPlayer().getHealth() + damage;
				
				if(health > VersionUtil.getMaxHealth(getPlayer())) health = VersionUtil.getMaxHealth(getPlayer());
				
				if(!getPlayer().isDead()) {
					getPlayer().setHealth(health);
				}
			}
		}
	}
	
	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
