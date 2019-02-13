package Marlang.AbilityWar.Ability.List;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.NumberUtil;
import Marlang.AbilityWar.Utils.NumberUtil.NumberStatus;
import Marlang.AbilityWar.Utils.PacketUtil.TitleObject;
import Marlang.AbilityWar.Utils.Library.SoundLib;

public class TheEmpress extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("여제", "Cooldown", 70, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public static SettingObject<Boolean> EasterEggConfig = new SettingObject<Boolean>("여제", "EasterEgg", true, 
			"# 이스터에그 활성화 여부",
			"# false로 설정하면 이스터에그가 발동되지 않습니다.") {
		
		@Override
		public boolean Condition(Boolean value) {
			return true;
		}
		
	};
	
	public TheEmpress(Player player) {
		super(player, "여제", Rank.B,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 현재 좌표에 따라 버프 혹은 아이템을 얻습니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&fX &7: &a+&f, Y &7: &a+ &f➡ 힘   10초 | 날카로움 IV 다이아 검"),
				ChatColor.translateAlternateColorCodes('&', "&fX &7: &a+&f, Y &7: &c- &f➡ 저항 20초 | 방패"),
				ChatColor.translateAlternateColorCodes('&', "&fX &7: &c-&f, Y &7: &a+ &f➡ 신속 30초 | 무한 활"),
				ChatColor.translateAlternateColorCodes('&', "&fX &7: &c-&f, Y &7: &c- &f➡ 재생 20초 | 황금사과"));
	}
	
	boolean EasterEgg = !EasterEggConfig.getValue();
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Location l = getPlayer().getLocation();
					
					NumberStatus X = NumberUtil.getNumberStatus(l.getX());
					NumberStatus Z = NumberUtil.getNumberStatus(l.getZ());
					
					Random random = new Random();
					boolean bool = random.nextBoolean();
					
					if(X.isPlus() && Z.isPlus()) {
						if(bool) {
							getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0), true);
						} else {
							ItemStack is = new ItemStack(Material.DIAMOND_SWORD);
							is.addEnchantment(Enchantment.DAMAGE_ALL, 4);
							getPlayer().getInventory().addItem(is);
						}
					} else if(X.isPlus() && Z.isMinus()) {
						if(bool) {
							getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 400, 0), true);
						} else {
							getPlayer().getInventory().addItem(new ItemStack(Material.SHIELD));
						}
					} else if(X.isMinus() && Z.isPlus()) {
						if(bool) {
							getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1), true);
						} else {
							ItemStack is = new ItemStack(Material.BOW);
							is.addEnchantment(Enchantment.ARROW_INFINITE, 1);
							getPlayer().getInventory().addItem(is);
						}
					} else if(X.isMinus() && Z.isMinus()) {
						if(bool) {
							getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1), true);
						} else {
							getPlayer().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
						}
					} else if(X.isZero() && Z.isZero()) {
						if(!EasterEgg) {
							EasterEgg = true;
							TitleObject title = new TitleObject(ChatColor.translateAlternateColorCodes('&', "&a여제의 가호"), "여제의 가호에 의해 모든 플레이어의 능력 쿨타임이 초기화되었습니다.");
							title.Broadcast(15, 80, 15);
							
							SoundLib.UI_TOAST_CHALLENGE_COMPLETE.broadcastSound();
							
							CooldownTimer.CoolReset();
						}
					}
					
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
	public void AbilityEvent(EventType type) {}
	
}
