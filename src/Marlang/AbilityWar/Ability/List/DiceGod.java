package Marlang.AbilityWar.Ability.List;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.GameManager.Object.Participant;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.EffectLib;
import Marlang.AbilityWar.Utils.VersionCompat.PlayerCompat;

public class DiceGod extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("다이스갓", "Cooldown", 60, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public DiceGod(Participant participant) {
		super(participant, "다이스 갓", Rank.GOD, 
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 &c재생 &f/ &b신속 &f/ &6힘 &f/ &5위더 &f/ &8구속 &f/ &7나약함 &f효과 중 하나를"),
				ChatColor.translateAlternateColorCodes('&', "&f10초간 받습니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f공격을 받았을 때 1/6 확률로 데미지를 받는 대신 데미지만큼 체력을 회복합니다."));
	}
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Player p = getPlayer();
					
					Random r = new Random();
					Integer random = r.nextInt(6);
					
					if(random.equals(0)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c재생 &f효과를 받았습니다."));
						EffectLib.REGENERATION.addPotionEffect(p, 200, 1, true);
					} else if(random.equals(1)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b신속 &f효과를 받았습니다."));
						EffectLib.SPEED.addPotionEffect(p, 200, 1, true);
					} else if(random.equals(2)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6힘 &f효과를 받았습니다."));
						EffectLib.INCREASE_DAMAGE.addPotionEffect(p, 200, 1, true);
					} else if(random.equals(3)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5위더 &f효과를 받았습니다."));
						EffectLib.WITHER.addPotionEffect(p, 200, 1, true);
					} else if(random.equals(4)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8구속 &f효과를 받았습니다."));
						EffectLib.SLOW.addPotionEffect(p, 200, 1, true);
					} else if(random.equals(5)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7나약함 &f효과를 받았습니다."));
						EffectLib.WEAKNESS.addPotionEffect(p, 200, 1, true);
					}
					
					Cool.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if(e.getEntity().equals(getPlayer())) {
				Random r = new Random();
				if(r.nextInt(6) == 0) {
					Double damage = e.getDamage();
					e.setDamage(0);
					
					Double health = getPlayer().getHealth() + damage;
					
					if(health > PlayerCompat.getMaxHealth(getPlayer())) health = PlayerCompat.getMaxHealth(getPlayer());
					
					if(!getPlayer().isDead()) {
						getPlayer().setHealth(health);
					}
				}
			}
		}
	}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
