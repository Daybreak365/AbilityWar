package DayBreak.AbilityWar.Ability.List;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionType;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.ParticleLib;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Library.Item.ItemLib.PotionBuilder;
import DayBreak.AbilityWar.Utils.Library.Item.ItemLib.PotionBuilder.PotionShape;

@AbilityManifest(Name = "양조사", Rank = Rank.B, Species = Species.HUMAN)
public class Brewer extends AbilityBase {
	
	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Brewer.class, "Cooldown", 7, 
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
	
	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Player p = getPlayer();
					
					Random r = new Random();
					
					PotionType type = PotionType.values()[r.nextInt(PotionType.values().length)];
					try {
						p.getInventory().addItem(new PotionBuilder(type, PotionShape.values()[r.nextInt(PotionShape.values().length)])
								.setExtended(r.nextBoolean()).setUpgraded(r.nextBoolean()).getItemStack(1));
					} catch (Exception e) {}
					Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&5오늘은 어떤 포션을 마실까..."));
					SoundLib.ENTITY_ILLUSIONER_CAST_SPELL.playSound(p);
					ParticleLib.SPELL_WITCH.spawnParticle(p.getLocation(), 2, 2, 2, 10);
					
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
