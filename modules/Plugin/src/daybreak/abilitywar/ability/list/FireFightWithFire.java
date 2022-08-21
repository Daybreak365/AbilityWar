package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.SubscribeEvent.Priority;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

@AbilityManifest(name = "이열치열", rank = Rank.B, species = Species.HUMAN, explain = {
		"§7패시브 §8- §c이열치열§f: §c화염 §f피해를 입을 때, 피해만큼 체력을 회복합니다.",
		"§7철괴 우클릭 §8- §c불사지르기§f: 8초간 몸에 불을 붙입니다. $[COOLDOWN_CONFIG]"
}, summarize = {
		"§c화염 피해§f를 §a역회복§f합니다. §7철괴 우클릭§f 시 신체를 §c발화§f시킵니다."
})
@Tips(tip = {
		"모든 화염 대미지를 카운터, 거기에 더해 회복까지 할 수 있습니다.",
		"상대의 화염 공격에 당하는 것 말고도, 직접 불을 만들어 회복할 수",
		"있도록 부싯돌과 부시(라이터)를 만들어보세요!"
}, strong = {
		@Description(subject = "화염 대미지 무시", explain = {
				"화염 대미지를 무시합니다. 용암 대미지는 그대로 받으니",
				"조심하세요!"
		})
}, stats = @Stats(offense = Level.ZERO, survival = Level.THREE, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.THREE), difficulty = Difficulty.EASY)
public class FireFightWithFire extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Flector.class, "cooldown", 20,
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

	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue(), CooldownDecrease._50);

	public FireFightWithFire(Participant participant) {
		super(participant);
	}

	@SubscribeEvent(onlyRelevant = true, priority = Priority.HIGHEST, ignoreCancelled = true)
	private void onEntityDamage(EntityDamageEvent e) {
		if (e.getCause().equals(DamageCause.FIRE) || e.getCause().equals(DamageCause.FIRE_TICK)) {
			if (!getPlayer().isDead()) {
				getPlayer().setHealth(Math.min(getPlayer().getHealth() + e.getFinalDamage(), getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			}
			e.setDamage(0);
		}
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldown.isCooldown()) {
			getPlayer().setFireTicks(160);
			cooldown.start();
			return true;
		}
		return false;
	}
}
