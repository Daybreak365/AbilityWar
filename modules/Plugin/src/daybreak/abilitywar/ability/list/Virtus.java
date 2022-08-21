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
import org.bukkit.event.player.PlayerVelocityEvent;
import org.jetbrains.annotations.NotNull;

@AbilityManifest(name = "베르투스", rank = Rank.A, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 다음 $[DURATION_CONFIG]초간 받는 대미지가 75% 감소합니다. $[COOLDOWN_CONFIG]",
		"넉백 또는 끌어당겨지는 효과를 기본적으로 줄여 받으며, 능력 사용 중에는",
		"효과가 증폭됩니다."
}, summarize = {
		"§5벡터§8(§e그랩 §3/ §b넉백§8)§f 영향을 줄여 받습니다.",
		"§7철괴 우클릭§f 시 잠시간 대미지를 75% 감소시키고, §5벡터§f 영향을 더욱 줄입니다."
})
public class Virtus extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Virtus.class, "duration", 5,
			"# 능력 지속시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Virtus.class, "cooldown", 70,
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
	private final AbilityTimer skill = new AbilityTimer(DURATION_CONFIG.getValue()) {
		@Override
		public void run(int count) {
			SoundLib.BLOCK_ANVIL_LAND.playSound(getPlayer());
			ParticleLib.LAVA.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 10);
		}
	}.register();

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown()) {
			skill.start();
			cooldownTimer.start();
			return true;
		}
		return false;
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onEntityDamage(final EntityDamageEvent e) {
		if (skill.isRunning()) {
			e.setDamage(e.getDamage() / 4);
			SoundLib.ITEM_SHIELD_BLOCK.playSound(getPlayer());
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		onEntityDamage(e);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onEntityDamageByBlock(final EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerVelocity(final PlayerVelocityEvent e) {
		e.setVelocity(e.getPlayer().getVelocity().multiply(skill.isRunning() ? .25 : .7));
	}

}
