import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.list.magnet.Magnet;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.manager.GameFactory;
import daybreak.abilitywar.game.manager.effect.Fear;
import daybreak.abilitywar.game.manager.gui.AbilityListGUI;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.item.Skulls;
import daybreak.abilitywar.utils.base.minecraft.nms.IDummy;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/** Optional Java 8/Paper 1.12.2 checks; creates entities and shuts down a disposable server. */
public final class LegacySmoke extends JavaPlugin {
    private int passed, failed;
    private IDummy first, second;
    private Player player, source;
    private TestGame game;
    private ArmorStand gadget;
    private Location start;
    private Inventory oldPage, newPage;
    private static List<Player> players;

    @GameManifest(name = "Legacy compatibility regression", description = {"Disposable test game"})
    public static class TestGame extends Game {
        public TestGame() { super(players); }
        @Override protected void progressGame(int seconds) {}
        @Override public AbilitySelect newAbilitySelect() { return null; }
        public void begin() { startGame(); }
    }

    private void check(String name, Runnable action) {
        try { action.run(); passed++; getLogger().info("PASS " + name); }
        catch (Throwable error) { failed++; getLogger().log(Level.SEVERE, "FAIL " + name, error); }
    }
    private void require(boolean condition) { if (!condition) throw new AssertionError(); }
    private void at(long tick, String name, Runnable action) {
        Bukkit.getScheduler().runTaskLater(this, () -> check(name, action), tick);
    }
    private Object field(Object instance, String name) {
        try { Field f = instance.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(instance); }
        catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
    }
    @Override public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, this::runChecks, 40);
    }
    private void runChecks() {
        check("plugin version and legacy server detection", () -> {
            require(Bukkit.getPluginManager().getPlugin("AbilityWar").isEnabled());
            require(Bukkit.getPluginManager().getPlugin("AbilityWar").getDescription().getVersion().equals("26.0.1"));
            require(ServerVersion.getName().equals("v1_12_R1"));
        });
        for (String ability : new String[] {"hermit.Hermit", "clown.Clown", "scarecrow.ScareCrow", "soul.Soul"}) {
            check("legacy ability " + ability, () -> {
                String[] parts = ability.split("\\.");
                try {
                    Class<? extends AbilityBase> type = Class.forName("daybreak.abilitywar.ability.list." + parts[0] + ".v1_12_R1." + parts[1]).asSubclass(AbilityBase.class);
                    require(AbilityFactory.isRegistered(type));
                } catch (ClassNotFoundException error) { throw new RuntimeException(error); }
            });
        }
        check("magnet registration", () -> require(AbilityFactory.isRegistered(Magnet.class)));
        check("legacy custom skull NMS", () -> require(Skulls.createCustomSkull("c3832ce7fbf00dd0eaa537918612cd2f2e9a2a2a8b6ec5821932d38768f955ce9") != null));
        final Location origin = Bukkit.getWorlds().get(0).getSpawnLocation().add(0, 10, 0);
        check("legacy hologram lifecycle", () -> {
            IHologram hologram = NMS.newHologram(origin.getWorld(), origin.getX(), origin.getY(), origin.getZ(), "legacy");
            try {
                hologram.setText("updated"); hologram.teleport(origin.clone().add(1, 0, 0));
                require(hologram.getText().equals("updated"));
            } finally { hologram.unregister(); }
            require(hologram.isUnregistered());
        });
        check("legacy absorption", () -> {
            ArmorStand stand = origin.getWorld().spawn(origin, ArmorStand.class);
            try { NMS.setAbsorptionHearts(stand, 4); require(NMS.getAbsorptionHearts(stand) == 4); }
            finally { stand.remove(); }
        });
        check("legacy dummy creation", () -> {
            first = NMS.createDummy(origin); second = NMS.createDummy(origin.clone().add(4, 0, 0));
            player = first.getBukkitEntity(); source = second.getBukkitEntity();
            require(first.isAlive() && second.isAlive());
        });
        at(5, "legacy game and magnet launch", () -> {
            players = Arrays.asList(player, source); GameFactory.registerMode(TestGame.class);
            game = new TestGame(); game.setRestricted(false); game.start(); game.begin();
            try { game.getParticipant(player).setAbility(Magnet.class); }
            catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
            ((Magnet) game.getParticipant(player).getAbility()).ActiveSkill(Material.IRON_INGOT, AbilityBase.ClickType.RIGHT_CLICK);
            for (Object timer : game.getParticipant(player).getAbility().getRunningTimers()) {
                if (timer instanceof Magnet.Gadget) gadget = (ArmorStand) field(timer, "armorStand");
            }
            require(gadget != null); start = gadget.getLocation();
        });
        at(15, "legacy magnet flight and cleanup", () -> {
            require(gadget.getLocation().distance(start) > 3);
            game.getParticipant(player).removeAbility(); require(!gadget.isValid());
        });
        at(18, "legacy fear application and cleanup", () -> {
            Fear.apply(game.getParticipant(player), TimeUnit.TICKS, 60, source);
            Fear fear = game.getParticipant(player).getPrimaryEffect(Fear.registration);
            require(fear != null && player.getPotionEffect(PotionEffectType.BLINDNESS).getDuration() >= 60);
            fear.stop(true); require(!player.hasPotionEffect(PotionEffectType.BLINDNESS));
        });
        at(20, "legacy GUI page switch", () -> {
            AbilityListGUI gui = new AbilityListGUI(player, this);
            gui.openGUI(1); oldPage = player.getOpenInventory().getTopInventory();
            gui.openGUI(2); newPage = player.getOpenInventory().getTopInventory();
            require(oldPage != newPage);
        });
        at(23, "legacy GUI population", () -> {
            require(oldPage.getItem(0).getType() == Material.STAINED_GLASS);
            require(newPage.getItem(0).getType() != Material.STAINED_GLASS);
            player.closeInventory();
        });
        Bukkit.getScheduler().runTaskLater(this, () -> {
            check("legacy cleanup", () -> {
                if (game != null) game.stop();
                if (first != null) first.remove();
                if (second != null) second.remove();
            });
            getLogger().info("LEGACY SMOKE RESULT passed=" + passed + " failed=" + failed);
            Bukkit.shutdown();
        }, 30);
    }
}
