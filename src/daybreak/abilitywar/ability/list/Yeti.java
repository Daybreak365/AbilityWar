package daybreak.abilitywar.ability.list;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.timer.CooldownTimer;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.EffectLib;
import daybreak.abilitywar.utils.library.item.MaterialLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.thread.TimerBase;

@AbilityManifest(Name = "설인", Rank = Rank.S, Species = Species.HUMAN)
public class Yeti extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Yeti.class, "Cooldown", 80, "# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static SettingObject<Integer> RangeConfig = new SettingObject<Integer>(Yeti.class, "Range", 15,
			"# 스킬 사용 시 눈 지형으로 바꿀 범위") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 50;
		}

	};

	public Yeti(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f눈과 얼음 위에 서있으면 다양한 버프를 받습니다."),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 주변을 눈 지형으로 바꿉니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	private TimerBase Buff = new TimerBase() {

		@Override
		public void onStart() {
		}

		@Override
		public void TimerProcess(Integer Seconds) {
			Material m = getPlayer().getLocation().getBlock().getType();
			Material bm = getPlayer().getLocation().subtract(0, 1, 0).getBlock().getType();
			if (m.equals(Material.SNOW) || bm.equals(Material.SNOW) || bm.equals(Material.SNOW_BLOCK) || bm.equals(Material.ICE) || bm.equals(Material.PACKED_ICE)) {
				EffectLib.SPEED.addPotionEffect(getPlayer(), 5, 2, true);
				EffectLib.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 5, 1, true);
				EffectLib.DAMAGE_RESISTANCE.addPotionEffect(getPlayer(), 5, 0, true);
			}
		}

		@Override
		public void onEnd() {
		}

	}.setPeriod(1);

	private TimerBase Ice = new TimerBase(RangeConfig.getValue()) {

		private int Count;
		private Location center;

		@Override
		public void onStart() {
			Count = 1;
			center = getPlayer().getLocation();

			center.getWorld().getHighestBlockAt(center).setType(Material.SNOW);
		}

		@Override
		public void TimerProcess(Integer Seconds) {
			for(Block b : LocationUtil.getBlocksAtSameY(center, Count, true, true)) {
				Block db = b.getLocation().subtract(0, 1, 0).getBlock();
				Material type = db.getType();
				if (type.equals(Material.WATER)) {
					db.setType(Material.PACKED_ICE);
				} else if(type.equals(Material.LAVA)) {
					db.setType(Material.OBSIDIAN);
				} else if(type.equals(MaterialLib.ACACIA_LEAVES.getMaterial()) || type.equals(MaterialLib.BIRCH_LEAVES.getMaterial()) || type.equals(MaterialLib.DARK_OAK_LEAVES.getMaterial())
						||type.equals(MaterialLib.JUNGLE_LEAVES.getMaterial()) || type.equals(MaterialLib.OAK_LEAVES.getMaterial()) || type.equals(MaterialLib.SPRUCE_LEAVES.getMaterial())) {
					MaterialLib.GREEN_WOOL.setType(db);
				} else {
					db.setType(Material.SNOW_BLOCK);
				}

				b.setType(Material.SNOW);
			}/*
			for (Location l : LocationUtil.getCircle(center, Count, Count * 20, true)) {
				ParticleLib.SNOWBALL.spawnParticle(l, 0, 0, 0, 1);

				Block db = l.subtract(0, 2, 0).getBlock();

				if (type.equals(Material.WATER)) {
					db.setType(Material.PACKED_ICE);
				}

				l.add(0, 1, 0).getBlock().setType(Material.SNOW);
			}*/
			Count++;
		}

		@Override
		public void onEnd() {}

	}.setPeriod(3);

	private CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if (mt.equals(MaterialType.Iron_Ingot)) {
			if (ct.equals(ClickType.RightClick)) {
				if (!Cool.isCooldown()) {
					Ice.StartTimer();

					Cool.StartTimer();

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void onRestrictClear() {
		Buff.StartTimer();
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}