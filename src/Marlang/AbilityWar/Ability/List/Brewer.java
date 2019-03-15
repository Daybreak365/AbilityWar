package Marlang.AbilityWar.Ability.List;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityManifest;
import Marlang.AbilityWar.Ability.AbilityManifest.Rank;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Game.Games.AbstractGame.Participant;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.ParticleLib;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Library.Item.ItemLib.PotionBuilder;
import Marlang.AbilityWar.Utils.Library.Item.ItemLib.PotionBuilder.PotionShape;

@AbilityManifest(Name = "양조사", Rank = Rank.B)
public class Brewer extends AbilityBase {
	
	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Brewer.class, "Cooldown", 45, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};
	
	public Brewer(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 랜덤한 포션 하나를 얻습니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Player p = getPlayer();
					
					Random r = new Random();
					
					PotionType type = PotionType.values()[r.nextInt(PotionType.values().length)];
					ItemStack Potion = new PotionBuilder(type, PotionShape.values()[r.nextInt(PotionShape.values().length)])
							.setExtended(r.nextBoolean()).setUpgraded(r.nextBoolean()).getItemStack(1);
					
					if(Potion != null) {
						p.getInventory().addItem(Potion);
					}
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

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
