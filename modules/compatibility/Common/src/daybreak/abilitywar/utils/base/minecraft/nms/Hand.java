package daybreak.abilitywar.utils.base.minecraft.nms;

public enum Hand {
	MAIN_HAND {
		@Override
		public Hand opposite() {
			return OFF_HAND;
		}
	},
	OFF_HAND {
		@Override
		public Hand opposite() {
			return MAIN_HAND;
		}
	};

	public abstract Hand opposite();
}
