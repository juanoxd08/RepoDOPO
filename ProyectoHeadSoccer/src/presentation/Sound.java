package presentation;

public enum Sound{
    GOAL("goal.wav"),
    STUNNED("stunned.wav"),
    FINISH("finish.wav"),
    KICK("kick.wav"),
    POWER_AURA("power_aura.wav"),
    POWER_SHOT("power_shot.wav"),
    POWER_HIT("power_hit.wav");

    private final String name;

    Sound(String name){
        this.name = name;
    }

    public String getFileName(){
        return this.name;
    }
    
}