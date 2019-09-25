package daybreak.abilitywar.ability.list;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.EffectLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.thread.TimerBase;

@AbilityManifest(Name = "검은 양초", Rank = Rank.A, Species = Species.OTHERS)
public class BlackCandle extends AbilityBase {

	public static SettingObject<Integer> ChanceConfig = new SettingObject<Integer>(BlackCandle.class, "Chance", 35,
			"# 데미지를 받았을 시 체력을 회복할 확률") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100;
		}
		
	};

	public BlackCandle(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f디버프를 받지 않으며, 스턴 공격을 받지 않습니다."),
				ChatColor.translateAlternateColorCodes('&', "&f또한, 데미지를 받았을 때 " + ChanceConfig.getValue() + "% 확률로 체력을 1.5칸 회복합니다."));
	}

	private TimerBase NoDebuff = new TimerBase() {
		
		@Override
		public void onStart() {}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			EffectLib.BAD_OMEN.removePotionEffect(getPlayer());
			EffectLib.BLINDNESS.removePotionEffect(getPlayer());
			EffectLib.CONFUSION.removePotionEffect(getPlayer());
			EffectLib.GLOWING.removePotionEffect(getPlayer());
			EffectLib.HARM.removePotionEffect(getPlayer());
			EffectLib.HUNGER.removePotionEffect(getPlayer());
			EffectLib.POISON.removePotionEffect(getPlayer());
			EffectLib.SLOW.removePotionEffect(getPlayer());
			EffectLib.SLOW_DIGGING.removePotionEffect(getPlayer());
			EffectLib.UNLUCK.removePotionEffect(getPlayer());
			EffectLib.WEAKNESS.removePotionEffect(getPlayer());
			EffectLib.WITHER.removePotionEffect(getPlayer());
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(1);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	private final int Chance = ChanceConfig.getValue();

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			Random r = new Random();
			if(r.nextInt(100) + 1 <= Chance) {
				double Health = getPlayer().getHealth() + 1.5;
				if(Health > 20.0) Health = 20.0;
				
				if(!getPlayer().isDead()) {
					getPlayer().setHealth(Health);
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(1, Tone.F));
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			Random r = new Random();
			if(r.nextInt(100) + 1 <= Chance) {
				double Health = getPlayer().getHealth() + 1.5;
				if(Health > 20.0) Health = 20.0;
				
				if(!getPlayer().isDead()) {
					getPlayer().setHealth(Health);
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(1, Tone.F));
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByBlockEvent e) {
		if(e.getEntity().equals(getPlayer())) {
			Random r = new Random();
			if(r.nextInt(100) + 1 <= Chance) {
				double Health = getPlayer().getHealth() + 1.5;
				if(Health > 20.0) Health = 20.0;
				
				if(!getPlayer().isDead()) {
					getPlayer().setHealth(Health);
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(1, Tone.F));
				}
			}
		}
	}
	
	@Override
	public void onRestrictClear() {
		NoDebuff.StartTimer();
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
