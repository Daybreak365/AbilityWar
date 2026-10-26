package daybreak.abilitywar.ability.list.hermit.v1_19_R3;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.list.hermit.AbstractHermit;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.minecraft.nms.v1_19_R3.InvisiblePlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Hermit extends AbstractHermit {
	private final InvisiblePlayer invisible;
	public Hermit(Participant participant) {
		super(participant);
		invisible = new InvisiblePlayer(this::getPlayer);
	}
	@Override protected void hide0() { invisible.hide(); }
	@Override protected void show0() { invisible.show(); }
	@SubscribeEvent private void onJoin(PlayerJoinEvent event) {
		Bukkit.getScheduler().runTaskLater(AbilityWar.getPlugin(), () -> {
			if (isHiding()) invisible.add(event.getPlayer());
		}, 2L);
	}
	@SubscribeEvent private void onQuit(PlayerQuitEvent event) { invisible.remove(event.getPlayer()); }
}
