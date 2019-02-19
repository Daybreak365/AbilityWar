package Marlang.AbilityWar.Ability.List;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.ParticleLib;
import Marlang.AbilityWar.Utils.Library.SoundLib;

public class Brewer extends AbilityBase {
	
	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("양조사", "Cooldown", 45, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Brewer(Player player) {
		super(player, "양조사", Rank.B,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 랜덤한 포션 하나를 얻습니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Player p = getPlayer();
					
					Random r = new Random();
					Integer random = r.nextInt(3);
					
					ItemStack Potion;
					if(random.equals(0)) {
						Potion = new ItemStack(Material.POTION);
					} else if(random.equals(1)) {
						Potion = new ItemStack(Material.LINGERING_POTION);
					} else {
						Potion = new ItemStack(Material.SPLASH_POTION);
					}
					
					PotionMeta potionMeta = (PotionMeta) Potion.getItemMeta();
					PotionType type = PotionType.values()[r.nextInt(PotionType.values().length)];
					try {
						if(type.isExtendable() && type.isUpgradeable()) {
							potionMeta.setBasePotionData(new PotionData(type, r.nextBoolean(), r.nextBoolean()));
						} else if(type.isExtendable() && !type.isUpgradeable()) {
							potionMeta.setBasePotionData(new PotionData(type, r.nextBoolean(), false));
						} else if(!type.isExtendable() && type.isUpgradeable()) {
							potionMeta.setBasePotionData(new PotionData(type, false, r.nextBoolean()));
						} else {
							potionMeta.setBasePotionData(new PotionData(type, false, false));
						}
					} catch(IllegalArgumentException exception) {}
					Potion.setItemMeta(potionMeta);
					
					p.getInventory().addItem(Potion);
					Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&5오늘은 어떤 포션을 마실까..."));
					SoundLib.ENTITY_ILLUSIONER_CAST_SPELL.playSound(p);
					ParticleLib.SPELL_WITCH.spawnParticle(p.getLocation(), 10, 2, 2, 2);
					
					Cool.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {}

	@Override
	public void onRestrictClear() {}

}
