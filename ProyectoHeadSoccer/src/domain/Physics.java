package domain;

/**
 * Global physical constants and utility calculations for the game physics engine.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public final class Physics {

    
    
    /*
     * Private constructor for utility class.
     */
    private Physics() {
    }

    public static final int FPS = 60;
    public static final double GRAVITY = 1.0;
    public static final double RESTITUTION = 0.48;
    public static final double CORNER_RESTITUTION = 0.74;
    public static final double HEAD_RESTITUTION = RESTITUTION;
    public static final double BODY_RESTITUTION = RESTITUTION;
    public static final double FOOT_RESTITUTION = RESTITUTION;
    public static final double CROSSBAR_RESTITUTION = RESTITUTION;
    public static final double HEAD_IMPULSE = 10.0;
    public static final double KICK_LIFT_FACTOR = -1.45;
    public static final double FRICTION = 0.95;
    public static final int TICK_MS = 1000/FPS;

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
