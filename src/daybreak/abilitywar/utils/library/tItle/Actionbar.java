package daybreak.abilitywar.utils.library.tItle;

import daybreak.abilitywar.utils.versioncompat.ServerVersion;
import java.lang.reflect.Constructor;
import org.bukkit.entity.Player;

/**
 * 액션바 메시지
 *
 * @author Daybreak 새벽
 */
public class Actionbar extends AbstractTitle {

    private String message;
    private int fadeIn;
    private int stay;
    private int fadeOut;

    /**
     * 액션바 메시지
     *
     * @param message 메시지
     * @param fadeIn  FadeIn 시간 (틱 단위)
     * @param stay    Stay 시간 (틱 단위)
     * @param fadeOut FadeOut 시간 (틱 단위)
     */
    public Actionbar(String message, int fadeIn, int stay, int fadeOut) {
        this.message = message;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    private static final Class<?> PacketPlayOutTitle = getNMSClass("PacketPlayOutTitle");
    private static final Class<?> IChatBaseComponent = getNMSClass("IChatBaseComponent");
    private static final Class<?> Packet = getNMSClass("Packet");

    private static Class<?> getNMSClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + ServerVersion.getStringVersion() + "." + className);
        } catch (Exception ex) {
            return null;
        }
    }

    public void sendTo(Player p) {
        try {
            Object Actionbar = IChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class)
                    .invoke(null, "{\"text\": \"" + this.message + "\"}");

            Constructor<?> Constructor = PacketPlayOutTitle.getConstructor(
                    PacketPlayOutTitle.getDeclaredClasses()[0],
                    IChatBaseComponent, int.class, int.class, int.class);
            Object TimePacket = Constructor.newInstance(
                    PacketPlayOutTitle.getDeclaredClasses()[0].getField("TIMES").get(null),
                    Actionbar, this.fadeIn, this.stay, this.fadeOut);
            Object ActionbarPacket = Constructor.newInstance(
                    PacketPlayOutTitle.getDeclaredClasses()[0].getField("ACTIONBAR").get(null),
                    Actionbar, fadeIn, stay, fadeOut);

            sendPacket(p, TimePacket);
            sendPacket(p, ActionbarPacket);
        } catch (Exception ignored) {}
    }

    private static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", Packet).invoke(playerConnection, packet);
        } catch (Exception ignored) {
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
    }

    public void setStay(int stay) {
        this.stay = stay;
    }

    public void setFadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
    }

}
