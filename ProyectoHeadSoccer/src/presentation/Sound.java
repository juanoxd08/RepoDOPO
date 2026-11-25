package presentation;

/**
 * Enumerates sound effect clips played in response to match gameplay events.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public enum Sound {
    GOAL("goal.wav"),
    STUNNED("stunned.wav"),
    FINISH("finish.wav"),
    KICK("kick.wav"),
    POWER_AURA("power_aura.wav"),
    POWER_SHOT("power_shot.wav"),
    POWER_HIT("power_hit.wav");

    private final String name;

    /*
     * Constructs a sound enum constant mapped to its WAV resource filename.
     */
    Sound(String name) {
        this.name = name;
    }

    /**
     * Returns the WAV resource filename for this sound effect.
     *
     * @return Audio filename
     */
    public String getFileName() {
        return this.name;
    }
}