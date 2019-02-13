package Marlang.AbilityWar.Ability.List;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.Timer.CooldownTimer;
import Marlang.AbilityWar.Ability.Timer.DurationTimer;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.LocationUtil;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.TimerBase;
import Marlang.AbilityWar.Utils.Library.SoundLib;

public class Pumpkin extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>("호박", "Cooldown", 80, 
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public static SettingObject<Integer> DurationConfig = new SettingObject<Integer>("호박", "Duration", 15, 
			"# 지속 시간") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};
	
	public Pumpkin(Player player) {
		super(player, "호박", Rank.C,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 주변 30블록 내에 있었던 플레이어들에게 " + DurationConfig.getValue() + "초간"),
				ChatColor.translateAlternateColorCodes('&', "&f귀속 저주가 걸린 호박을 씌웁니다. " + Messager.formatCooldown(CooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f♪ 호박 같은 네 얼굴 ♪"));
		
		Song.setPeriod(3);
	}
	
	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());
	
	TimerBase Song = new TimerBase(13) {
		
		ArrayList<Player> Players;
		
		Integer Count;
		
		@Override
		public void TimerStart(Data<?>... args) {
			if(args.length > 0) {
				Set<?> list = args[0].getValue(Set.class);
				if(list != null) {
					Players = new ArrayList<Player>();
					
					for(Object o : list) {
						if(o instanceof Player) {
							Players.add((Player) o);
						}
					}
				} else {
					this.StopTimer(true);
				}
			} else {
				this.StopTimer(true);
			}
			
			Count = 1;
		}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			if(Count.equals(1)) {
				SoundLib.BELL.playInstrument(Players, Note.natural(0, Tone.D));
			} else if(Count.equals(3)) {
				SoundLib.BELL.playInstrument(Players, Note.natural(0, Tone.E));
			} else if(Count.equals(4)) {
				SoundLib.BELL.playInstrument(Players, Note.sharp(1, Tone.F));
			} else if(Count.equals(7)) {
				SoundLib.BELL.playInstrument(Players, Note.sharp(1, Tone.F));
			} else if(Count.equals(10)) {
				SoundLib.BELL.playInstrument(Players, Note.natural(1, Tone.G));
			} else if(Count.equals(12)) {
				SoundLib.BELL.playInstrument(Players, Note.sharp(1, Tone.F));
			} else if(Count.equals(13)) {
				SoundLib.BELL.playInstrument(Players, Note.natural(0, Tone.E));
			}
			
			Count++;
		}
		
		@Override
		public void TimerEnd() {}
		
	};
	
	DurationTimer Duration = new DurationTimer(this, DurationConfig.getValue(), Cool) {
		
		HashMap<Player, ItemStack> Players;
		
		@SuppressWarnings("rawtypes")
		@Override
		public void TimerStart(Data<?>... args) {
			Players = new HashMap<Player, ItemStack>();
			LocationUtil.getNearbyPlayers(getPlayer(), 30, 30).stream().forEach(p -> Players.put(p, p.getInventory().getHelmet()));
			Song.StartTimer(new Data<Set>(Players.keySet(), Set.class));
			
			super.TimerStart(args);
		}
		
		@Override
		public void DurationSkill(Integer Seconds) {
			ItemStack Pumpkin = getPumpkin(Seconds);
			Players.keySet().stream().forEach(p -> p.getInventory().setHelmet(Pumpkin));
		}
		
		@Override
		public void TimerEnd() {
			Players.keySet().stream().forEach(p -> p.getInventory().setHelmet(Players.get(p)));
			
			super.TimerEnd();
		}
		
		private ItemStack getPumpkin(Integer Time) {
			ItemStack Pumpkin = new ItemStack(Material.PUMPKIN);
			ItemMeta PumpkinMeta = Pumpkin.getItemMeta();
			PumpkinMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6호박"));
			PumpkinMeta.setLore(Messager.getStringList(
					ChatColor.translateAlternateColorCodes('&', "&f♪ 호박 같은 네 얼굴 ♪"),
					ChatColor.translateAlternateColorCodes('&', "&f남은 시간&7: &a" + Time + "초")
					));
			Pumpkin.setItemMeta(PumpkinMeta);
			Pumpkin.addEnchantment(Enchantment.BINDING_CURSE, 1);
			
			return Pumpkin;
		}
		
	};

	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.RightClick)) {
				if(!Duration.isDuration() && !Cool.isCooldown()) {
					Duration.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {}

	@Override
	public void AbilityEvent(EventType type) {}

}
