package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionType;

import java.util.Random;

@AbilityManifest(Name = "양조사", Rank = Rank.B, Species = Species.HUMAN)
public class Brewer extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Brewer.class, "Cooldown", 50,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Brewer(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 랜덤한 포션 세개를 얻습니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !cooldownTimer.isCooldown()) {
			Inventory brewingGUI = Bukkit.createInventory(null, InventoryType.BREWING, "");
			Player player = getPlayer();
			Random random = new Random();
			for (int i = 0; i < 3; i++) {
				brewingGUI.setItem(i, new ItemLib.PotionBuilder(
						PotionType.values()[random.nextInt(PotionType.values().length)],
						ItemLib.PotionBuilder.PotionShape.values()[random.nextInt(ItemLib.PotionBuilder.PotionShape.values().length)])
						.setExtended(random.nextBoolean())
						.setUpgraded(random.nextBoolean())
						.build(1));
			}
			SoundLib.ENTITY_ILLUSIONER_CAST_SPELL.playSound(player);
			ParticleLib.SPELL_WITCH.spawnParticle(player.getLocation(), 2, 2, 2, 10);
			player.openInventory(brewingGUI);
			cooldownTimer.start();

			return true;
		}

		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
