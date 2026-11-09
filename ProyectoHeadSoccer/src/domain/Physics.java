package domain;

public final class Physics {
    private Physics() {
        // Utility class
    }

    public static final int FPS = 60;
    public static final double GRAVITY = 2.0;
    public static final double RESTITUTION = 0.78;
    public static final double CORNER_RESTITUTION = 0.94;
    public static final double FRICTION = 0.6;

    public static int seconds(double s) {
        return (int) Math.round(s * FPS);
    }
}
