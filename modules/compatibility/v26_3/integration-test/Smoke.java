import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import daybreak.abilitywar.utils.base.minecraft.nms.*;
import daybreak.abilitywar.utils.base.minecraft.nms.v26_3.*;
import daybreak.abilitywar.utils.base.minecraft.damage.v26_3.DamageImpl;
import daybreak.abilitywar.utils.base.minecraft.raytrace.v26_3.RayTraceImpl;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.ParticleLib;

public class Smoke extends JavaPlugin {
    private int passed, failed;
    private void check(String name, Runnable test) {
        try { test.run(); passed++; getLogger().info("PASS " + name); }
        catch (Throwable t) { failed++; getLogger().log(java.util.logging.Level.SEVERE, "FAIL " + name, t); }
    }
    private void require(boolean condition) { if (!condition) throw new AssertionError(); }
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            final Location location = Bukkit.getWorlds().get(0).getSpawnLocation().add(0, 3, 0);
            final World world = location.getWorld();
            final INMS nms = new NMSImpl();
            check("server implementation", () -> {
                if (Bukkit.getName().equalsIgnoreCase("Paper")) {
                    require(daybreak.abilitywar.utils.base.minecraft.server.ServerType.getServerType() == daybreak.abilitywar.utils.base.minecraft.server.ServerType.PAPER);
                } else if (Bukkit.getName().equalsIgnoreCase("CraftBukkit")) {
                    require(daybreak.abilitywar.utils.base.minecraft.server.ServerType.getServerType() == daybreak.abilitywar.utils.base.minecraft.server.ServerType.CRAFTBUKKIT);
                } else throw new AssertionError("Unexpected test server: " + Bukkit.getName());
            });
            check("sound library", () -> SoundLib.ENTITY_PLAYER_LEVELUP.playSound(location, 1, 1));
            check("particle library", () -> {
                try {
                    java.lang.reflect.Field field=ParticleLib.SimpleParticle.class.getDeclaredField("particle");field.setAccessible(true);
                    require(field.get(ParticleLib.FLAME)!=null);require(field.get(ParticleLib.REDSTONE)!=null);require(field.get(ParticleLib.ITEM_CRACK)!=null);
                } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
            });
            check("custom skull", () -> require(nms.createCustomSkull("c3832ce7fbf00dd0eaa537918612cd2f2e9a2a8b6ec5821932d38768f955ce9") != null));
            check("hologram lifecycle", () -> {
                IHologram hologram = nms.newHologram(world, location.getX(), location.getY(), location.getZ(), "quote \" newline\n 한글");
                require(hologram.getText().contains("한글")); hologram.setText("updated");
                hologram.teleport(world, location.getX()+1, location.getY(), location.getZ(), 40, 0);
                require(hologram.getText().equals("updated")); hologram.unregister(); require(hologram.isUnregistered());
            });
            check("world border", () -> { IWorldBorder b=nms.createWorldBorder(world); b.setSize(50);b.setCenter(5,5);b.setWorld(world);b.setDamageBuffer(3); });
            check("raytrace", () -> {
                Location block=location.clone().add(10,0,0); block.getBlock().setType(Material.STONE);
                require(new RayTraceImpl().hitsBlock(world,block.getBlockX()-1,block.getBlockY()+0.5,block.getBlockZ()+0.5,block.getBlockX()+2,block.getBlockY()+0.5,block.getBlockZ()+0.5));
                block.getBlock().setType(Material.AIR);
            });
            check("entity operations", () -> {
                ArmorStand stand=world.spawn(location,ArmorStand.class);
                try { nms.setAbsorptionHearts(stand,4);require(nms.getAbsorptionHearts(stand)==4);nms.setInvisible(stand,true);require(nms.isInvisible(stand));nms.setLocation(stand,location.getX()+1,location.getY(),location.getZ(),0,0);require(nms.getBoundingBox(stand)!=null);nms.swingHand(stand,Hand.MAIN_HAND);nms.removeBoundingBox(stand); }
                finally { stand.remove(); }
            });
            check("damage", () -> {
                Cow cow=world.spawn(location,Cow.class); Cow source=world.spawn(location,Cow.class);
                try { DamageImpl d=new DamageImpl(); require(d.damageMagic(cow,null,true,1));((org.bukkit.craftbukkit.entity.CraftLivingEntity)cow).getHandle().damageCooldownTime=0;require(d.damageArrow(cow,source,1));((org.bukkit.craftbukkit.entity.CraftLivingEntity)cow).getHandle().damageCooldownTime=0;require(d.damageFixed(cow,source,1));((org.bukkit.craftbukkit.entity.CraftLivingEntity)cow).getHandle().damageCooldownTime=0;require(d.damageThorn(cow,source,1));((org.bukkit.craftbukkit.entity.CraftLivingEntity)cow).getHandle().damageCooldownTime=0;require(d.damageExplosion(cow,source,1)); }
                finally { cow.remove();source.remove(); }
            });
            check("arrow ground state", () -> {
                Arrow arrow=world.spawn(location,Arrow.class);
                try {nms.setInGround(arrow,true);require(arrow.isInBlock());}finally{arrow.remove();}
            });
            check("ability registration", () -> {
                require(daybreak.abilitywar.ability.AbilityFactory.isRegistered(daybreak.abilitywar.ability.list.hermit.v26_3.Hermit.class));
                require(daybreak.abilitywar.ability.AbilityFactory.isRegistered(daybreak.abilitywar.ability.list.clown.v26_3.Clown.class));
            });
            check("invisibility packet isolation", () -> {
                try {
                    var filter=InvisiblePlayer.class.getDeclaredMethod("hidePacket",Object.class,int.class,boolean.class); filter.setAccessible(true);
                    var original=new net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket(42, java.util.List.of(
                        new net.minecraft.network.syncher.SynchedEntityData.DataValue<>(0,net.minecraft.network.syncher.EntityDataSerializers.BYTE,(byte) 1),
                        new net.minecraft.network.syncher.SynchedEntityData.DataValue<>(1,net.minecraft.network.syncher.EntityDataSerializers.INT,300)));
                    var result=(net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket)filter.invoke(null,original,42,false);
                    require(((Byte)result.packedItems().getFirst().value())==33);
                    require(((Byte)original.packedItems().getFirst().value())==1);
                    require(result.packedItems().get(1).equals(original.packedItems().get(1)));
                    require(filter.invoke(null,original,43,false)==original);
                    var equipment=new net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket(42,java.util.List.of(
                        com.mojang.datafixers.util.Pair.of(net.minecraft.world.entity.EquipmentSlot.HEAD,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET))));
                    require(filter.invoke(null,equipment,42,true)==equipment);
                    var hidden=(net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket)filter.invoke(null,equipment,42,false);
                    require(hidden.getSlots().stream().allMatch(pair -> pair.getSecond().isEmpty()));
                    require(!equipment.getSlots().getFirst().getSecond().isEmpty());
                    var bundle=new net.minecraft.network.protocol.game.ClientboundBundlePacket(java.util.List.of(original,equipment));
                    var hiddenBundle=(net.minecraft.network.protocol.game.ClientboundBundlePacket)filter.invoke(null,bundle,42,false);
                    var first=(net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket)hiddenBundle.subPackets().iterator().next();
                    require(((Byte)first.packedItems().getFirst().value())==33);
                    require(((Byte)original.packedItems().getFirst().value())==1);
                } catch (ReflectiveOperationException e) {throw new RuntimeException(e);}
            });
            check("dummy", () -> {
                IDummy dummy=nms.createDummy(location);require(dummy.isAlive());dummy.addDamage(2);
                Player player=dummy.getBukkitEntity();
                check("grappling hook cleanup", () -> {
                    long before=world.getEntitiesByClass(ArmorStand.class).size();
                    var hook=new daybreak.abilitywar.ability.list.grapplinghook.v26_3.HooksImpl().createHook(player,location.clone().add(4,0,0));
                    require(world.getEntitiesByClass(ArmorStand.class).size()==before+1);
                    hook.die();require(world.getEntitiesByClass(ArmorStand.class).size()==before);
                });nms.setCooldown(player,Material.STONE,20);require(nms.hasCooldown(player,Material.STONE));nms.getSteeringDirection(player);nms.isJumpingInVehicle(player);
                Bukkit.getScheduler().runTaskLater(this, () -> check("dummy cleanup", () -> {dummy.remove();require(!dummy.isAlive());}), 5L);
            });
            Runnable finish = () -> { getLogger().info("SMOKE RESULT passed="+passed+" failed="+failed); Bukkit.shutdown(); };
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (Boolean.getBoolean("abilitywar.gameplaySmoke")) {
                    check("start gameplay regressions", () -> new GameplaySmoke(this, this::check, finish).start());
                    Bukkit.getScheduler().runTaskLater(this, () -> { getLogger().severe("Gameplay smoke timeout"); Bukkit.shutdown(); }, 800L);
                } else finish.run();
            }, 30L);
        },40L);
    }
}
