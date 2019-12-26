package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.ItemLib.PotionBuilder;
import daybreak.abilitywar.utils.library.item.ItemLib.PotionBuilder.PotionShape;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionType;

import java.util.Random;

@AbilityManifest(Name = "양조사", Rank = Rank.B, Species = Species.HUMAN)
public class Brewer extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Brewer.class, "Cooldown", 7,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Brewer(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 랜덤한 포션 하나를 얻습니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	private final CooldownTimer Cool = new CooldownTimer(CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (ct.equals(ClickType.RIGHT_CLICK)) {
				if (!Cool.isCooldown()) {
					Player p = getPlayer();

					Random r = new Random();

					PotionType type = PotionType.values()[r.nextInt(PotionType.values().length)];
					try {
						p.getInventory().addItem(new PotionBuilder(type, PotionShape.values()[r.nextInt(PotionShape.values().length)])
								.setExtended(r.nextBoolean()).setUpgraded(r.nextBoolean()).getItemStack(1));
					} catch (Exception ignored) {
					}
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5오늘은 어떤 포션을 마실까..."));
					SoundLib.ENTITY_ILLUSIONER_CAST_SPELL.playSound(p);
					ParticleLib.SPELL_WITCH.spawnParticle(p.getLocation(), 2, 2, 2, 10);

					Cool.startTimer();

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
