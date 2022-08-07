package daybreak.abilitywar.utils.base.minecraft.nms;

public enum SteeringDirection {
    FIXED(0, 0),
    FORWARD(0, 1),
    BACKWARD(0, -1),
    LEFT(1, 0),
    RIGHT(-1, 0),
    FORWARD_RIGHT(FORWARD, RIGHT),
    FORWARD_LEFT(FORWARD, LEFT),
    BACKWARD_RIGHT(BACKWARD, RIGHT),
    BACKWARD_LEFT(BACKWARD, LEFT);

    private static final SteeringDirection[][] map = new SteeringDirection[3][3];

    static {
        for (SteeringDirection value : values()) {
            map[value.xxa + 1][value.zza + 1] = value;
        }
    }

    public static SteeringDirection get(float xxa, float zza) {
        return map[(int) Math.signum(xxa) + 1][(int) Math.signum(zza) + 1];
    }

    private final int xxa, zza;

    SteeringDirection(final int xxa, final int zza) {
        this.xxa = xxa;
        this.zza = zza;
    }

    SteeringDirection(final SteeringDirection a, final SteeringDirection b) {
        this.xxa = a.xxa + b.xxa;
        this.zza = a.zza + b.zza;
    }

    @Override
    public String toString() {
        return name();
    }
}
