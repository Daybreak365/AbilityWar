package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.block.SnowTerrain;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

@AbilityManifest(name = "설인", rank = Rank.S, species = Species.HUMAN, explain = {
		"눈과 얼음 위에서 §6힘§f, §b신속§f, §3저항 §f버프를 받습니다. 철괴를 우클릭하면 주변 지역을",
		"눈 지형으로 바꿉니다. $[COOLDOWN_CONFIG]"
}, summarize = {
		"눈과 얼음 위에서 §6힘§f, §b신속§f, §3저항 §f버프를 받습니다. 철괴를 우클릭하면 주변 지역을",
		"눈 지형으로 바꿉니다. $[COOLDOWN_CONFIG]"
})
public class Yeti extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Yeti.class, "cooldown", 80, "# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> RANGE_CONFIG = abilitySettings.new SettingObject<Integer>(Yeti.class, "range", 15,
			"# 스킬 사용 시 눈 지형으로 바꿀 범위") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1 && value <= 50;
		}

	};

	public Yeti(Participant participant) {
		super(participant);
	}

	private final AbilityTimer buff = new AbilityTimer() {

		@Override
		public void run(int count) {
			Material m = getPlayer().getLocation().getBlock().getType();
			Material bm = getPlayer().getLocation().subtract(0, 1, 0).getBlock().getType();
			if (m.equals(Material.SNOW) || bm.equals(Material.SNOW) || bm.equals(Material.SNOW_BLOCK) || bm.equals(Material.ICE) || bm.equals(Material.PACKED_ICE)) {
				PotionEffects.SPEED.addPotionEffect(getPlayer(), 5, 1, true);
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 5, 0, true);
				PotionEffects.DAMAGE_RESISTANCE.addPotionEffect(getPlayer(), 5, 0, true);
			}
		}

	}.setPeriod(TimeUnit.TICKS, 1).register();

	private final SnowTerrain terrain = new SnowTerrain();

	private final AbilityTimer iceMaker = new AbilityTimer(RANGE_CONFIG.getValue()) {

		private int count;
		private Location center;

		@Override
		public void onStart() {
			count = 1;
			center = getPlayer().getLocation();
		}

		@Override
		public void run(int sec) {
			for (Block block : LocationUtil.getBlocks2D(center, count, true, true, true)) {
				Block below = block.getRelative(BlockFace.DOWN);
				terrain.cover(below.getType() == Material.SNOW ? below : block);
			}
			count++;
		}

	}.setPeriod(TimeUnit.TICKS, 1).register();

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			buff.start();
		} else if (update == Update.ABILITY_DESTROY) {
			terrain.restore();
		}
	}

	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown()) {
			iceMaker.start();
			cooldownTimer.start();
			return true;
		}

		return false;
	}

}
