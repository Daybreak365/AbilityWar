package daybreak.abilitywar.utils.base.math;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class LocationUtilTest {

    @Test
    public void locationCollectionPreservesNegativeChunkBoundsAndEntityOrder() {
        WorldFixture fixture = new WorldFixture();
        Entity first = fixture.entity(Entity.class, -17, 64, -1);
        Entity second = fixture.entity(Entity.class, -16, 64, 0);
        Entity third = fixture.entity(Entity.class, 1, 64, 16);
        fixture.entities.put("-2,-1", new Entity[] {first});
        fixture.entities.put("-1,0", new Entity[] {second, first});
        fixture.entities.put("0,1", new Entity[] {third});

        Entity[] result = LocationUtil.collectEntities(new Location(fixture.world, -0.25, 64, 15.75), 16);

        assertArrayEquals(new Entity[] {first, second, first, third}, result);
        assertEquals(Arrays.asList("-2,-1", "-2,0", "-2,1", "-1,-1", "-1,0", "-1,1", "0,-1", "0,0", "0,1"), fixture.requests);
    }

    @Test
    public void chunkCollectionPreservesExplicitBoundsAndEmptyChunks() {
        WorldFixture fixture = new WorldFixture();
        Entity first = fixture.entity(Entity.class, -1, 64, 0);
        Entity second = fixture.entity(Entity.class, 16, 64, 16);
        fixture.entities.put("-1,0", new Entity[] {first});
        fixture.entities.put("1,1", new Entity[] {second});

        Entity[] result = LocationUtil.collectEntities(fixture.chunk(9, 9), -1, 0, 1, 1);

        assertArrayEquals(new Entity[] {first, second}, result);
        assertEquals(Arrays.asList("-1,0", "-1,1", "0,0", "0,1", "1,0", "1,1"), fixture.requests);
        assertArrayEquals(new Entity[0], LocationUtil.collectEntities(fixture.chunk(9, 9), 1, 0, 0, 1));
        assertEquals(6, fixture.requests.size());
    }

    @Test
    public void nearbyCollectionKeepsCylinderHeightTypeAndPredicateOrder() {
        WorldFixture fixture = new WorldFixture();
        LivingEntity negativeEdge = fixture.entity(LivingEntity.class, -3, 62, 0);
        LivingEntity positiveEdge = fixture.entity(LivingEntity.class, 3, 66, 0);
        LivingEntity diagonal = fixture.entity(LivingEntity.class, 3, 64, 3);
        LivingEntity tooHigh = fixture.entity(LivingEntity.class, 0, 66.1, 0);
        LivingEntity rejected = fixture.entity(LivingEntity.class, 0, 64, 0);
        Entity otherType = fixture.entity(Entity.class, 0, 64, 0);
        fixture.entities.put("-1,0", new Entity[] {negativeEdge});
        fixture.entities.put("0,0", new Entity[] {otherType, positiveEdge, diagonal, tooHigh, rejected});
        List<LivingEntity> checked = new ArrayList<>();

        List<LivingEntity> result = LocationUtil.getNearbyEntities(LivingEntity.class,
                new Location(fixture.world, 0, 64, 0), 3, 2, entity -> {
                    checked.add(entity);
                    return entity != rejected;
                });

        assertEquals(Arrays.asList(negativeEdge, positiveEdge), result);
        assertEquals(Arrays.asList(negativeEdge, positiveEdge, rejected), checked);
        assertEquals(Arrays.asList("-1,-1", "-1,0", "0,-1", "0,0"), fixture.requests);
    }

    private static final class WorldFixture {
        private final Map<String, Entity[]> entities = new HashMap<>();
        private final List<String> requests = new ArrayList<>();
        private final World world = proxy(World.class, (proxy, method, args) -> {
            if (method.getName().equals("getChunkAt") && args.length == 2) {
                int x = (Integer) args[0], z = (Integer) args[1];
                requests.add(x + "," + z);
                return chunk(x, z);
            }
            throw new AssertionError("Unexpected World method: " + method);
        });

        private Chunk chunk(int x, int z) {
            return proxy(Chunk.class, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getWorld": return world;
                    case "getX": return x;
                    case "getZ": return z;
                    case "getEntities": return entities.getOrDefault(x + "," + z, new Entity[0]);
                    default: throw new AssertionError("Unexpected Chunk method: " + method);
                }
            });
        }

        private <T extends Entity> T entity(Class<T> type, double x, double y, double z) {
            return proxy(type, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getLocation": return new Location(world, x, y, z);
                    case "getWorld": return world;
                    default: throw new AssertionError("Unexpected Entity method: " + method);
                }
            });
        }
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                switch (method.getName()) {
                    case "equals": return proxy == args[0];
                    case "hashCode": return System.identityHashCode(proxy);
                    case "toString": return type.getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
                    default: throw new AssertionError(method);
                }
            }
            return handler.invoke(proxy, method, args);
        }));
    }
}
