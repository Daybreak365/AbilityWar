package daybreak.abilitywar.utils.installer;

import java.util.Comparator;

public class Version implements Comparable<Version> {

	static final Comparator<Version> comparator = new Comparator<Version>() {
		@Override
		public int compare(Version first, Version second) {
			return first.compareTo(second);
		}
	};

	private final String versionString;
	private final int[] version;

	Version(String versionString) {
		this.versionString = versionString;
		String[] split = versionString.split("\\.");
		int[] version = new int[split.length];
		for (int i = 0; i < split.length; i++) {
			version[i] = Integer.parseInt(split[i]);
		}
		this.version = version;
	}

	public String getVersionString() {
		return versionString;
	}

	public int[] getVersion() {
		return version;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Version) {
			return ((Version) object).versionString.equals(versionString);
		}
		return false;
	}

	@Override
	public int compareTo(Version compare) {
		int[] comparedVersion = compare.version;
		for (int i = 0; i < Math.max(version.length, comparedVersion.length); i++) {
			int token = i + 1 <= version.length ? version[i] : 0;
			int comparedToken = i + 1 <= comparedVersion.length ? comparedVersion[i] : 0;
			if (comparedToken < token) {
				return 1;
			} else if (comparedToken != token) {
				return -1;
			}
		}
		return 0;
	}

}
