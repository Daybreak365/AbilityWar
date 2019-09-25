package daybreak.abilitywar.ability.list;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.timer.CooldownTimer;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.EffectLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.EnchantLib;
import daybreak.abilitywar.utils.library.item.MaterialLib;
import daybreak.abilitywar.utils.library.tItle.Title;
import daybreak.abilitywar.utils.math.NumberUtil;
import daybreak.abilitywar.utils.math.NumberUtil.NumberStatus;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;

@AbilityManifest(Name = "여제", Rank = Rank.B, Species = Species.HUMAN)
public class TheEmpress extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(TheEmpress.class, "Cooldown", 70, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public static SettingObject<Boolean> EasterEggConfig = new SettingObject<Boolean>(TheEmpress.class, "EasterEgg", true, 
			"# 이스터에그 활성화 여부",
			"# false로 설정하면 이스터에그가 발동되지 않습니다.") {
		
		@Override
		public boolean Condition(Boolean value) {
			return true;
		}
		
	};
	
	public TheEmpress(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 현재 좌표에 따라 버프 혹은 아이템을 얻습니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&fX &7: &a+&f, Z &7: &a+ &f➡ 힘   10초 | 날카로움 II 다이아 검"),
				ChatColor.translateAlternateColorCodes('&', "&fX &7: &a+&f, Z &7: &c- &f➡ 저항 20초 | " + ((ServerVersion.getVersion() >= 9) ? "방패" : "거미줄")),
				ChatColor.translateAlternateColorCodes('&', "&fX &7: &c-&f, Z &7: &a+ &f➡ 신속 30초 | 무한 활"),
				ChatColor.translateAlternateColorCodes('&', "&fX &7: &c-&f, Z &7: &c- &f➡ 재생 20초 | 황금사과"));
	}
	
	private boolean EasterEgg = !EasterEggConfig.getValue();
	
	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
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
							getPlayer().getInventory().addItem(EnchantLib.DAMAGE_ALL.addEnchantment(is, 2));
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
							Title title = new Title(ChatColor.translateAlternateColorCodes('&', "&a여제의 가호"),
									"여제의 가호에 의해 모든 플레이어의 능력 쿨타임이 초기화되었습니다.", 15, 80, 15);
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
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}