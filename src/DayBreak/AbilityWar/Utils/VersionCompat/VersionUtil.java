package DayBreak.AbilityWar.Utils.VersionCompat;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VersionUtil {
	
	private VersionUtil() {}
	
	/**
	 * 플레이어의 손에 있는 아이템을 반환합니다.
	 * @param p	손에 있는 아이템을 확인할 플레이어
	 * @return	손에 있는 아이템
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack getItemInHand(Player p) {
		if(ServerVersion.getVersion() >= 9) {
			return p.getInventory().getItemInMainHand();
		} else {
			return p.getInventory().getItemInHand();
		}
	}
	
	/**
	 * 플레이어의 최대 체력을 반환합니다.
	 * @param p	최대 체력을 확인할 플레이어
	 * @return	최대 체력
	 */
	@SuppressWarnings("deprecation")
	public static double getMaxHealth(Player p) {
		if(ServerVersion.getVersion() >= 9) {
			return p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		} else {
			return p.getMaxHealth();
		}
	}
	
	@SuppressWarnings("deprecation")
	public static double getMaxHealth(Damageable d) {
		if(ServerVersion.getVersion() >= 11) {
			if(d instanceof Attributable) {
				return ((Attributable) d).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			} else {
				return 1;
			}
		} else {
			return d.getMaxHealth();
		}
	}
	
}
