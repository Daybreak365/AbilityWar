package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Library.Item.EnchantLib;
import Marlang.AbilityWar.Utils.VersionCompat.PlayerCompat;

public class Curse extends AbilityBase {

	public static SettingObject<Integer> CountConfig = new SettingObject<Integer>("컬스", "Count", 1,
			"# 능력 사용횟수") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};

	public Curse(Player player) {
		super(player, "컬스", Rank.B,
				ChatColor.translateAlternateColorCodes('&', "&f상대방을 철괴로 타격하면 상대방이 착용하고 있는 모든 갑옷에 귀속저주를 겁니다."),
				ChatColor.translateAlternateColorCodes('&', "&f" + CountConfig.getValue() + "번만 사용할 수 있습니다."));
	}
	
	private Integer Count = CountConfig.getValue();

	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.LeftClick)) {
				if(Count > 0) {
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&a대상&f이 없습니다!"));
				} else {
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c이미 능력을 사용하였습니다."));
				}
			}
		}
		
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if(e.getDamager().equals(getPlayer())) {
				if(e.getEntity() instanceof Player) {
					Player p = (Player) e.getEntity();
					if(!e.isCancelled()) {
						if(PlayerCompat.getItemInHand(getPlayer()).getType().equals(Material.IRON_INGOT)) {
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
					}
				}
			}
		}
	}

	@Override
	public void onRestrictClear() {}

}
