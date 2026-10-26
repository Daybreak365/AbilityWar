import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.list.grapplinghook.Hooks;
import daybreak.abilitywar.ability.list.magnet.Magnet;
import daybreak.abilitywar.utils.base.minecraft.ability.list.grapplinghook.HookEntity;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.nms.*;
import daybreak.abilitywar.utils.base.minecraft.raytrace.RayTrace;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;

/** Runs only in a disposable world: creates entities and shuts down the server. */
public final class MatrixSmoke extends JavaPlugin implements Listener {
    private int passed, failed;
    private int robustnessPassed, robustnessFailed;
    private IDummy dummy, observer;
    private Player player;

    private void check(String name, Runnable action) {
        try { action.run(); passed++; getLogger().info("PASS " + name); }
        catch (Throwable error) { failed++; getLogger().log(Level.SEVERE, "FAIL " + name, error); }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    @Override public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskLater(this, this::runChecks, 40);
    }

    @EventHandler public void onWorldInit(WorldInitEvent event) {
        // The fixture does not need hundreds of permanently loaded spawn chunks.
        event.getWorld().setKeepSpawnInMemory(false);
    }

    @EventHandler public void onWorldLoad(WorldLoadEvent event) {
        final World world = event.getWorld();
        final Location spawn = world.getSpawnLocation();
        // Load the two columns used by the fixture before plugins can start skin lookups
        // on the same worker pool that old CraftBukkit uses for chunk generation.
        for (int x = spawn.getBlockX() >> 4; x <= (spawn.getBlockX() + 10) >> 4; x++) {
            final int z = spawn.getBlockZ() >> 4;
            world.loadChunk(x, z);
            try {
                world.getClass().getMethod("addPluginChunkTicket", int.class, int.class, Plugin.class)
                        .invoke(world, x, z, this);
            } catch (NoSuchMethodException olderApi) { /* Old worlds retain loaded chunks through this short test. */ }
            catch (ReflectiveOperationException error) { throw new IllegalStateException(error); }
        }
    }

    private void runChecks() {
        final Plugin abilityWar = Bukkit.getPluginManager().getPlugin("AbilityWar");
        check("plugin startup", () -> require(abilityWar != null && abilityWar.isEnabled(), "AbilityWar did not enable"));
        if (abilityWar == null || !abilityWar.isEnabled()) { finish(); return; }
        getLogger().info("MATRIX SERVER " + Bukkit.getVersion() + " Bukkit=" + Bukkit.getBukkitVersion()
                + " adapter=" + ServerVersion.getName() + " plugin=" + abilityWar.getDescription().getVersion());
        check("server version", () -> {
            require(!ServerVersion.getName().equals("UNSUPPORTED"), "Unrecognized server");
            String expected = System.getProperty("abilitywar.expectedAdapter");
            require(expected == null || expected.equals(ServerVersion.getName()), "Expected " + expected);
        });
        for (String ability : new String[] {"hermit.Hermit", "clown.Clown"}) {
            check("ability " + ability, () -> {
                String[] parts = ability.split("\\.");
                try {
                    Class<? extends AbilityBase> type = Class.forName("daybreak.abilitywar.ability.list."
                            + parts[0] + "." + ServerVersion.getName() + "." + parts[1]).asSubclass(AbilityBase.class);
                    require(AbilityFactory.isRegistered(type), "Ability was not registered");
                } catch (ClassNotFoundException error) { throw new RuntimeException(error); }
            });
        }
        check("magnet registration", () -> require(AbilityFactory.isRegistered(Magnet.class), "Magnet was not registered"));
        final World world = Bukkit.getWorlds().get(0);
        final Location location = world.getSpawnLocation().add(0, 5, 0);
        check("sound", () -> SoundLib.ENTITY_PLAYER_LEVELUP.playSound(location, 1, 1));
        check("particle aliases", () -> {
            try {
                Field particle = ParticleLib.SimpleParticle.class.getDeclaredField("particle");
                particle.setAccessible(true);
                for (Object value : new Object[] {ParticleLib.FLAME, ParticleLib.REDSTONE, ParticleLib.ITEM_CRACK})
                    require(particle.get(value) != null, "Missing particle mapping");
            } catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
        });
        check("custom skull", () -> require(NMS.createCustomSkull(
                "c3832ce7fbf00dd0eaa537918612cd2f2e9a2a2a8b6ec5821932d38768f955ce9") != null, "Missing skull"));
        check("hologram lifecycle", () -> {
            IHologram hologram = NMS.newHologram(world, location, "한글 hologram");
            try {
                require(hologram.getText().contains("한글"), "Text changed");
                hologram.setText("updated");
                hologram.teleport(location.clone().add(1, 0, 0));
                require(hologram.getText().equals("updated"), "Text did not update");
                require(hologram.getLocation().distance(location.clone().add(1, 0, 0)) < .01, "Hologram did not move");
            } finally { hologram.unregister(); }
            require(hologram.isUnregistered(), "Hologram leaked");
        });
        // Kept separate from ABI checks: official older adapters already concatenate raw JSON.
        try {
            IHologram hologram = NMS.newHologram(world, location, "quote \" newline\n 한글");
            try { require(hologram.getText().contains("한글"), "Text changed"); }
            finally { hologram.unregister(); }
            robustnessPassed++; getLogger().info("ROBUSTNESS PASS quoted hologram text");
        } catch (Throwable error) {
            robustnessFailed++; getLogger().log(Level.WARNING, "ROBUSTNESS FAIL quoted hologram text", error);
        }
        check("world border", () -> {
            IWorldBorder border = NMS.createWorldBorder(world);
            border.setWorld(world); border.setSize(50); border.setCenter(5, 5);
            border.setDamageAmount(.2); border.setDamageBuffer(3); border.setWarningDistance(5); border.setWarningTime(10);
        });
        check("block raytrace", () -> {
            Block block = location.clone().add(10, 0, 0).getBlock();
            Material old = block.getType();
            try {
                block.setType(Material.STONE);
                require(RayTrace.hitsBlock(world, block.getX() - 1, block.getY() + .5, block.getZ() + .5,
                        block.getX() + 2, block.getY() + .5, block.getZ() + .5), "Missed solid block");
                block.setType(Material.AIR);
                require(!RayTrace.hitsBlock(world, block.getX() + .1, block.getY() + .5, block.getZ() + .5,
                        block.getX() + .9, block.getY() + .5, block.getZ() + .5), "Hit empty space");
            } finally { block.setType(old); }
        });
        check("entity operations", () -> {
            ArmorStand stand = world.spawn(location, ArmorStand.class);
            try {
                NMS.setAbsorptionHearts(stand, 4); require(NMS.getAbsorptionHearts(stand) == 4, "Absorption mismatch");
                NMS.setInvisible(stand, true); require(NMS.isInvisible(stand), "Not invisible");
                NMS.setLocation(stand, location.getX() + 1, location.getY(), location.getZ(), 40, 0);
                require(Math.abs(stand.getLocation().getX() - location.getX() - 1) < .01, "Position mismatch");
                require(NMS.getBoundingBox(stand) != null, "Missing bounding box");
                NMS.clearActiveItem(stand); NMS.swingHand(stand, Hand.MAIN_HAND); NMS.removeBoundingBox(stand);
            } finally { stand.remove(); }
        });
        for (String kind : new String[] {"magic", "magic with armor", "arrow", "fixed", "thorn", "explosion"}) {
            check("damage " + kind, () -> {
                Cow cow = world.spawn(location, Cow.class), source = world.spawn(location, Cow.class);
                try {
                    double before = cow.getHealth();
                    boolean accepted;
                    switch (kind) {
                        case "magic": accepted = Damages.damageMagic(cow, null, true, 1); break;
                        case "magic with armor": accepted = Damages.damageMagic(cow, null, false, 1); break;
                        case "arrow": accepted = Damages.damageArrow(cow, source, 1); break;
                        case "fixed": accepted = Damages.damageFixed(cow, source, 1); break;
                        case "thorn": accepted = Damages.damageThorn(cow, source, 1); break;
                        default: accepted = Damages.damageExplosion(cow, source, 1); break;
                    }
                    require(accepted && cow.getHealth() < before, "Damage was not applied");
                } finally { cow.remove(); source.remove(); }
            });
        }
        check("arrow state", () -> {
            Arrow arrow = world.spawn(location, Arrow.class);
            try {
                require(NMS.isArrow(arrow), "Arrow not recognized"); NMS.setInGround(arrow, true);
                require(arrow.isInBlock(), "Arrow is not in ground");
                NMS.setPickupStatus(arrow, PickupStatus.DISALLOWED);
                require(NMS.getPickupStatus(arrow) == PickupStatus.DISALLOWED, "Pickup state mismatch");
            } finally { arrow.remove(); }
        });
        check("dummy creation", () -> {
            dummy = NMS.createDummy(location); observer = NMS.createDummy(location.clone().add(3, 0, 0));
            require(dummy.isAlive() && observer.isAlive(), "Dummy is not alive");
            player = dummy.getBukkitEntity(); require(player != null, "Dummy has no player wrapper");
        });
        if (player != null && observer != null) {
            checkAdapterHelpers();
            check("dummy display and damage counter", () -> { dummy.display(observer.getBukkitEntity()); dummy.addDamage(2); });
            check("player operations", () -> {
                NMS.sendTitle(player, "title", "subtitle", 0, 20, 0); NMS.clearTitle(player);
                NMS.sendActionbar(player, "test", 0, 20, 0); NMS.setArrowsInBody(player, 2);
                NMS.setCooldown(player, Material.STONE, 20);
                require(NMS.hasCooldown(player, Material.STONE), "Cooldown missing");
                require(NMS.getCooldown(player, Material.STONE) > 0, "Cooldown missing duration");
                NMS.getSteeringDirection(player); NMS.isJumpingInVehicle(player);
                require(NMS.getAttackCooldown(player) >= 0, "Invalid attack cooldown");
                NMS.rotateHead(player, observer.getBukkitEntity(), 20, 10);
                NMS.setCamera(player, observer.getBukkitEntity()); NMS.setCamera(player, player);
                NMS.setWorldBorder(player, NMS.createWorldBorder(world)); NMS.resetWorldBorder(player);
            });
            check("hologram observer packets", () -> {
                IHologram hologram = NMS.newHologram(world, location, "before");
                try {
                    hologram.display(player); hologram.display(observer.getBukkitEntity());
                    hologram.setText("after"); hologram.teleport(location.clone().add(2, 0, 0));
                    hologram.hide(player); hologram.hide(observer.getBukkitEntity());
                } finally { hologram.unregister(); }
            });
            check("grappling hook location cleanup", () -> {
                final Set<UUID> before = new HashSet<>();
                for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) before.add(stand.getUniqueId());
                HookEntity hook = Hooks.createHook(player, location.clone().add(4, 0, 0));
                final List<ArmorStand> anchors = new ArrayList<>();
                for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class))
                    if (!before.contains(stand.getUniqueId())) anchors.add(stand);
                try { require(hook != null, "No hook"); } finally { hook.die(); }
                // Older worlds remove dead entities from their entity list on the next tick.
                Bukkit.getScheduler().runTaskLater(this, () -> check("hook anchor removed after tick", () -> {
                    for (ArmorStand anchor : anchors) require(!anchor.isValid() || anchor.isDead(), "Hook anchor leaked");
                }), 3);
            });
            check("grappling hook target cleanup", () -> {
                HookEntity hook = Hooks.createHook(player, observer.getBukkitEntity());
                try { require(hook != null, "No hook"); } finally { hook.die(); }
            });
        }
        Bukkit.getScheduler().runTaskLater(this, () -> {
            check("dummy cleanup", () -> {
                if (dummy != null) { dummy.remove(); require(!dummy.isAlive(), "Dummy leaked"); }
                if (observer != null) { observer.remove(); require(!observer.isAlive(), "Observer leaked"); }
            });
            finish();
        }, 10);
    }

    private void checkAdapterHelpers() {
        final String prefix = "daybreak.abilitywar.utils.base.minecraft.nms." + ServerVersion.getName() + ".";
        try {
            final Class<?> channels = Class.forName(prefix + "ChannelUtil");
            check("player connection access", () -> {
                try {
                    Object handle = player.getClass().getMethod("getHandle").invoke(player);
                    Method accessor = null;
                    for (Method method : channels.getMethods()) if (method.getName().equals("getChannel")) accessor = method;
                    require(accessor != null && accessor.invoke(null, handle) != null, "Missing network channel");
                } catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
            });
        } catch (ClassNotFoundException unusedByAdapter) { /* Older adapters access their channels directly. */ }
        catch (Throwable error) {
            failed++; getLogger().log(Level.SEVERE, "FAIL player connection helper initialization", error);
        }
        try {
            final Class<?> invisibility = Class.forName(prefix + "InvisiblePlayer");
            check("invisibility helper state", () -> {
                try {
                    Object helper = invisibility.getConstructor(Player.class).newInstance(player);
                    NMS.setInvisible(player, false);
                    try {
                        invisibility.getMethod("hide").invoke(helper);
                        require(NMS.isInvisible(player), "Invisible state was not applied");
                    } finally { invisibility.getMethod("show").invoke(helper); }
                    require(!NMS.isInvisible(player), "Invisible state was not restored");
                } catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
            });
            check("invisibility owner rebind", () -> {
                final Player replacement = observer.getBukkitEntity();
                final AtomicReference<Player> owner = new AtomicReference<>(player);
                try {
                    Object helper = invisibility.getConstructor(Supplier.class)
                            .newInstance((Supplier<Player>) owner::get);
                    NMS.setInvisible(player, false);
                    NMS.setInvisible(replacement, false);
                    invisibility.getMethod("hide").invoke(helper);
                    require(NMS.isInvisible(player), "Original owner was not hidden");
                    // Reconnected players may inherit the flag saved while the ability hid them.
                    NMS.setInvisible(replacement, true);
                    owner.set(replacement);
                    invisibility.getMethod("add", Player.class).invoke(helper, replacement);
                    require(!NMS.isInvisible(player), "Old player wrapper retained the ability's flag");
                    require(NMS.isInvisible(replacement), "Reconnected player was not hidden");
                    invisibility.getMethod("show").invoke(helper);
                    require(!NMS.isInvisible(replacement), "Persisted ability flag was mistaken for original invisibility");

                    // show() must also refresh a changed owner without a preceding join/add call.
                    owner.set(player);
                    NMS.setInvisible(player, true);
                    helper = invisibility.getConstructor(Supplier.class)
                            .newInstance((Supplier<Player>) owner::get);
                    invisibility.getMethod("hide").invoke(helper);
                    NMS.setInvisible(replacement, true);
                    owner.set(replacement);
                    invisibility.getMethod("show").invoke(helper);
                    require(NMS.isInvisible(player) && NMS.isInvisible(replacement), "Original invisibility was lost on rebind");
                } catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
                finally { NMS.setInvisible(player, false); NMS.setInvisible(replacement, false); }
            });
            check("invisibility equipment and bundle filtering", () -> {
                try {
                    Class<?> equipment;
                    try { equipment = Class.forName("net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket"); }
                    catch (ClassNotFoundException spigotNames) {
                        equipment = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment");
                    }
                    Object packet = equipment.getConstructor(int.class, List.class).newInstance(42, Collections.emptyList());
                    Method filter = invisibility.getDeclaredMethod("hidePacket", Object.class, int.class, boolean.class);
                    filter.setAccessible(true);
                    require(filter.invoke(null, packet, 42, true) == packet, "Self equipment was hidden");
                    require(filter.invoke(null, packet, 43, false) == packet, "Unrelated equipment changed");
                    require(filter.invoke(null, packet, 42, false) != packet, "Viewer equipment was not filtered");
                    try {
                        Class<?> bundleClass = Class.forName("net.minecraft.network.protocol.game.ClientboundBundlePacket");
                        Object bundle = bundleClass.getConstructor(Iterable.class).newInstance(Collections.singletonList(packet));
                        require(filter.invoke(null, bundle, 42, false) != bundle, "Bundled equipment was not filtered");
                    } catch (ClassNotFoundException beforeBundles) { /* Bundles were introduced in 1.19.4. */ }
                } catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
            });
        } catch (ClassNotFoundException unusedByAdapter) { /* Older abilities implement hiding internally. */ }
        catch (Throwable error) {
            failed++; getLogger().log(Level.SEVERE, "FAIL invisibility helper initialization", error);
        }
    }

    private void finish() {
        getLogger().info("MATRIX SMOKE RESULT passed=" + passed + " failed=" + failed);
        getLogger().info("MATRIX ROBUSTNESS RESULT passed=" + robustnessPassed + " failed=" + robustnessFailed);
        Bukkit.shutdown();
    }
}
