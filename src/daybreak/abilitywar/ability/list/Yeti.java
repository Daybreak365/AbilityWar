package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.event.AbilityRestrictionClearEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.BlockX;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

@AbilityManifest(Name = "설인", Rank = Rank.S, Species = Species.HUMAN)
public class Yeti extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Yeti.class, "Cooldown", 80, "# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> RangeConfig = new SettingObject<Integer>(Yeti.class, "Range", 15,
			"# 스킬 사용 시 눈 지형으로 바꿀 범위") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 50;
		}

	};

	public Yeti(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f눈과 얼음 위에 서있으면 &6힘&f, &b신속 &f버프를 받습니다."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 주변을 눈 지형으로 바꿉니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	private final Timer Buff = new Timer() {

		@Override
		public void onStart() {
		}

		@Override
		public void onProcess(int count) {
			Material m = getPlayer().getLocation().getBlock().getType();
			Material bm = getPlayer().getLocation().subtract(0, 1, 0).getBlock().getType();
			if (m.equals(Material.SNOW) || bm.equals(Material.SNOW) || bm.equals(Material.SNOW_BLOCK) || bm.equals(Material.ICE) || bm.equals(Material.PACKED_ICE)) {
				PotionEffects.SPEED.addPotionEffect(getPlayer(), 5, 2, true);
				PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 5, 1, true);
			}
		}

		@Override
		public void onEnd() {
		}

	}.setPeriod(1);

	private final Timer iceMaker = new Timer(RangeConfig.getValue()) {

		private int count;
		private Location center;

		@Override
		public void onStart() {
			count = 1;
			center = getPlayer().getLocation();

			center.getWorld().getHighestBlockAt(center).setType(Material.SNOW);
		}

		@Override
		public void onProcess(int sec) {
			for (Block b : LocationUtil.getBlocks2D(center, count, true, true)) {
				Block db = b.getLocation().subtract(0, 1, 0).getBlock();
				Material type = db.getType();
				if (type.equals(Material.WATER)) {
					db.setType(Material.PACKED_ICE);
				} else if (type.equals(Material.LAVA)) {
					db.setType(Material.OBSIDIAN);
				} else if (MaterialX.ACACIA_LEAVES.compareType(db) || MaterialX.BIRCH_LEAVES.compareType(db) || MaterialX.DARK_OAK_LEAVES.compareType(db)
						|| MaterialX.JUNGLE_LEAVES.compareType(db) || MaterialX.OAK_LEAVES.compareType(db) || MaterialX.SPRUCE_LEAVES.compareType(db)) {
					BlockX.setType(db, MaterialX.GREEN_WOOL);
				} else {
					db.setType(Material.SNOW_BLOCK);
				}

				b.setType(Material.SNOW);
			}
			count++;
		}

		@Override
		public void onEnd() {
		}

	}.setPeriod(1);

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.RIGHT_CLICK) && !cooldownTimer.isCooldown()) {
			iceMaker.startTimer();
			cooldownTimer.startTimer();
			return true;
		}

		return false;
	}

	@SubscribeEvent(onlyRelevant = true)
	public void onRestrictionClear(AbilityRestrictionClearEvent e) {
		Buff.startTimer();
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
