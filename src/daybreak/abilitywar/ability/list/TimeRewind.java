package daybreak.abilitywar.ability.list;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.timer.CooldownTimer;
import daybreak.abilitywar.ability.timer.DurationTimer;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.events.ParticipantDeathEvent;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.database.PushingArray;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.thread.TimerBase;

@AbilityManifest(Name = "시간 역행", Rank = Rank.S, Species = Species.HUMAN)
public class TimeRewind extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(TimeRewind.class, "Cooldown", 100,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static final SettingObject<Integer> TimeConfig = new SettingObject<Integer>(TimeRewind.class, "Time", 5,
			"# 능력을 사용했을 때 몇초 전으로 돌아갈지 설정합니다.") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};

	public TimeRewind(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 시간을 역행해 " + TimeConfig.getValue() + "초 전으로 돌아갑니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}
	
	private final CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	private final int Time = TimeConfig.getValue();
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.IRON_INGOT)) {
			if(ct.equals(ClickType.RIGHT_CLICK)) {
				if(!Cool.isCooldown()) {
					Skill.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	@SubscribeEvent
	public void onPlayerDeath(ParticipantDeathEvent e) {
		if(e.getParticipant().equals(getParticipant())) {
			array = new PushingArray<>(PlayerData.class, Time * 20);
		}
	}
	
	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity().equals(getPlayer()) && Rewinding) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getDamager().equals(getPlayer()) && Rewinding) {
			e.setCancelled(true);
		}
	}
	
	private boolean Rewinding = false;
	
	private PushingArray<PlayerData> array = new PushingArray<>(PlayerData.class, Time * 20);
	
	private final DurationTimer Skill = new DurationTimer(this, Time * 10, Cool) {
		
		private List<PlayerData> list;
		
		@Override
		public void onDurationStart() {
			Rewinding = true;
			this.list = array.toList();
		}
		
		@Override
		public void onDurationProcess(int seconds) {
			PlayerData data = list.get((Time * 10 - seconds));
			if(data != null && data.getHealth() > 0) {
				getPlayer().teleport(data.getLocation());
				if(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() >= data.getHealth()) getPlayer().setHealth(data.getHealth());
			}
		}
		
		@Override
		public void onDurationEnd() {
			Rewinding = false;
			SoundLib.BELL.playInstrument(getPlayer(), Note.natural(0, Tone.D));
			SoundLib.BELL.playInstrument(getPlayer(), Note.sharp(0, Tone.F));
			SoundLib.BELL.playInstrument(getPlayer(), Note.natural(1, Tone.A));
		}
		
	}.setPeriod(1);
	
	private final TimerBase Save = new TimerBase() {
		
		@Override
		public void onStart() {}
		
		@Override
		public void onProcess(int Seconds) {
			array.add(new PlayerData(getPlayer()));
		}

		@Override
		public void onEnd() {}
		
	}.setPeriod(2);
	
	@Override
	public void onRestrictClear() {
		Save.StartTimer();
	}
	
	private class PlayerData {
		
		final Location location;
		final Double Health;
		
		public PlayerData(Player p) {
			this.location = p.getLocation();
			this.Health = p.getHealth();
		}

		public Location getLocation() {
			return location;
		}

		public Double getHealth() {
			return Health;
		}
		
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
