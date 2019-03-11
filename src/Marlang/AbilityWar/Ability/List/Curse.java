package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityManifest;
import Marlang.AbilityWar.Ability.AbilityManifest.Rank;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.GameManager.Game.AbstractGame.Participant;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Library.Item.EnchantLib;

@AbilityManifest(Name = "컬스", Rank = Rank.B)
public class Curse extends AbilityBase {

	public static SettingObject<Integer> CountConfig = new SettingObject<Integer>(Curse.class, "Count", 1,
			"# 능력 사용횟수") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};

	public Curse(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f상대방을 철괴로 타격하면 상대방이 착용하고 있는 모든 갑옷에 귀속저주를 겁니다."),
				ChatColor.translateAlternateColorCodes('&', "&f" + CountConfig.getValue() + "번만 사용할 수 있습니다."));
	}
	
	private Integer Count = CountConfig.getValue();

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(entity != null) {
				if(entity instanceof Player) {
					Player p = (Player) entity;
					if(Count > 0) {
						ItemStack Helmet = p.getInventory().getHelmet();
						if(Helmet != null) {
							p.getInventory().setHelmet(EnchantLib.BINDING_CURSE.addEnchantment(Helmet, 1));
						}

						ItemStack Chestplate = p.getInventory().getChestplate();
						if(Chestplate != null) {
							p.getInventory().setChestplate(EnchantLib.BINDING_CURSE.addEnchantment(Chestplate, 1));
						}
						
						ItemStack Leggings = p.getInventory().getLeggings();
						if(Leggings != null) {
							p.getInventory().setLeggings(EnchantLib.BINDING_CURSE.addEnchantment(Leggings, 1));
						}

						ItemStack Boots = p.getInventory().getBoots();
						if(Boots != null) {
							p.getInventory().setBoots(EnchantLib.BINDING_CURSE.addEnchantment(Boots, 1));
						}
						
						SoundLib.ENTITY_ELDER_GUARDIAN_CURSE.playSound(p);
						
						Count--;
					}
				}
			} else {
				if(Count <= 0) {
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c더 이상 이 능력을 사용할 수 없습니다!"));
				}
			}
		}
	}
	
}
