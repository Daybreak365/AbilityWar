package daybreak.abilitywar.ability.list;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerMoveEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.tItle.Title;
import daybreak.abilitywar.utils.math.LocationUtil;

@AbilityManifest(Name = "헤르밋", Rank = Rank.C, Species = Species.HUMAN)
public class Hermit extends AbilityBase {

	public static final SettingObject<Integer> DistanceConfig = new SettingObject<Integer>(Hermit.class, "Distance", 15,
			"# 몇칸 이내에 플레이어가 들어왔을 때 알림을 띄울지 설정합니다.") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};

	public Hermit(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f자신의 주변 " + DistanceConfig.getValue() + "칸 내에 플레이어가 들어올 경우 알려줍니다."),
				ChatColor.translateAlternateColorCodes('&', "&f또한, 플레이어가 들어왔을 때 신속과 투명 버프가 부여됩니다."));
	}

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	private final int Distance = DistanceConfig.getValue();
	
	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		Participant p = getGame().getParticipant(e.getPlayer());
		if(p != null && !getParticipant().equals(p) && getPlayer().getWorld().equals(p.getPlayer().getWorld())) {
			if(!LocationUtil.isInCircle(getPlayer().getLocation(), e.getFrom(), Double.valueOf(Distance), true) && 
					LocationUtil.isInCircle(getPlayer().getLocation(), e.getTo(), Double.valueOf(Distance), true)) {
				Title title = new Title(ChatColor.translateAlternateColorCodes('&', "&8헤르밋"),
						ChatColor.translateAlternateColorCodes('&', "&e" + p.getPlayer().getName() + " &f접근중"), 5, 30, 5);
				title.sendTo(getPlayer());
				PotionEffects.SPEED.addPotionEffect(getPlayer(), 100, 3, true);
				PotionEffects.INVISIBILITY.addPotionEffect(getPlayer(), 100, 0, true);
			}
		}
	}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}
	
}
