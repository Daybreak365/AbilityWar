package DayBreak.AbilityWar.Ability.List;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.EffectLib;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Library.Item.EnchantLib;
import DayBreak.AbilityWar.Utils.Library.Item.MaterialLib;
import DayBreak.AbilityWar.Utils.Library.Packet.TitlePacket;
import DayBreak.AbilityWar.Utils.Math.NumberUtil;
import DayBreak.AbilityWar.Utils.Math.NumberUtil.NumberStatus;
import DayBreak.AbilityWar.Utils.VersionCompat.ServerVersion;

@AbilityManifest(Name = "?ó¨?†ú", Rank = Rank.B)
public class TheEmpress extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(TheEmpress.class, "Cooldown", 70, 
			"# Ïø®Ì??ûÑ") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public static SettingObject<Boolean> EasterEggConfig = new SettingObject<Boolean>(TheEmpress.class, "EasterEgg", true, 
			"# ?ù¥?ä§?Ñ∞?óêÍ∑? ?ôú?Ñ±?ôî ?ó¨Î∂?",
			"# falseÎ°? ?Ñ§?†ï?ïòÎ©? ?ù¥?ä§?Ñ∞?óêÍ∑∏Í? Î∞úÎèô?êòÏß? ?ïä?äµ?ãà?ã§.") {
		
		@Override
		public boolean Condition(Boolean value) {
			return true;
		}
		
	};
	
	public TheEmpress(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&fÏ≤†Í¥¥Î•? ?ö∞?Å¥Î¶??ïòÎ©? ?òÑ?û¨ Ï¢åÌëú?óê ?î∞?ùº Î≤ÑÌîÑ ?òπ?? ?ïÑ?ù¥?Öú?ùÑ ?ñª?äµ?ãà?ã§. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&fX &7: &a+&f, Y &7: &a+ &f?û° ?ûò   10Ï¥? | ?Ç†Ïπ¥Î°ú?? IV ?ã§?ù¥?ïÑ Í≤?"),
				ChatColor.translateAlternateColorCodes('&', "&fX &7: &a+&f, Y &7: &c- &f?û° ???ï≠ 20Ï¥? | " + ((ServerVersion.getVersion() >= 9) ? "Î∞©Ìå®" : "Í±∞Î?∏Ï§Ñ")),
				ChatColor.translateAlternateColorCodes('&', "&fX &7: &c-&f, Y &7: &a+ &f?û° ?ã†?Üç 30Ï¥? | Î¨¥Ìïú ?ôú"),
				ChatColor.translateAlternateColorCodes('&', "&fX &7: &c-&f, Y &7: &c- &f?û° ?û¨?Éù 20Ï¥? | ?ô©Í∏àÏÇ¨Í≥?"));
	}
	
	boolean EasterEgg = !EasterEggConfig.getValue();
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Location l = getPlayer().getLocation();
					
					NumberStatus X = NumberUtil.getNumberStatus(l.getX());
					NumberStatus Z = NumberUtil.getNumberStatus(l.getZ());
					
					Random random = new Random();
					boolean bool = random.nextBoolean();
					
					if(X.isPlus() && Z.isPlus()) {
						if(bool) {
							EffectLib.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 200, 1, true);
						} else {
							ItemStack is = new ItemStack(Material.DIAMOND_SWORD);
							getPlayer().getInventory().addItem(EnchantLib.DAMAGE_ALL.addEnchantment(is, 4));
						}
					} else if(X.isPlus() && Z.isMinus()) {
						if(bool) {
							EffectLib.DAMAGE_RESISTANCE.addPotionEffect(getPlayer(), 400, 1, true);
						} else {
							if(ServerVersion.getVersion() >= 9) {
								getPlayer().getInventory().addItem(new ItemStack(Material.SHIELD));
							} else {
								getPlayer().getInventory().addItem(MaterialLib.COBWEB.getItem());
							}
						}
					} else if(X.isMinus() && Z.isPlus()) {
						if(bool) {
							EffectLib.SPEED.addPotionEffect(getPlayer(), 600, 1, true);
						} else {
							ItemStack is = new ItemStack(Material.BOW);
							getPlayer().getInventory().addItem(EnchantLib.ARROW_INFINITE.addEnchantment(is, 1));
						}
					} else if(X.isMinus() && Z.isMinus()) {
						if(bool) {
							EffectLib.REGENERATION.addPotionEffect(getPlayer(), 400, 1, true);
						} else {
							getPlayer().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
						}
					} else if(X.isZero() && Z.isZero()) {
						if(!EasterEgg) {
							EasterEgg = true;
							TitlePacket title = new TitlePacket(ChatColor.translateAlternateColorCodes('&', "&a?ó¨?†ú?ùò Í∞??ò∏"),
									"?ó¨?†ú?ùò Í∞??ò∏?óê ?ùò?ï¥ Î™®Îì† ?îå?†à?ù¥?ñ¥?ùò ?ä•?†• Ïø®Ì??ûÑ?ù¥ Ï¥àÍ∏∞?ôî?êò?óà?äµ?ãà?ã§.", 15, 80, 15);
							title.Broadcast();
							
							SoundLib.UI_TOAST_CHALLENGE_COMPLETE.broadcastSound();
							
							CooldownTimer.ResetCool();
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
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
