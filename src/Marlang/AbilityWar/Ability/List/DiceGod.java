package Marlang.AbilityWar.Ability.List;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Utils.Messager;

public class DiceGod extends AbilityBase {
	
	public DiceGod() {
		super("다이스 갓", Rank.God, 
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 &c재생 &f/ &b신속 &f/ &6힘 &f/ &5위더 &f/ &8구속 &f/ &7나약함 &f효과 중 하나를"),
				ChatColor.translateAlternateColorCodes('&', "&f10초간 받습니다. " + Messager.formatCooldown(60)),
				ChatColor.translateAlternateColorCodes('&', "&f신은 주사위 놀이를 하지 않는다던가..."));
		
		registerTimer(Cool);
	}
	
	CooldownTimer Cool = new CooldownTimer(this, 60);
	
	@Override
	public void ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Player p = getPlayer();
					
					Random r = new Random();
					Integer random = r.nextInt(6);
					
					if(random.equals(0)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c재생 &f효과를 받았습니다."));
						p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1), true);
					} else if(random.equals(1)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b신속 &f효과를 받았습니다."));
						p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1), true);
					} else if(random.equals(2)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6힘 &f효과를 받았습니다."));
						p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 1), true);
					} else if(random.equals(3)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5위더 &f효과를 받았습니다."));
						p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 1), true);
					} else if(random.equals(4)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8구속 &f효과를 받았습니다."));
						p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 1), true);
					} else if(random.equals(5)) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7나약함 &f효과를 받았습니다."));
						p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1), true);
					}
					
					Cool.StartTimer();
				}
			}
		}
	}
	
	@Override
	public void PassiveSkill(Event event) {}
	
}
