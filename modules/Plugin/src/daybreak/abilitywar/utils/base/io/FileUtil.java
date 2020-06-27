package daybreak.abilitywar.utils.base.io;

import daybreak.abilitywar.utils.base.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * File 유틸
 *
 * @author Daybreak 새벽
 */
public class FileUtil {

	private static final Logger logger = Logger.getLogger(FileUtil.class.getName());
	private static final File mainDirectory = new File("plugins/AbilityWar");

	static {
		if (!mainDirectory.exists()) {
			mainDirectory.mkdirs();
		}
	}

	private FileUtil() {
	}

	public static File newFile(String path) {
		File file = new File(mainDirectory.getPath() + "/" + path);
		try {
			if (!file.exists()) {
				if (file.getParentFile() != null && !file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}

			return file;
		} catch (IOException e) {
			logger.log(Level.SEVERE, file.getPath() + " 파일을 생성하지 못했습니다.");
			return file;
		}
	}

	public static File newDirectory(String path) {
		File directory = new File(mainDirectory.getPath() + "/" + path);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}

}
