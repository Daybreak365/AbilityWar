package DayBreak.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.AbilityManifest.Species;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.EffectLib;
import DayBreak.AbilityWar.Utils.Library.ParticleLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;

@AbilityManifest(Name = "설인", Rank = Rank.S, Species = Species.HUMAN)
public class Yeti extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Yeti.class, "Cooldown", 80, "# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static SettingObject<Integer> RangeConfig = new SettingObject<Integer>(Yeti.class, "Range", 10,
			"# 스킬 사용 시 눈 지형으로 바꿀 범위") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1 && value <= 50;
		}

	};

	public Yeti(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f눈 위에 서 있으면 다양한 버프를 받습니다."),
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

		Integer Count;
		Location center;

		@Override
		public void onStart() {
			Count = 1;
			center = getPlayer().getLocation();

			center.getWorld().getHighestBlockAt(center).setType(Material.SNOW);
		}

		@Override
		public void TimerProcess(Integer Seconds) {
			for (Location l : LocationUtil.getCircle(center, Count, Count * 20, true)) {
				ParticleLib.SNOWBALL.spawnParticle(l, 1, 0, 0, 0);

				Block db = l.subtract(0, 2, 0).getBlock();

				if (db.getType().equals(Material.WATER)) {
					db.setType(Material.PACKED_ICE);
				}

				l.add(0, 1, 0).getBlock().setType(Material.SNOW);
			}

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
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
