package daybreak.abilitywar.utils.base.minecraft.version;

import org.junit.Test;
import static org.junit.Assert.*;

public class VersionTest {
    @Test public void resolvesEveryLegacyAdapter() {
        for (NMSVersion version : NMSVersion.values()) {
            if (version != NMSVersion.UNSUPPORTED && version != NMSVersion.v26_3) {
                assertEquals(version, NMSVersion.resolve("org.bukkit.craftbukkit." + version.name() + ".CraftServer", "ignored"));
            }
        }
    }
    @Test public void resolvesUnversionedCraftBukkitAndPaper() {
        assertEquals(NMSVersion.v26_3, NMSVersion.resolve("org.bukkit.craftbukkit.CraftServer", "26.3-R0.1-SNAPSHOT"));
        assertEquals(NMSVersion.v26_3, NMSVersion.resolve("org.bukkit.craftbukkit.CraftServer", "26.3"));
    }
    @Test public void resolvesPaperBuildAndLocalVersionFormats() {
        for (String version : new String[] {"26.3.build.222-stable", "26.3.build.1-experimental", "26.3.local-SNAPSHOT"}) {
            assertEquals(version, NMSVersion.v26_3, NMSVersion.resolve("org.bukkit.craftbukkit.CraftServer", version));
        }
    }
    @Test public void rejectsOtherPaperVersionsAndMalformedVersions() {
        for (String version : new String[] {"26.4.build.1-stable", "26.3.1.build.1-stable", "26.3-pre-1.build.1-experimental", "26.3-rc-1.local-SNAPSHOT", "26.3.build.x-stable", "26.3.build.222", "26.3-Rgarbage", "26.30", "26.3.local-SNAPSHOT-extra"}) {
            assertEquals(version, NMSVersion.UNSUPPORTED, NMSVersion.resolve("org.bukkit.craftbukkit.CraftServer", version));
        }
    }
    @Test public void rejectsUnimplementedAndPrereleaseVersions() {
        for (String version : new String[] {"26.1", "26.2", "26.3.1", "26.4", "26.3-pre-1", "26.3-rc-1", "26.3-snapshot-1", "1.21.11-R0.1-SNAPSHOT", ""}) {
            assertEquals(version, NMSVersion.UNSUPPORTED, NMSVersion.resolve("org.bukkit.craftbukkit.CraftServer", version));
        }
        assertEquals(NMSVersion.UNSUPPORTED, NMSVersion.resolve("Unknown", ""));
    }
    @Test public void modernVersionSortsAfterLegacy() {
        assertTrue(NMSVersion.v26_3.isAboveOrEqual(NMSVersion.v1_20_R3));
        assertTrue(NMSVersion.v1_20_R3.isBelowOrEqual(NMSVersion.v26_3));
        assertFalse(NMSVersion.v26_3.isBelowOrEqual(NMSVersion.v1_20_R3));
    }
}
