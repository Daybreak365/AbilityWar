package daybreak.abilitywar.ability.list.scarecrow;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface FakeScareCrowEntity extends ScareCrowEntity {

	void hide(Player player);
	void lookAt(Player receiver, Location location);

}
