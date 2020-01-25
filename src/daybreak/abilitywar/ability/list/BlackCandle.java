package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Random;

@AbilityManifest(Name = "검은 양초", Rank = Rank.A, Species = Species.OTHERS)
public class BlackCandle extends AbilityBase {

	public static final SettingObject<Integer> ChanceConfig = new SettingObject<Integer>(BlackCandle.class, "Chance", 35,
			"# 대미지를 받았을 시 체력을 회복할 확률") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 100;
		}

	};

	public BlackCandle(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f독 대미지와 시듦 대미지를 받지 않으며, 스턴 공격을 받지 않습니다."),
				ChatColor.translateAlternateColorCodes('&', "&f대미지를 받았을 때 " + ChanceConfig.getValue() + "% 확률로 체력을 1.5칸 회복합니다."));
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		return false;
	}

	private final int chance = ChanceConfig.getValue();

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			if (e.getCause().equals(EntityDamageEvent.DamageCause.WITHER) || e.getCause().equals(EntityDamageEvent.DamageCause.POISON)) {
				e.setCancelled(true);
			}
			Random r = new Random();
			if (r.nextInt(100) + 1 <= chance) {
				double Health = getPlayer().getHealth() + 1.5;
				if (Health > 20.0) Health = 20.0;

				if (!getPlayer().isDead()) {
					getPlayer().setHealth(Health);
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(1, Tone.F));
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			Random r = new Random();
			if (r.nextInt(100) + 1 <= chance) {
				double Health = getPlayer().getHealth() + 1.5;
				if (Health > 20.0) Health = 20.0;

				if (!getPlayer().isDead()) {
					getPlayer().setHealth(Health);
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(1, Tone.F));
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageByBlockEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			Random r = new Random();
			if (r.nextInt(100) + 1 <= chance) {
				double Health = getPlayer().getHealth() + 1.5;
				if (Health > 20.0) Health = 20.0;

				if (!getPlayer().isDead()) {
					getPlayer().setHealth(Health);
					SoundLib.PIANO.playInstrument(getPlayer(), Note.flat(1, Tone.F));
				}
			}
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
