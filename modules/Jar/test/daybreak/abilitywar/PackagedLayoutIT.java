package daybreak.abilitywar;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.*;

/** Verifies packaging ownership without changing the shaded addon-facing Plugin artifact. */
public class PackagedLayoutIT {
    @Test
    public void distributionContainsEveryRequiredAdapter() throws Exception {
        try (ZipFile distribution = archive("packagedJar")) {
            for (String version : System.getProperty("packagedAdapters").split(",")) {
                for (String path : new String[] {
                        "utils/base/minecraft/nms/" + version + "/NMSImpl.class",
                        "utils/base/minecraft/nms/" + version + "/HologramImpl.class",
                        "utils/base/minecraft/nms/" + version + "/DummyImpl.class",
                        "utils/base/minecraft/damage/" + version + "/DamageImpl.class",
                        "utils/base/minecraft/raytrace/" + version + "/RayTraceImpl.class",
                        "ability/list/clown/" + version + "/Clown.class",
                        "ability/list/hermit/" + version + "/Hermit.class",
                        "ability/list/grapplinghook/" + version + "/HooksImpl.class"}) {
                    assertNotNull("Missing adapter: " + path, distribution.getEntry("daybreak/abilitywar/" + path));
                }
            }
            if (System.getProperty("packagedAdapters").contains("v1_12_R1")) {
                for (String ability : new String[] {"scarecrow/v1_12_R1/ScareCrow", "soul/v1_12_R1/Soul"}) {
                    assertNotNull("Missing legacy ability: " + ability,
                            distribution.getEntry("daybreak/abilitywar/ability/list/" + ability + ".class"));
                }
            }
        }
    }

    @Test
    public void distributionPreservesPluginAndAdapterClasses() throws Exception {
        try (ZipFile distribution = archive("packagedJar");
             ZipFile plugin = archive("pluginJar");
             ZipFile adapter = archive("modernAdapterJar")) {
            assertClassesPreserved(plugin, distribution);
            assertClassesPreserved(adapter, distribution);
            assertArrayEquals(read(plugin, "plugin.yml"), read(distribution, "plugin.yml"));
            assertNotNull(distribution.getEntry("daybreak/google/common/collect/Multimap.class"));
            assertNull(distribution.getEntry("com/google/common/collect/Multimap.class"));
        }
    }

    @Test
    public void distributionHasNoDuplicateEntries() throws Exception {
        try (ZipFile distribution = archive("packagedJar")) {
            Set<String> names = new HashSet<>();
            for (Enumeration<? extends ZipEntry> entries = distribution.entries(); entries.hasMoreElements();) {
                String name = entries.nextElement().getName();
                assertTrue("Duplicate distribution entry: " + name, names.add(name));
            }
        }
    }

    private static ZipFile archive(String property) throws IOException {
        return new ZipFile(Paths.get(System.getProperty(property)).toFile());
    }

    private static void assertClassesPreserved(ZipFile source, ZipFile distribution) throws IOException {
        int count = 0;
        for (Enumeration<? extends ZipEntry> entries = source.entries(); entries.hasMoreElements();) {
            String name = entries.nextElement().getName();
            if (!name.endsWith(".class") || name.startsWith("META-INF/")) continue;
            assertArrayEquals("Changed or stale packaged class: " + name, read(source, name), read(distribution, name));
            count++;
        }
        assertTrue("No classes checked in " + source.getName(), count > 0);
    }

    private static byte[] read(ZipFile archive, String name) throws IOException {
        ZipEntry entry = archive.getEntry(name);
        assertNotNull("Missing " + name + " in " + archive.getName(), entry);
        try (InputStream input = archive.getInputStream(entry);
             ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) bytes.write(buffer, 0, count);
            return bytes.toByteArray();
        }
    }
}
