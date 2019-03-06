package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityManifest;
import Marlang.AbilityWar.Ability.AbilityManifest.Rank;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.GameManager.Object.Participant;
import Marlang.AbilityWar.Utils.Library.Packet.TitlePacket;
import Marlang.AbilityWar.Utils.Math.LocationUtil;
import Marlang.AbilityWar.Utils.Thread.AbilityWarThread;

@AbilityManifest(Name = "헤르밋", Rank = Rank.C)
public class Hermit extends AbilityBase {

	public static SettingObject<Integer> DistanceConfig = new SettingObject<Integer>("헤르밋", "Distance", 15, 
			"# 몇칸 이내에 플레이어가 들어왔을 때 알림을 띄울지 설정합니다.") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};

	public Hermit(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f자신의 주변 " + DistanceConfig.getValue() + "칸 내에 플레이어가 들어올 경우 알려줍니다."));
	}

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		return false;
	}

	Integer Distance = DistanceConfig.getValue();
	
	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof PlayerMoveEvent) {
			PlayerMoveEvent e = (PlayerMoveEvent) event;
			Player p = e.getPlayer();
			if(p.getWorld().equals(getPlayer().getWorld())) {
				if(!LocationUtil.isInCircle(getPlayer().getLocation(), e.getFrom(), Double.valueOf(Distance)) && 
						LocationUtil.isInCircle(getPlayer().getLocation(), e.getTo(), Double.valueOf(Distance))) {
					if(AbilityWarThread.isGameTaskRunning() && AbilityWarThread.getGame().isParticipating(p)) {
						TitlePacket title = new TitlePacket(ChatColor.translateAlternateColorCodes('&', "&8헤르밋"),
								ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + " &f접근중"), 5, 30, 5);
						title.Send(getPlayer());
					}
				}
			}
		}
	}

	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}
