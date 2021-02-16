package daybreak.abilitywar.utils.base.minecraft;

import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WorldReset {

	public static final ImmutableSet<String> defaultWorlds = ImmutableSet.of("world", "world_nether", "world_the_end");
	private static final File mainDirectory = FileUtil.newDirectory("mapBackup");

	private WorldReset() {}

	public static boolean hasBackup(World world) {
		final File file = getBackup(world);
		return file.exists();
	}

	public static File getBackup(World world) {
		return new File(mainDirectory, world.getName() + ".zip");
	}

	public static boolean resetWorld(World world) {
		if (defaultWorlds.contains(world.getName())) return false;
		if (!hasBackup(world)) return false;
		Bukkit.getConsoleSender().sendMessage(Messager.defaultPrefix + "§e" + world.getName() + " §f월드 초기화 시작");
		final File worldFile = new File(world.getName()), backup = getBackup(world);
		{
			final Location safe = Bukkit.getWorlds().get(0).getSpawnLocation();
			for (Player player : world.getPlayers()) {
				player.teleport(safe);
				player.sendMessage(Messager.defaultPrefix + "안전한 곳으로 강제 이동됩니다.");
			}
		}
		Bukkit.unloadWorld(world, false);
		worldFile.delete();
		try (final FileInputStream inputStream = new FileInputStream(backup)) {
			try (final ZipInputStream zipStream = new ZipInputStream(inputStream)) {
				final byte[] buffer = new byte[1024];
				ZipEntry zipEntry;
				while ((zipEntry = zipStream.getNextEntry()) != null) {
					final File file = new File(worldFile, zipEntry.getName());
					if (zipEntry.isDirectory()) {
						file.mkdirs();
					} else {
						final File parent = file.getParentFile();
						if (parent.isDirectory()) {
							parent.mkdirs();
						}
						try (final FileOutputStream outputStream = new FileOutputStream(file)) {
							int length;
							while ((length = zipStream.read(buffer)) > 0) {
								outputStream.write(buffer, 0, length);
							}
						}
					}
				}
				zipStream.closeEntry();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		new WorldCreator(world.getName()).createWorld();
		Bukkit.getConsoleSender().sendMessage(Messager.defaultPrefix + "§e" + world.getName() + " §f월드 초기화 완료");
		return true;
	}

	public static void resetWorlds() {
		for (World world : Bukkit.getWorlds()) {
			resetWorld(world);
		}
	}

}
