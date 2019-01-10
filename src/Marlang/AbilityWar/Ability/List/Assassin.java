package Marlang.AbilityWar.Ability.List;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.event.Event;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.CooldownTimer;
import Marlang.AbilityWar.Ability.Skill.SkillTimer;
import Marlang.AbilityWar.Ability.Skill.SkillTimer.SkillType;
import Marlang.AbilityWar.Utils.DistanceUtil;
import Marlang.AbilityWar.Utils.EffectUtil;
import Marlang.AbilityWar.Utils.Messager;

public class Assassin extends AbilityBase {
	
	public Assassin() {
		super("암살자", Rank.A,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 최대 4명의 적에게 텔레포트하며"),
				ChatColor.translateAlternateColorCodes('&', "&f데미지를 줍니다. " + Messager.formatCooldown(60)));
		Skill.setPeriod(5);
		
		registerTimer(Cool);
		registerTimer(Skill);
	}
	
	CooldownTimer Cool = new CooldownTimer(this, 60);
	
	SkillTimer Skill = new SkillTimer(this, 4, SkillType.Active, Cool) {
		
		ArrayList<Damageable> Entities = new ArrayList<Damageable>();
		
		@Override
		public void TimerStart() {
			Entities.addAll(DistanceUtil.getNearbyDamageableEntities(getPlayer(), 6, 3));
		}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			if(Entities.size() >= 1) {
				Damageable e = Entities.get(0);
				Entities.remove(e);
				getPlayer().teleport(e);
				e.damage(10, getPlayer());
				EffectUtil.sendSound(getPlayer(), Sound.ENTITY_PLAYER_ATTACK_SWEEP);
				EffectUtil.sendSound(getPlayer(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
			} else {
				this.StopTimer();
			}
		}
		
	};
	
	@Override
	public void ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					Skill.Execute();
				}
			}
		}
	}
	
	@Override
	public void PassiveSkill(Event event) {}
	
}
