package domain;

/**
 * Global physical constants and utility calculations for the game physics engine.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public final class Physics {
    /*
     * Private constructor for utility class.
     */
    private Physics() {
    }

    public static final int FPS = 60;
    public static final double GRAVITY = 2.0;
    public static final double RESTITUTION = 0.78;
    public static final double CORNER_RESTITUTION = 0.94;
    public static final double FRICTION = 0.6;

    /**
     * Converts a duration in seconds to equivalent frame ticks.
     * 
     * @param s Duration in seconds
     * @return Number of frames
     */
    public static int seconds(double s) {
        return (int) Math.round(s * FPS);
    }
}
