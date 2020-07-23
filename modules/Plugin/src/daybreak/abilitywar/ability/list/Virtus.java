package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

@AbilityManifest(name = "베르투스", rank = Rank.A, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 다음 $[DurationConfig]초간 받는 대미지가 75% 감소합니다. $[COOLDOWN_CONFIG]"
})
public class Virtus extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> DurationConfig = abilitySettings.new SettingObject<Integer>(Virtus.class, "Duration", 5,
			"# 능력 지속시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Virtus.class, "Cooldown", 70,
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

	public Virtus(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final AbilityTimer skill = new AbilityTimer(DurationConfig.getValue()) {
		@Override
		public void run(int count) {
			SoundLib.BLOCK_ANVIL_LAND.playSound(getPlayer());
			ParticleLib.LAVA.spawnParticle(getPlayer().getLocation(), 3, 3, 3, 10);
		}
	}.register();

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType.equals(ClickType.RIGHT_CLICK) && !cooldownTimer.isCooldown()) {
			skill.start();
			cooldownTimer.start();
			return true;
		}
		return false;
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onEntityDamage(EntityDamageEvent e) {
		if (skill.isRunning()) {
			e.setDamage(e.getDamage() / 4);
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if (skill.isRunning()) {
			e.setDamage(e.getDamage() / 4);
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onEntityDamage(EntityDamageByBlockEvent e) {
		if (skill.isRunning()) {
			e.setDamage(e.getDamage() / 4);
		}
	}

}
