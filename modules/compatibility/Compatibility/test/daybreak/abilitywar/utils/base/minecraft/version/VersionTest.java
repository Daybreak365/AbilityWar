package daybreak.abilitywar.utils.base.minecraft.version;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class VersionTest {
    @Test public void resolvesEveryLegacyAdapter() {
        for (NMSVersion version : NMSVersion.values()) {
            if (version.getVersionedPackage()) {
                for (String release : version.getMinecraftVersions()) {
                    assertEquals(version, NMSVersion.resolve("org.bukkit.craftbukkit." + version.name() + ".CraftServer", release + "-R0.1-SNAPSHOT"));
                }
            }
        }
    }
    @Test public void resolvesEveryModernReleaseAndServerVersionFormat() {
        for (NMSVersion adapter : NMSVersion.values()) {
            if (!adapter.getUnversionedPackage()) continue;
            for (String release : adapter.getMinecraftVersions()) {
                for (String suffix : new String[] {"", "-R0.1-SNAPSHOT", ".build.222-stable", ".build.1-experimental", ".local-SNAPSHOT"}) {
                    assertEquals(release + suffix, adapter, NMSVersion.resolve("org.bukkit.craftbukkit.CraftServer", release + suffix));
                }
            }
        }
    }
    @Test public void releaseFamiliesDoNotOverlap() {
        Set<String> seen = new HashSet<>();
        for (NMSVersion adapter : NMSVersion.values()) {
            for (String release : adapter.getMinecraftVersions()) assertTrue("Duplicate release: " + release, seen.add(release));
        }
    }
    @Test public void rejectsUnknownPrereleaseAndMalformedVersions() {
        for (String release : new String[] {"26.4.build.1-stable", "26.3.1.build.1-stable", "26.3-pre-1.build.1-experimental", "26.3-rc-1.local-SNAPSHOT", "26.3.build.x-stable", "26.3.build.222", "26.3-Rgarbage", "26.30", "26.3.local-SNAPSHOT-extra", "1.21.12", "1.21.1-pre1", "", "1.20.4-R0.1-SNAPSHOT"}) {
            assertEquals(release, NMSVersion.UNSUPPORTED, NMSVersion.resolve("org.bukkit.craftbukkit.CraftServer", release));
        }
        assertEquals(NMSVersion.UNSUPPORTED, NMSVersion.resolve("Unknown", "26.3"));
        assertEquals(NMSVersion.UNSUPPORTED, NMSVersion.resolve("example.fake.package.v1_12_R1.CraftServer", "1.12.2"));
    }
    @Test public void revisionPackageDoesNotAcceptAnotherReleaseOrPrerelease() {
        for (String release : new String[] {"1.20.2", "1.20.4-pre1", "1.20.4-rc1", "garbage"}) {
            assertEquals(release, NMSVersion.UNSUPPORTED,
                    NMSVersion.resolve("org.bukkit.craftbukkit.v1_20_R3.CraftServer", release));
        }
        assertEquals(NMSVersion.UNSUPPORTED,
                NMSVersion.resolve("org.bukkit.craftbukkit.v1_21_R7.CraftServer", "1.21.12-R0.1-SNAPSHOT"));
    }
    @Test public void modernVersionSortsAfterLegacy() {
        assertTrue(NMSVersion.v26_3.isAboveOrEqual(NMSVersion.v1_20_R3));
        assertTrue(NMSVersion.v1_18_R2.isAboveOrEqual(NMSVersion.v1_18_R1));
        assertTrue(NMSVersion.v1_21_R1.isAboveOrEqual(NMSVersion.v1_20_R4));
        assertTrue(NMSVersion.v26_1.isAboveOrEqual(NMSVersion.v1_21_R7));
        assertTrue(NMSVersion.v1_20_R3.isBelowOrEqual(NMSVersion.v26_3));
        assertFalse(NMSVersion.v26_3.isBelowOrEqual(NMSVersion.v1_20_R3));
    }
}
