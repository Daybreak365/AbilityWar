package daybreak.abilitywar;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.*;

/** Reads class headers without loading server-specific classes into the test JVM. */
public class PackagedAdapterApiIT {
    @Test
    public void remappedWorldBordersRetainThePluginInterface() throws Exception {
        try (ZipFile jar = new ZipFile(System.getProperty("packagedJar"))) {
            for (String version : System.getProperty("packagedAdapters").split(",")) {
                if (!(version.startsWith("v1_18_") || version.startsWith("v1_19_")
                        || version.startsWith("v1_20_") || version.startsWith("v1_21_")
                        || version.startsWith("v26_"))) continue;
                ZipEntry entry = jar.getEntry("daybreak/abilitywar/utils/base/minecraft/nms/"
                        + version + "/WorldBorderImpl.class");
                assertNotNull(entry);
                Set<String> methods;
                try (DataInputStream input = new DataInputStream(jar.getInputStream(entry))) {
                    methods = publicMethods(input);
                }
                for (String method : new String[] {"setWorld(Lorg/bukkit/World;)V", "setCenter(DD)V",
                        "setSize(D)V", "setDamageAmount(D)V", "setDamageBuffer(D)V",
                        "setWarningDistance(I)V", "setWarningTime(I)V"}) {
                    assertTrue(version + " lost IWorldBorder." + method, methods.contains(method));
                }
            }
        }
    }

    @Test
    public void reobfuscationPreservesDummyInterfaceMethods() throws Exception {
        try (ZipFile jar = new ZipFile(System.getProperty("packagedJar"))) {
            for (String version : System.getProperty("packagedAdapters").split(",")) {
                if (!version.equals("v1_20_R4") && !version.startsWith("v1_21_R")) continue;
                ZipEntry entry = jar.getEntry("daybreak/abilitywar/utils/base/minecraft/nms/"
                        + version + "/DummyImpl.class");
                assertNotNull("Missing " + version + " dummy", entry);
                Set<String> methods;
                try (DataInputStream input = new DataInputStream(jar.getInputStream(entry))) {
                    methods = publicMethods(input);
                }
                for (String method : new String[] {"isAlive()Z", "remove()V", "addDamage(D)V",
                        "display(Lorg/bukkit/entity/Player;)V", "getUniqueID()Ljava/util/UUID;",
                        "getBukkitEntity()Lorg/bukkit/entity/Player;"}) {
                    assertTrue(version + " reobfuscation lost IDummy." + method, methods.contains(method));
                }
            }
        }
    }

    @Test
    public void universalJarCanBeReadByJava21PaperRemappers() throws Exception {
        try (ZipFile jar = new ZipFile(System.getProperty("packagedJar"))) {
            for (Enumeration<? extends ZipEntry> entries = jar.entries(); entries.hasMoreElements();) {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                try (DataInputStream input = new DataInputStream(jar.getInputStream(entry))) {
                    assertEquals(0xCAFEBABE, input.readInt());
                    input.readUnsignedShort();
                    int major = input.readUnsignedShort();
                    assertTrue("Paper 1.20.5/1.21 remaps the entire JAR: " + entry.getName()
                            + " has class version " + major, major <= 65);
                }
            }
        }
    }

    private static Set<String> publicMethods(DataInputStream input) throws IOException {
        assertEquals(0xCAFEBABE, input.readInt());
        input.readInt(); // minor and major
        String[] strings = new String[input.readUnsignedShort()];
        for (int index = 1; index < strings.length; index++) {
            switch (input.readUnsignedByte()) {
                case 1: strings[index] = input.readUTF(); break;
                case 3: case 4: case 9: case 10: case 11: case 12: case 17: case 18:
                    skip(input, 4); break;
                case 5: case 6: skip(input, 8); index++; break;
                case 7: case 8: case 16: case 19: case 20: skip(input, 2); break;
                case 15: skip(input, 3); break;
                default: throw new IOException("Unknown constant pool tag");
            }
        }
        skip(input, 6); // flags, this class, superclass
        skip(input, 2L * input.readUnsignedShort());
        int fields = input.readUnsignedShort();
        for (int field = 0; field < fields; field++) {
            skip(input, 6);
            skipAttributes(input);
        }
        Set<String> methods = new HashSet<>();
        int count = input.readUnsignedShort();
        for (int method = 0; method < count; method++) {
            int flags = input.readUnsignedShort();
            String name = strings[input.readUnsignedShort()];
            String descriptor = strings[input.readUnsignedShort()];
            if ((flags & 1) != 0 && (flags & 0x400) == 0) methods.add(name + descriptor);
            skipAttributes(input);
        }
        return methods;
    }

    private static void skipAttributes(DataInputStream input) throws IOException {
        int count = input.readUnsignedShort();
        for (int attribute = 0; attribute < count; attribute++) {
            input.readUnsignedShort();
            skip(input, Integer.toUnsignedLong(input.readInt()));
        }
    }

    private static void skip(DataInputStream input, long count) throws IOException {
        while (count > 0) {
            long skipped = input.skip(count);
            if (skipped == 0) { input.readByte(); skipped = 1; }
            count -= skipped;
        }
    }
}
