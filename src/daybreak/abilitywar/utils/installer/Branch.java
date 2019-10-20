package daybreak.abilitywar.utils.installer;

public enum Branch {

	Master("master");

	String Name;

	private Branch(String Name) {
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
			return Branch.Master;
		}
		default: {
			return null;
		}
		}
	}

}
