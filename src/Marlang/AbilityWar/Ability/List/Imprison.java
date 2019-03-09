package Marlang.AbilityWar.Ability.List;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityManifest;
import Marlang.AbilityWar.Ability.AbilityManifest.Rank;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.GameManager.Game.AbstractGame.Participant;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Math.LocationUtil;

@AbilityManifest(Name = "구속", Rank = Rank.B)
public class Imprison extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("구속", "Cooldown", 25, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Imprison(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f상대방을 철괴로 타격하면 대상을 유리막 속에 가둡니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());

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
					if(!Cool.isCooldown()) {
						List<Block> blocks = LocationUtil.getBlocks(entity.getLocation(), 3, true, false, true);
						for(Block b : blocks) {
							b.setType(Material.GLASS);
						}
						
						Cool.StartTimer();
					}
				}
			} else {
				Cool.isCooldown();
			}
		}
	}
	
}
