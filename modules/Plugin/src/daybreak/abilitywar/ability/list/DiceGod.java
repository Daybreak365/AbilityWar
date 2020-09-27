package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.Random;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@AbilityManifest(name = "다이스 갓", rank = Rank.A, species = Species.GOD, explain = {
		"철괴를 우클릭하면 §c재생 §f/ §6힘 §f/ §3저항 §f/ §5시듦 §f/ §8구속 §f/ §7나약함 §f효과 중 하나를",
		"7초간 받습니다. $[COOLDOWN_CONFIG]",
		"공격을 받았을 때 1/6 확률로 대미지를 받는 대신 대미지만큼 체력을 회복합니다."
})
@Tips(tip = {
		"사용자의 운에 따라 이득이 될 수도, 해가 될 수도 있습니다.",
		"좋은 버프가 나왔을 때 바로 공격하고, 디버프가 나왔을 때 바로",
		"빠져나올 수 있는 판단력도 중요합니다."
}, strong = {
		@Description(subject = "운", explain = {
				"사용자의 운이 곧 강점입니다.",
				"오늘 느낌이 좋으신가요? 이 능력은 당신에게 딱입니다."
		})
}, weak = {
		@Description(subject = "운", explain = {
				"사용자의 운이 곧 약점입니다.",
				"오늘 느낌이 좋지 않으신가요? 이 능력은 피하세요."
		})
}, stats = @Stats(offense = Level.THREE, survival = Level.THREE, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.THREE), difficulty = Difficulty.EASY)
public class DiceGod extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(DiceGod.class, "Cooldown", 14,
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

	private static final Random random = new Random();
	private static final PlayerConsumer[] events = {
			new PlayerConsumer() {
				@Override
				public void accept(Player player) {
					player.sendMessage("§c재생 §f효과를 받습니다.");
					PotionEffects.REGENERATION.addPotionEffect(player, 140, 1, true);
				}
			},
			new PlayerConsumer() {
				@Override
				public void accept(Player player) {
					player.sendMessage("§6힘 §f효과를 받습니다.");
					PotionEffects.INCREASE_DAMAGE.addPotionEffect(player, 140, 1, true);
				}
			},
			new PlayerConsumer() {
				@Override
				public void accept(Player player) {
					player.sendMessage("§3저항 §f효과를 받습니다.");
					PotionEffects.DAMAGE_RESISTANCE.addPotionEffect(player, 140, 1, true);
				}
			},
			new PlayerConsumer() {
				@Override
				public void accept(Player player) {
					player.sendMessage("§5시듦 §f효과를 받습니다.");
					PotionEffects.WITHER.addPotionEffect(player, 140, 1, true);
				}
			},
			new PlayerConsumer() {
				@Override
				public void accept(Player player) {
					player.sendMessage("§8구속 §f효과를 받습니다.");
					PotionEffects.SLOW.addPotionEffect(player, 140, 1, true);
				}
			},
			new PlayerConsumer() {
				@Override
				public void accept(Player player) {
					player.sendMessage("§7나약함 §f효과를 받습니다.");
					PotionEffects.WEAKNESS.addPotionEffect(player, 140, 1, true);
				}
			}
	};

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

	public DiceGod(Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown()) {
			random.pick(events).accept(getPlayer());
			cooldownTimer.start();
			return true;
		}
		return false;
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (random.nextInt(6) == 0) {
			if (!getPlayer().isDead()) {
				getPlayer().setHealth(Math.min(getPlayer().getHealth() + e.getFinalDamage(), getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			}
			e.setDamage(0);
		}
	}

	private interface PlayerConsumer extends Consumer<Player> {}

}
