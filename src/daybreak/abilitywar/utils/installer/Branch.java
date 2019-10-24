package daybreak.abilitywar.utils.installer;

public enum Branch {

	MASTER("master");

	private final String Name;

	Branch(String Name) {
		this.Name = Name;
	}

	public String getName() {
		return Name;
	}

	public static Branch getBranch(Integer Version) {
		switch (Version) {
		case 12:
		case 13:
		case 14: {
			return Branch.MASTER;
		}
		default: {
			return null;
		}
		}
	}

}
