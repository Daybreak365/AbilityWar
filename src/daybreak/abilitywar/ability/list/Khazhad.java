package daybreak.abilitywar.ability.list;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.timer.CooldownTimer;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.FallBlock;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;

@AbilityManifest(Name = "카쟈드", Rank = Rank.A, Species = Species.GOD)
public class Khazhad extends AbilityBase {

	private static final SettingObject<Integer> LeftCooldownConfig = new SettingObject<Integer>(Khazhad.class, "LeftCooldown", 4, "# 좌클릭 쿨타임") {
		
		@Override
		public boolean Condition(Integer arg0) {
			return arg0 >= 0;
		}

	};

	private static final SettingObject<Integer> RightCooldownConfig = new SettingObject<Integer>(Khazhad.class, "RightCooldown", 10, "# 우클릭 쿨타임") {
		
		@Override
		public boolean Condition(Integer arg0) {
			return arg0 >= 0;
		}

	};
	
	public Khazhad(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 좌클릭하면 자신이 보고 있는 방향으로 얼음을 날립니다. " + Messager.formatCooldown(LeftCooldownConfig.getValue())),
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 자신이 보고 있는 블록 5칸 주변의 물을 모두 얼립니다. "),
				ChatColor.translateAlternateColorCodes('&', Messager.formatCooldown(RightCooldownConfig.getValue())));
	}

	private final CooldownTimer LeftCool = new CooldownTimer(this, LeftCooldownConfig.getValue(), "좌클릭");
	private final CooldownTimer RightCool = new CooldownTimer(this, RightCooldownConfig.getValue(), "우클릭").setActionbarNotice(false);
	
	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if(mt.equals(MaterialType.IRON_INGOT)) {
			if(ct.equals(ClickType.LEFT_CLICK)) {
				if(!LeftCool.isCooldown()) {
					FallBlock fall = new FallBlock(Material.PACKED_ICE, getPlayer().getEyeLocation(), getPlayer().getLocation().getDirection().multiply(1.7)) {
						
						@Override
						public void onChangeBlock(FallingBlock block) {
							for(int x = -2; x < 2; x++) {
								for(int y = -2; y < 2; y++) {
									for(int z = -2; z < 2; z++) {
										Location l = block.getLocation().clone().add(x, y, z);
										l.getBlock().setType(Material.PACKED_ICE);
									}
								}
							}
						}
						
					};
					
					fall.toggleGlowing(true).toggleSetBlock(true).Spawn();
					
					LeftCool.StartTimer();
					return true;
				}
			} else if(ct.equals(ClickType.RIGHT_CLICK)) {
				if(!RightCool.isCooldown()) {
					for(Block b : LocationUtil.getBlocks(getPlayer().getTargetBlock(null, 30).getLocation(), 5, false, false, false)) {
						if(b.getType().equals(Material.WATER) || (ServerVersion.getVersion() < 13 && b.getType().equals(Material.valueOf("STATIONARY_WATER")))) {
							b.setType(Material.PACKED_ICE);
						}
					}
					
					RightCool.StartTimer();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}

	@Override
	protected void onRestrictClear() {}

}
