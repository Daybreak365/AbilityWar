package daybreak.abilitywar.utils.versioncompat;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("deprecation")
public class VersionUtil {

	private VersionUtil() {
	}

	private static final Logger logger = Logger.getLogger(VersionUtil.class.getName());

	/**
	 * 플레이어의 손에 있는 아이템을 반환합니다.
	 *
	 * @param p 손에 있는 아이템을 확인할 플레이어
	 * @return 손에 있는 아이템
	 */
	public static ItemStack getItemInHand(Player p) {
		if (ServerVersion.getVersion() >= 9) {
			return p.getInventory().getItemInMainHand();
		} else {
			return p.getInventory().getItemInHand();
		}
	}

	/**
	 * 플레이어의 최대 체력을 반환합니다.
	 *
	 * @param p 최대 체력을 확인할 플레이어
	 * @return 최대 체력
	 */
	public static double getMaxHealth(Player p) {
		if (ServerVersion.getVersion() >= 9) {
			return p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		} else {
			return p.getMaxHealth();
		}
	}

	public static double getMaxHealth(Damageable d) {
		if (ServerVersion.getVersion() >= 11) {
			if (d instanceof Attributable) {
				return ((Attributable) d).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			} else {
				return 1;
			}
		} else {
			return d.getMaxHealth();
		}
	}

	private static final Class<?> PacketPlayInClientCommand = NMSUtil.getNMSClass("PacketPlayInClientCommand");
	private static final Class<?> EnumClientCommand = PacketPlayInClientCommand.getDeclaredClasses()[0];
	private static Object PERFORM_RESPAWN;

	static {
		try {
			PERFORM_RESPAWN = EnumClientCommand.getField("PERFORM_RESPAWN").get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			logger.log(Level.SEVERE, "리스폰 기능을 초기화하는 도중 오류가 발생하였습니다.");
		}
	}

	public static void respawn(Player player) {
		try {
			Constructor<?> constructor = PacketPlayInClientCommand.getConstructor(EnumClientCommand);
			NMSUtil.sendPacket(player, constructor.newInstance(PERFORM_RESPAWN));
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException ex) {
			logger.log(Level.SEVERE, player.getName() + " 플레이어를 리스폰하는 도중 오류가 발생하였습니다.");
		}
	}

}
