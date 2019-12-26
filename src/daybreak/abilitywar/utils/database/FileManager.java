package daybreak.abilitywar.utils.database;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * File 유틸
 *
 * @author Daybreak 새벽
 */
public class FileManager {

	private static final Logger logger = Logger.getLogger(FileManager.class.getName());
	private static final File mainDirectory = new File("plugins/AbilityWar");

	static {
		if (!mainDirectory.exists()) {
			mainDirectory.mkdirs();
		}
	}

	private FileManager() {
	}

	public static File createFile(String path) {
		File file = new File(mainDirectory.getPath() + "/" + path);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			return file;
		} catch (IOException e) {
			logger.log(Level.SEVERE, file.getPath() + " 파일을 생성하지 못했습니다.");
			return file;
		}
	}

	public static File createDirectory(String path) {
		File directory = new File(mainDirectory.getPath() + "/" + path);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}

}
