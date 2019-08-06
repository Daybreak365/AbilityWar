package DayBreak.AbilityWar.Ability.List;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Ability.Timer.DurationTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Library.Item.EnchantLib;
import DayBreak.AbilityWar.Utils.Library.Item.MaterialLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "호박", Rank = Rank.C, Species = Species.HUMAN)
public class Pumpkin extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Pumpkin.class, "Cooldown", 80, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Pumpkin.class, "Duration", 15, 
			"# 지속 시간") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};
	
	public Pumpkin(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 주변 30블록 내에 있었던 플레이어들에게 " + DurationConfig.getValue() + "초간"),
				ChatColor.translateAlternateColorCodes('&', "&f귀속 저주가 걸린 호박을 씌웁니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f♪ 호박 같은 네 얼굴 ♪"));
	}
	
	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	private TimerBase Song = new TimerBase(13) {
		
		private ArrayList<Player> Players;
		
		private Integer Count;
		
		@Override
		public void onStart() {
			this.Players = new ArrayList<Player>(Pumpkin.this.Players.keySet());
			
			Count = 1;
		}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			if(Count.equals(1)) {
				SoundLib.BELL.playInstrument(Players, Note.natural(0, Tone.D));
				SoundLib.BELL.playInstrument(getPlayer(), Note.natural(0, Tone.D));
			} else if(Count.equals(3)) {
				SoundLib.BELL.playInstrument(Players, Note.natural(0, Tone.E));
				SoundLib.BELL.playInstrument(getPlayer(), Note.natural(0, Tone.E));
			} else if(Count.equals(4)) {
				SoundLib.BELL.playInstrument(Players, Note.sharp(1, Tone.F));
				SoundLib.BELL.playInstrument(getPlayer(), Note.sharp(1, Tone.F));
			} else if(Count.equals(7)) {
				SoundLib.BELL.playInstrument(Players, Note.sharp(1, Tone.F));
				SoundLib.BELL.playInstrument(getPlayer(), Note.sharp(1, Tone.F));
			} else if(Count.equals(10)) {
				SoundLib.BELL.playInstrument(Players, Note.natural(1, Tone.G));
				SoundLib.BELL.playInstrument(getPlayer(), Note.natural(1, Tone.G));
			} else if(Count.equals(12)) {
				SoundLib.BELL.playInstrument(Players, Note.sharp(1, Tone.F));
				SoundLib.BELL.playInstrument(getPlayer(), Note.sharp(1, Tone.F));
			} else if(Count.equals(13)) {
				SoundLib.BELL.playInstrument(Players, Note.natural(0, Tone.E));
				SoundLib.BELL.playInstrument(getPlayer(), Note.natural(0, Tone.E));
			}
			
			Count++;
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(3);

	private HashMap<Player, ItemStack> Players;
	
	private DurationTimer Duration = new DurationTimer(this, DurationConfig.getValue(), Cool) {
		
		@Override
		public void onDurationStart() {
			Players = new HashMap<Player, ItemStack>();
			LocationUtil.getNearbyPlayers(getPlayer(), 30, 30).stream().forEach(p -> Players.put(p, p.getInventory().getHelmet()));
			Song.StartTimer();
		}
		
		@Override
		public void DurationProcess(Integer Seconds) {
			ItemStack Pumpkin = getPumpkin(Seconds);
			Players.keySet().stream().forEach(p -> p.getInventory().setHelmet(Pumpkin));
		}
		
		@Override
		public void onDurationEnd() {
			Players.keySet().stream().forEach(p -> p.getInventory().setHelmet(Players.get(p)));
		}
		
		private ItemStack getPumpkin(Integer Time) {
			ItemStack Pumpkin = new ItemStack(MaterialLib.CARVED_PUMPKIN.getMaterial());
			ItemMeta PumpkinMeta = Pumpkin.getItemMeta();
			PumpkinMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6호박"));
			PumpkinMeta.setLore(Messager.getStringList(
					ChatColor.translateAlternateColorCodes('&', "&f♪ 호박 같은 네 얼굴 ♪"),
					ChatColor.translateAlternateColorCodes('&', "&f남은 시간&7: &a" + Time + "초")
					));
			Pumpkin.setItemMeta(PumpkinMeta);
			EnchantLib.BINDING_CURSE.addUnsafeEnchantment(Pumpkin, 1);
			return Pumpkin;
		}
		
	}.setForcedStopNotice(true);

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.Iron_Ingot)) {
			if(ct.equals(ClickType.RightClick)) {
				if(!Duration.isDuration() && !Cool.isCooldown()) {
					Duration.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
