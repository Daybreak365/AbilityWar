package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerMoveEvent;

@AbilityManifest(name = "헤르밋", rank = Rank.C, species = Species.HUMAN, explain = {
		"자신의 주변 $[DistanceConfig]칸 내에 플레이어가 들어올 경우 알려주며,",
		"신속과 투명 버프가 부여됩니다."
})
public class Hermit extends AbilityBase {

	public static final SettingObject<Integer> DistanceConfig = new SettingObject<Integer>(Hermit.class, "Distance", 15,
			"# 몇칸 이내에 플레이어가 들어왔을 때 알림을 띄울지 설정합니다.") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public Hermit(Participant participant) {
		super(participant);
	}

	private final int distance = DistanceConfig.getValue();

	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		Participant participant = getGame().getParticipant(e.getPlayer());
		if (participant != null && !getParticipant().equals(participant) && getPlayer().getWorld().equals(participant.getPlayer().getWorld())) {
			if (!LocationUtil.isInCircle(getPlayer().getLocation(), e.getFrom(), distance) && LocationUtil.isInCircle(getPlayer().getLocation(), e.getTo(), distance)) {
				getPlayer().sendMessage(ChatColor.DARK_GRAY + "헤르밋 " + ChatColor.WHITE + "| " + ChatColor.YELLOW + participant.getPlayer().getName() + ChatColor.WHITE + " 접근중");
				PotionEffects.SPEED.addPotionEffect(getPlayer(), 100, 3, true);
				PotionEffects.INVISIBILITY.addPotionEffect(getPlayer(), 100, 0, true);
			}
		}
	}

}
