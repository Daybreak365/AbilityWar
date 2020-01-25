package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.MaterialLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.EnchantLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

@AbilityManifest(Name = "호박", Rank = Rank.C, Species = Species.OTHERS)
public class Pumpkin extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Pumpkin.class, "Cooldown", 80,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Pumpkin.class, "Duration", 15,
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

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private final Timer Song = new Timer(13) {

		private ArrayList<Player> Players;

		private Integer Count;

		@Override
		public void onStart() {
			this.Players = new ArrayList<Player>(Pumpkin.this.Players.keySet());

			Count = 1;
		}

		@Override
		public void onProcess(int count) {
			if (Count.equals(1)) {
				SoundLib.BELL.playInstrument(Players, Note.natural(0, Tone.D));
				SoundLib.BELL.playInstrument(getPlayer(), Note.natural(0, Tone.D));
			} else if (Count.equals(3)) {
				SoundLib.BELL.playInstrument(Players, Note.natural(0, Tone.E));
				SoundLib.BELL.playInstrument(getPlayer(), Note.natural(0, Tone.E));
			} else if (Count.equals(4)) {
				SoundLib.BELL.playInstrument(Players, Note.sharp(1, Tone.F));
				SoundLib.BELL.playInstrument(getPlayer(), Note.sharp(1, Tone.F));
			} else if (Count.equals(7)) {
				SoundLib.BELL.playInstrument(Players, Note.sharp(1, Tone.F));
				SoundLib.BELL.playInstrument(getPlayer(), Note.sharp(1, Tone.F));
			} else if (Count.equals(10)) {
				SoundLib.BELL.playInstrument(Players, Note.natural(1, Tone.G));
				SoundLib.BELL.playInstrument(getPlayer(), Note.natural(1, Tone.G));
			} else if (Count.equals(12)) {
				SoundLib.BELL.playInstrument(Players, Note.sharp(1, Tone.F));
				SoundLib.BELL.playInstrument(getPlayer(), Note.sharp(1, Tone.F));
			} else if (Count.equals(13)) {
				SoundLib.BELL.playInstrument(Players, Note.natural(0, Tone.E));
				SoundLib.BELL.playInstrument(getPlayer(), Note.natural(0, Tone.E));
			}

			Count++;
		}

		@Override
		public void onEnd() {
		}

	}.setPeriod(3);

	private HashMap<Player, ItemStack> Players;

	private final DurationTimer Duration = new DurationTimer(DurationConfig.getValue(), cooldownTimer) {

		@Override
		public void onDurationStart() {
			Players = new HashMap<Player, ItemStack>();
			LocationUtil.getNearbyPlayers(getPlayer(), 30, 30).stream().forEach(p -> Players.put(p, p.getInventory().getHelmet()));
			Song.startTimer();
		}

		@Override
		public void onDurationProcess(int seconds) {
			ItemStack Pumpkin = getPumpkin(seconds);
			Players.keySet().stream().forEach(p -> p.getInventory().setHelmet(Pumpkin));
		}

		@Override
		public void onDurationEnd() {
			Players.keySet().forEach(p -> p.getInventory().setHelmet(Players.get(p)));
		}

		@Override
		public void onSilentEnd() {
			Players.keySet().forEach(p -> p.getInventory().setHelmet(Players.get(p)));
		}

		private ItemStack getPumpkin(Integer Time) {
			ItemStack Pumpkin = new ItemStack(MaterialLib.CARVED_PUMPKIN.getMaterial());
			ItemMeta PumpkinMeta = Pumpkin.getItemMeta();
			PumpkinMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6호박"));
			PumpkinMeta.setLore(Messager.asList(
					ChatColor.translateAlternateColorCodes('&', "&f♪ 호박 같은 네 얼굴 ♪"),
					ChatColor.translateAlternateColorCodes('&', "&f남은 시간&7: &a" + Time + "초")
			));
			Pumpkin.setItemMeta(PumpkinMeta);
			EnchantLib.BINDING_CURSE.addUnsafeEnchantment(Pumpkin, 1);
			return Pumpkin;
		}

	};

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (ct.equals(ClickType.RIGHT_CLICK)) {
				if (!Duration.isDuration() && !cooldownTimer.isCooldown()) {
					Duration.startTimer();

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
