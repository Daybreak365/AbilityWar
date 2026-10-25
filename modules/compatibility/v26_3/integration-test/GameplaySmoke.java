import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.list.Canis;
import daybreak.abilitywar.ability.list.magnet.Magnet;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.manager.GameFactory;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.effect.Fear;
import daybreak.abilitywar.game.manager.gui.AbilityGUI;
import daybreak.abilitywar.game.manager.gui.AbilityListGUI;
import daybreak.abilitywar.game.manager.object.AbilitySelect;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.nms.IDummy;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;

/** Real server entities and scheduled ticks; no client rendering assertions. Disposable world only. */
public final class GameplaySmoke {
    private final JavaPlugin plugin;
    private final BiConsumer<String, Runnable> check;
    private final Runnable done;
    private IDummy dummy, other;
    private Player player, source;
    private TestGame game;
    private Location origin;
    private ArmorStand gadget;
    private Location gadgetStart;
    private Arrow arrow;
    private Canis canis;
    private Horse mount;
    private float absorptionAtDismount;
    private int monitoredFearTicks;
    private boolean continuousFear = true;
    private Fear fear;
    private static List<Player> players;
    private org.bukkit.scheduler.BukkitTask playerTicks;
    private Inventory obsoletePage, currentPage, closedPage;
    private String expectedPageName;
    private LifecycleAbility lifecycleAbility;
    private ActionbarChannel permanentChannel, temporaryChannel;

    public static class LifecycleEvent extends Event {
        private static final HandlerList HANDLERS = new HandlerList();
        @Override public HandlerList getHandlers() { return HANDLERS; }
        public static HandlerList getHandlerList() { return HANDLERS; }
    }
    public static final class LifecycleChildEvent extends LifecycleEvent {}

    @AbilityManifest(name = "Internal lifecycle regression", rank = AbilityManifest.Rank.C, species = AbilityManifest.Species.HUMAN)
    public static class LifecycleAbility extends AbilityBase {
        private int ticks, stopped, events;
        public LifecycleAbility(Participant participant) { super(participant); }
        private void startTimer() {
            new AbilityTimer() {
                @Override protected void run(int count) { ticks++; }
                @Override protected void onSilentEnd() { stopped++; }
            }.setPeriod(TimeUnit.TICKS, 1).register().start();
        }
        @SubscribeEvent(childs = {LifecycleChildEvent.class})
        public void onLifecycle(LifecycleEvent event) { events++; }
    }

    @GameManifest(name = "26.3 gameplay regression", description = {"Disposable test game"})
    public static class TestGame extends Game {
        public TestGame() { super(players); }
        @Override protected void progressGame(int seconds) {}
        @Override public AbilitySelect newAbilitySelect() { return null; }
        public void begin() { startGame(); }
    }
    public GameplaySmoke(JavaPlugin plugin, BiConsumer<String, Runnable> check, Runnable done) {
        this.plugin = plugin; this.check = check; this.done = done;
    }
    private void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private void at(long tick, String name, Runnable action) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> check.accept(name, action), tick);
    }
    private Object get(Object instance, String name) {
        try { Field f=instance.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(instance); }
        catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }
    private void ability(Class<? extends AbilityBase> type) {
        try { game.getParticipant(player).setAbility(type); }
        catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }
    private int observerCount(Class<? extends Event> eventType) {
        final Map<?, ?> priorities = (Map<?, ?>) get(game.getEventManager(), "observers");
        int count = 0;
        for (Object priority : priorities.values()) {
            Object registrations = ((Map<?, ?>) priority).get(eventType);
            if (registrations != null) count += ((daybreak.google.common.collect.Multimap<?, ?>) registrations).size();
        }
        return count;
    }
    public void start() {
        final World world=Bukkit.getWorlds().get(0);
        origin=world.getSpawnLocation().add(40, 0, 0);
        origin.setY(Math.max(90, world.getHighestBlockYAt(origin)+5));
        origin.setX(origin.getBlockX()+.5); origin.setZ(origin.getBlockZ()+.5);origin.setYaw(0);origin.setPitch(0);
        for (int x=-1;x<=1;x++) for (int z=-1;z<=2;z++) world.addPluginChunkTicket((origin.getBlockX() >> 4)+x,(origin.getBlockZ() >> 4)+z,plugin);
        for (int x=-8;x<=8;x++) for (int z=-8;z<=18;z++) {
            origin.clone().add(x,-1,z).getBlock().setType(Material.STONE);
            for (int y=0;y<=5;y++) origin.clone().add(x,y,z).getBlock().setType(Material.AIR);
        }
        dummy=NMS.createDummy(origin); other=NMS.createDummy(origin.clone().add(-4,0,0));
        player=dummy.getBukkitEntity();source=other.getBukkitEntity();
        // Dummy players have no incoming network tick. Advance real ServerPlayer logic
        // explicitly so potion durations, inventory attributes and riding are exercised.
        playerTicks=Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            ((org.bukkit.craftbukkit.entity.CraftPlayer)player).getHandle().doTick();
            ((org.bukkit.craftbukkit.entity.CraftPlayer)source).getHandle().doTick();
        },3,1);
        at(5,"gameplay harness", () -> {
            players=Arrays.asList(player,source);GameFactory.registerMode(TestGame.class);game=new TestGame();game.setRestricted(false);game.start();game.begin();
            Fear.apply(game.getParticipant(player),TimeUnit.TICKS,60,source);
            fear=game.getParticipant(player).getPrimaryEffect(Fear.registration);
            require(fear!=null,"Fear not applied");
        });
        final org.bukkit.scheduler.BukkitTask monitor = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (fear!=null && fear.isRunning()) {
                monitoredFearTicks++;
                PotionEffect effect=player.getPotionEffect(PotionEffectType.BLINDNESS);
                continuousFear &= effect!=null && effect.getDuration()>20;
            }
        },6,1);
        at(10,"magnet launch", () -> {
            player.teleport(origin);ability(Magnet.class);
            ((Magnet)game.getParticipant(player).getAbility()).ActiveSkill(Material.IRON_INGOT,AbilityBase.ClickType.RIGHT_CLICK);
            gadget=null;
            for (var timer : game.getParticipant(player).getAbility().getRunningTimers()) {
                if (timer instanceof Magnet.Gadget) gadget=(ArmorStand)get(timer,"armorStand");
            }
            require(gadget!=null,"Gadget timer not started");
            gadgetStart=gadget.getLocation();
        });
        at(19,"magnet moves with marker hitbox removed", () -> require(gadgetStart.distance(gadget.getLocation())>3,"Gadget did not fly"));
        at(21,"magnet captures projectile", () -> {
            arrow=world.spawn(gadget.getLocation().add(1,1.75,0),Arrow.class);arrow.setGravity(true);
        });
        at(25,"fear extension keeps continuous blindness", () -> {
            Fear.apply(game.getParticipant(player),TimeUnit.TICKS,80,source);
            require(player.getPotionEffect(PotionEffectType.BLINDNESS).getDuration()>=80,"Blindness was not extended");
        });
        at(30,"magnet cleanup releases projectiles", () -> {
            require(!arrow.hasGravity(),"Projectile was not attracted");
            game.getParticipant(player).removeAbility();
            require(!gadget.isValid(),"Gadget remains after ability removal");require(arrow.hasGravity(),"Projectile gravity not restored");arrow.remove();
            player.teleport(origin);ability(Magnet.class);
            for(int x=-1;x<=1;x++) for(int y=0;y<4;y++) origin.clone().add(x,y,3).getBlock().setType(Material.STONE);
            ((Magnet)game.getParticipant(player).getAbility()).ActiveSkill(Material.IRON_INGOT,AbilityBase.ClickType.RIGHT_CLICK);
            gadget=null;
            for (var timer : game.getParticipant(player).getAbility().getRunningTimers()) {
                if (timer instanceof Magnet.Gadget) gadget=(ArmorStand)get(timer,"armorStand");
            }
            require(gadget!=null,"Gadget timer not started");
        });
        at(42,"magnet stops before wall", () -> {
            require(gadget.getLocation().getZ()<origin.getBlockZ()+3,"Gadget crossed wall");
            game.getParticipant(player).removeAbility();
            for(int x=-1;x<=1;x++) for(int y=0;y<4;y++) origin.clone().add(x,y,3).getBlock().setType(Material.AIR);
        });
        at(45,"fear stays dark and cleans up", () -> {
            monitor.cancel();require(monitoredFearTicks>=35 && continuousFear,"Blindness expired or remained too short");
            fear.stop(true);require(!player.hasPotionEffect(PotionEffectType.BLINDNESS),"Fear blindness remains: "+player.getPotionEffect(PotionEffectType.BLINDNESS)+" expected "+get(fear,"blindnessTicks"));
            Fear.apply(game.getParticipant(player),TimeUnit.TICKS,60,source);
            fear=game.getParticipant(player).getPrimaryEffect(Fear.registration);
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,200,2));fear.stop(true);
            require(player.getPotionEffect(PotionEffectType.BLINDNESS).getAmplifier()==2,"Removed independent blindness");
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        });
        at(50,"original canis mounts without ground charge", () -> {
            player.teleport(origin);ability(Canis.class);canis=(Canis)game.getParticipant(player).getAbility();
            require(canis.ActiveSkill(Material.IRON_INGOT,AbilityBase.ClickType.RIGHT_CLICK),"Original mount failed");
            require(canis.isMounted() && !canis.isLanceReady(),"Original mounted lance charge not restored");
            mount=(Horse)player.getVehicle();
            require(!canis.ActiveSkill(Material.IRON_INGOT,AbilityBase.ClickType.LEFT_CLICK),"Uncharged dash started");
        });
        at(55,"hologram updates with two viewers", () -> {
            IHologram hologram=NMS.newHologram(world,origin.getX(),origin.getY()+3,origin.getZ(),"initial");
            try {
                hologram.display(player);hologram.display(source);
                hologram.setText("updated 한글");hologram.setText("updated 한글");
                hologram.teleport(world,origin.getX()+1,origin.getY()+3,origin.getZ(),40,0);
                require(hologram.getText().equals("updated 한글"),"Hologram text changed");
                require(hologram.getLocation().getX()==origin.getX()+1,"Hologram teleport changed");
                hologram.hide(player);hologram.setText("remaining viewer");
                require(hologram.getText().equals("remaining viewer"),"Remaining viewer update failed");
            } finally {hologram.unregister();}
            require(hologram.isUnregistered(),"Hologram cleanup failed");
        });
        at(60,"GUI page switch prepares only current page", () -> {
            AbilityGUI gui=new AbilityGUI(source,game,plugin);
            gui.openGUI(1);obsoletePage=source.getOpenInventory().getTopInventory();
            gui.openGUI(2);currentPage=source.getOpenInventory().getTopInventory();
            TreeMap<String, AbilityFactory.AbilityRegistration> ordered=new TreeMap<>();
            for (var registration : AbilityList.values()) ordered.put(registration.getManifest().name(),registration);
            require(ordered.size()>36,"Second page missing");
            expectedPageName=new ArrayList<>(ordered.keySet()).get(36);
            require(obsoletePage!=currentPage,"Page inventory was reused");
            require(currentPage.getItem(0).getType()==Material.WHITE_STAINED_GLASS,"Loading placeholder changed");
        });
        at(62,"GUI page switch discards obsolete render", () -> {
            require(obsoletePage.getItem(0).getType()==Material.WHITE_STAINED_GLASS,"Obsolete page was populated");
            require(currentPage.getItem(0).getType()==Material.IRON_BLOCK,"Current page was not populated");
            require(ChatColor.stripColor(currentPage.getItem(0).getItemMeta().getDisplayName()).equals(expectedPageName),"Current page contains another page's items");
            source.closeInventory();
        });
        at(65,"GUI same-page sort replaces pending render", () -> {
            AbilityListGUI gui=new AbilityListGUI(source,plugin);
            gui.openGUI(1);obsoletePage=source.getOpenInventory().getTopInventory();
            InventoryClickEvent click=new InventoryClickEvent(source.getOpenInventory(),InventoryType.SlotType.CONTAINER,45,ClickType.LEFT,InventoryAction.PICKUP_ALL);
            Bukkit.getPluginManager().callEvent(click);
            require(click.isCancelled(),"Sort click was not handled");
            currentPage=source.getOpenInventory().getTopInventory();
            require(currentPage!=obsoletePage,"Sort did not open a new inventory");
        });
        at(67,"GUI sorted page retains selected ordering", () -> {
            var expected=AbilityListGUI.SortType.RANK.getItem(AbilityListGUI.SortType.RANK.getIterable().iterator().next(),true);
            require(obsoletePage.getItem(0).getType()==Material.WHITE_STAINED_GLASS,"Old sort render was applied");
            require(currentPage.getItem(0).equals(expected),"Rank-sorted first item changed");
            source.closeInventory();
        });
        at(70,"GUI close cancels pending render", () -> {
            new AbilityListGUI(source,plugin).openGUI(1);
            closedPage=source.getOpenInventory().getTopInventory();source.closeInventory();
        });
        at(72,"closed GUI remains untouched", () -> require(closedPage.getItem(0).getType()==Material.WHITE_STAINED_GLASS,"Closed inventory was populated"));
        at(75,"timer traversal and child event registration", () -> {
            AbilityFactory.registerAbility(LifecycleAbility.class);
            try {game.getParticipant(source).setAbility(LifecycleAbility.class);}
            catch (ReflectiveOperationException e) {throw new RuntimeException(e);}
            lifecycleAbility=(LifecycleAbility)game.getParticipant(source).getAbility();
            lifecycleAbility.startTimer();lifecycleAbility.startTimer();
            for (var ignored : lifecycleAbility.getRunningTimers()) break;
            lifecycleAbility.startTimer();
            require(lifecycleAbility.getRunningTimers().size()==3,"Early traversal prevented later timer registration");
            Bukkit.getPluginManager().callEvent(new LifecycleChildEvent());
            require(lifecycleAbility.events==1,"Child event dispatch changed");
            require(observerCount(LifecycleEvent.class)==1 && observerCount(LifecycleChildEvent.class)==1,"Lifecycle observers missing");
        });
        at(78,"ability removal clears timers and child event observers", () -> {
            require(lifecycleAbility.ticks>0,"Fixture timers did not run");
            game.getParticipant(source).removeAbility();
            require(lifecycleAbility.stopped==3 && lifecycleAbility.getRunningTimers().isEmpty(),"Ability timers were not stopped and removed");
            require(observerCount(LifecycleEvent.class)==0 && observerCount(LifecycleChildEvent.class)==0,"Child event observer remains after removal");
            Bukkit.getPluginManager().callEvent(new LifecycleChildEvent());
            require(lifecycleAbility.events==1,"Removed ability received an event");
        });
        at(80,"actionbar composition contains both channels", () -> {
            var actionbar=game.getParticipant(source).actionbar();
            permanentChannel=actionbar.newChannel().update("persistent");
            temporaryChannel=actionbar.newChannel().update("temporary",1);
            String text=(String)get(actionbar,"lastString");
            require(text.contains("persistent") && text.contains("temporary"),"Channel composition lost a value");
        });
        at(85,"identical actionbar text refreshes duration", () -> {
            String before=(String)get(game.getParticipant(source).actionbar(),"lastString");
            temporaryChannel.update("temporary",3);
            require(get(temporaryChannel,"seconds").equals(3),"Repeated text did not reset duration");
            require(before.equals(get(game.getParticipant(source).actionbar(),"lastString")),"Repeated text changed composition");
        });
        at(110,"refreshed actionbar remains visible", () -> require(((String)get(game.getParticipant(source).actionbar(),"lastString")).contains("temporary"),"Refreshed channel expired too soon"));
        at(150,"actionbar expiry and unregister rebuild composition", () -> {
            var actionbar=game.getParticipant(source).actionbar();
            require(get(actionbar,"lastString").equals("persistent"),"Expired channel remains in composition");
            permanentChannel.unregister();temporaryChannel.unregister();
            require(get(actionbar,"lastString").equals(""),"Removed channels remain in composition");
        });
        at(215,"original canis charges while mounted and dashes on iron left click", () -> {
            require(canis.isMounted(),"Original horse auto-dismounted");
            require(canis.ActiveSkill(Material.IRON_INGOT,AbilityBase.ClickType.LEFT_CLICK),"Mounted charge/dash failed");
            require((Boolean)get(get(canis,"highSpeedDash"),"dashing"),"Dash did not start");
        });
        at(385,"original canis stays mounted after dash", () -> {
            require(canis.isMounted() && mount.isValid(),"Horse auto-dismounted after dash");
            require(!(Boolean)get(get(canis,"highSpeedDash"),"dashing"),"Dash did not end");
        });
        at(390,"original canis dismount absorption and remount cooldown", () -> {
            player.leaveVehicle();
            require(!canis.isMounted() && !mount.isValid(),"Manual dismount failed");
            absorptionAtDismount=NMS.getAbsorptionHearts(player);
            require(absorptionAtDismount>0,"Original absorption missing");
            require(!canis.mount(),"Original remount cooldown missing");
        });
        at(460,"original canis absorption decreases", () -> {
            final float remaining=NMS.getAbsorptionHearts(player);
            // The original reverse timer starts at ceil(amount / 2), then decrements
            // before running. Preserve that behavior when reverting the ability.
            require(remaining<absorptionAtDismount,"Original absorption did not decrease");
            plugin.getLogger().info("Original Canis absorption: "+absorptionAtDismount+" -> "+remaining);
        });
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            check.accept("gameplay cleanup", () -> {game.stop();playerTicks.cancel();dummy.remove();other.remove();world.removePluginChunkTickets(plugin);});done.run();
        },480);
    }
}
