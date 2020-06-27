package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Support;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion.Version;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionType;

@Support(min = Version.v1_11_R1)
@AbilityManifest(name = "양조사", rank = Rank.B, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 임의의 포션 세 개를 얻습니다. $[CooldownConfig]"
})
public class Brewer extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(Brewer.class, "Cooldown", 50,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public Brewer(Participant participant) {
		super(participant);
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

}
