package daybreak.abilitywar;

import org.junit.Test;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipFile;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

/** Runs after shading, against the actual distributable rather than reactor classes. */
public class PackagedVersionIT {
    private static final String PACKAGE = "daybreak/abilitywar/utils/base/minecraft/version/";
    private final Path jar = Paths.get(System.getProperty("packagedJar"));

    @Test public void universalDistributionContainsEveryRecognizedVersion() throws Exception {
        Set<String> packaged = new HashSet<>(Arrays.asList(System.getProperty("packagedAdapters").split(",")));
        if (!packaged.contains("v1_12_R1")) return; // Explicit single-version developer profile.
        try (URLClassLoader loader = new URLClassLoader(new URL[] {jar.toUri().toURL()}, null)) {
            Class<?> version = loader.loadClass("daybreak.abilitywar.utils.base.minecraft.version.NMSVersion");
            Set<String> recognized = new HashSet<>();
            for (Object value : version.getEnumConstants()) {
                String name = ((Enum<?>) value).name();
                if (!name.equals("UNSUPPORTED")) recognized.add(name);
            }
            assertEquals("Resolver and universal packaging must cover the same adapters", recognized, packaged);
        }
    }

    @Test public void distributedJarRecognizesPaper222AndCraftBukkit() throws Exception {
        // No parent loader: stale classes in the final JAR cannot be masked by test dependencies.
        try (URLClassLoader loader = new URLClassLoader(new URL[] {jar.toUri().toURL()}, null)) {
            Class<?> version = loader.loadClass("daybreak.abilitywar.utils.base.minecraft.version.NMSVersion");
            for (String input : new String[] {"26.3.build.222-stable", "26.3.local-SNAPSHOT", "26.3-R0.1-SNAPSHOT"}) {
                Object actual = version.getMethod("resolve", String.class, String.class)
                        .invoke(null, "org.bukkit.craftbukkit.CraftServer", input);
                assertEquals(input, "v26_3", ((Enum<?>) actual).name());
            }
            Object unsupported = version.getMethod("resolve", String.class, String.class)
                    .invoke(null, "org.bukkit.craftbukkit.CraftServer", "26.4.build.1-stable");
            assertEquals("UNSUPPORTED", ((Enum<?>) unsupported).name());
        }
    }

    @Test public void distributedJarRecognizesModernAndLegacyServerBrands() throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[] {jar.toUri().toURL()}, null)) {
            Class<?> type = loader.loadClass("daybreak.abilitywar.utils.base.minecraft.server.ServerType");
            String[][] cases = {{"Paper", "26.3-222-e3ba7b1 (MC: 26.3)", "PAPER"},
                    {"CraftBukkit", "4663-Bukkit-c4db4c3", "CRAFTBUKKIT"},
                    {"Unknown", "git-Paper-123 (MC: 1.20.4)", "PAPER"},
                    {"Unknown", "git-Spigot-abc", "SPIGOT"}, {"Unknown", "unknown", "UNKNOWN"}};
            for (String[] test : cases) {
                Object actual = type.getMethod("resolve", String.class, String.class).invoke(null, test[0], test[1]);
                assertEquals(test[1], test[2], ((Enum<?>) actual).name());
            }
        }
    }

    @Test public void distributedVersionClassesMatchCurrentCompilation() throws Exception {
        try (ZipFile archive = new ZipFile(jar.toFile())) {
            for (String name : new String[] {"NMSVersion.class", "NMSVersion$Companion.class", "ServerVersion.class", "ServerVersion$INSTANCE.class"}) {
                String entry = PACKAGE + name;
                assertNotNull(entry, archive.getEntry(entry));
                try (InputStream input = archive.getInputStream(archive.getEntry(entry));
                     ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = input.read(buffer)) != -1) bytes.write(buffer, 0, count);
                    assertArrayEquals("Stale packaged class: " + entry,
                            Files.readAllBytes(Paths.get(System.getProperty("compatibilityClasses"), entry)),
                            bytes.toByteArray());
                }
            }
        }
    }
}
