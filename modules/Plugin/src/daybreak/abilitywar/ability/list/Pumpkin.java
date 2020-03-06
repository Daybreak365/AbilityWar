package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion.Version;
import daybreak.abilitywar.utils.library.MaterialX;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Support(Version.v1_11_R1)
@AbilityManifest(Name = "호박", Rank = Rank.C, Species = Species.OTHERS)
public class Pumpkin extends AbilityBase implements ActiveHandler {

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

	private final Timer music = new Timer(32) {

		private List<Player> players;

		private int count;

		@Override
		public void onStart() {
			this.players = new ArrayList<>(Pumpkin.this.players.keySet());
			players.add(getPlayer());
			count = 1;
		}

		@Override
		public void run(int count) {
			switch (this.count) {
				case 1:
					SoundLib.BELL.playInstrument(players, Note.natural(0, Tone.D));
					break;
				case 3:
				case 20:
					SoundLib.BELL.playInstrument(players, Note.natural(0, Tone.E));
					break;
				case 13:
					SoundLib.BELL.playInstrument(players, Note.natural(0, Tone.A));
					SoundLib.BELL.playInstrument(players, Note.natural(0, Tone.E));
					break;
				case 4:
				case 7:
				case 32:
					SoundLib.BELL.playInstrument(players, Note.natural(0, Tone.D));
					SoundLib.BELL.playInstrument(players, Note.sharp(1, Tone.F));
					break;
				case 12:
				case 22:
					SoundLib.BELL.playInstrument(players, Note.sharp(1, Tone.F));
					break;
				case 10:
				case 31:
					SoundLib.BELL.playInstrument(players, Note.natural(1, Tone.G));
					break;
				case 23:
				case 26:
					SoundLib.BELL.playInstrument(players, Note.natural(0, Tone.A));
					SoundLib.BELL.playInstrument(players, Note.natural(1, Tone.G));
					break;
				case 29:
					SoundLib.BELL.playInstrument(players, Note.natural(1, Tone.A));
					break;
			}

			this.count++;
		}

	}.setPeriod(TimeUnit.TICKS, 3);

	private Map<Player, ItemStack> players;

	private final DurationTimer durationTimer = new DurationTimer(DurationConfig.getValue(), cooldownTimer) {

		@Override
		public void onDurationStart() {
			players = new HashMap<>();
			for (Player p : LocationUtil.getNearbyPlayers(getPlayer(), 30, 30)) {
				players.put(p, p.getInventory().getHelmet());
			}
			music.start();
		}

		@Override
		public void onDurationProcess(int seconds) {
			ItemStack pumpkin = getPumpkin(seconds);
			players.keySet().forEach(p -> p.getInventory().setHelmet(pumpkin));
		}

		@Override
		public void onDurationEnd() {
			players.forEach((Player p, ItemStack stack) -> p.getInventory().setHelmet(stack));
		}

		@Override
		public void onDurationSilentEnd() {
			players.forEach((Player p, ItemStack stack) -> p.getInventory().setHelmet(stack));
		}

		private ItemStack getPumpkin(int time) {
			ItemStack stack = MaterialX.CARVED_PUMPKIN.parseItem();
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6호박"));
			meta.setLore(Arrays.asList(
					ChatColor.translateAlternateColorCodes('&', "&d♪ &f호박 같은 네 얼굴 &d♪"),
					ChatColor.translateAlternateColorCodes('&', "&f남은 시간&5: &d" + time + "초")
			));
			stack.setItemMeta(meta);
			EnchantLib.BINDING_CURSE.addUnsafeEnchantment(stack, 1);
			return stack;
		}

	};

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !durationTimer.isDuration() && !cooldownTimer.isCooldown()) {
			durationTimer.start();
			return true;
		}
		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
