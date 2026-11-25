package presentation;

import domain.Country;
import domain.DOPOSoccerException;
import domain.MachineProfile;
import domain.MatchConfig;
import domain.MatchType;
import domain.Modality;

/**
 * Holds the user-selected match configuration across setup screens
 * (modality, countries, player names, match rule, target, machine profile, and resolution).
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class MatchSetup {
    private Modality modality = Modality.PVP;
    private Country leftCountry = Country.SOUTH_KOREA;
    private Country rightCountry = Country.CAMEROON;
    private String leftPlayerName = "jugador 1";
    private String rightPlayerName = "jugador 2";
    private MatchType matchType = MatchType.TIME;
    private int matchDurationMinutes = 2;
    private int targetGoals = 3;
    private FieldResolution resolution = FieldResolution.MEDIUM;
    private MachineProfile machineProfile = MachineProfile.EXPERTA;

    /**
     * Returns the configured display name for the left player.
     *
     * @return Left player name
     */
    public String getLeftPlayerName() {
        return leftPlayerName;
    }

    /**
     * Updates the configured display name for the left player.
     *
     * @param leftPlayerName Left player name
     */
    public void setLeftPlayerName(String leftPlayerName) {
        if (leftPlayerName != null && !leftPlayerName.trim().isEmpty()) {
            this.leftPlayerName = leftPlayerName.trim();
        }
    }

    /**
     * Returns the configured display name for the right player.
     *
     * @return Right player name
     */
    public String getRightPlayerName() {
        return rightPlayerName;
    }

    /**
     * Sets the right player display name for PVP matches.
     *
     * @param rightPlayerName Right player display name
     */
    public void setRightPlayerName(String rightPlayerName) {
        if (rightPlayerName != null && !rightPlayerName.trim().isEmpty()) {
            this.rightPlayerName = rightPlayerName.trim();
        }
    }

    /**
     * Returns the currently selected match modality.
     *
     * @return Match modality
     */
    public Modality getModality() {
        return modality;
    }

    /**
     * Sets the match modality.
     *
     * @param modality Target modality
     */
    public void setModality(Modality modality) {
        if (modality != null) {
            this.modality = modality;
        }
    }

    /**
     * Returns the selected country for the left player.
     *
     * @return Left country
     */
    public Country getLeftCountry() {
        return leftCountry;
    }

    /**
     * Cycles the left player's country to the next available option.
     */
    public void cycleLeftCountry() {
        Country[] countries = Country.values();
        leftCountry = countries[(leftCountry.ordinal() + 1) % countries.length];
    }

    /**
     * Returns the selected country for the right player.
     *
     * @return Right country
     */
    public Country getRightCountry() {
        return rightCountry;
    }

    /**
     * Cycles the right player's country to the next available option.
     */
    public void cycleRightCountry() {
        Country[] countries = Country.values();
        rightCountry = countries[(rightCountry.ordinal() + 1) % countries.length];
    }

    /**
     * Returns the selected machine AI profile.
     *
     * @return Machine profile
     */
    public MachineProfile getMachineProfile() {
        return machineProfile;
    }

    /**
     * Cycles the machine AI profile to the next available option.
     */
    public void cycleMachineProfile() {
        MachineProfile[] profiles = MachineProfile.values();
        machineProfile = profiles[(machineProfile.ordinal() + 1) % profiles.length];
    }

    /**
     * Returns the selected match rule type.
     *
     * @return Match type
     */
    public MatchType getMatchType() {
        return matchType;
    }

    /**
     * Returns the configured duration in minutes for time-based matches.
     *
     * @return Duration in minutes
     */
    public int getMatchDurationMinutes() {
        return matchDurationMinutes;
    }

    /**
     * Returns the configured goal target for goal-based matches.
     *
     * @return Goal target
     */
    public int getTargetGoals() {
        return targetGoals;
    }

    /**
     * Selects time-based mode and cycles the duration between 1, 2, and 3 minutes.
     */
    public void selectOrCycleTimeMatch() {
        if (matchType == MatchType.TIME) {
            matchDurationMinutes = (matchDurationMinutes % 3) + 1;
        } else {
            matchType = MatchType.TIME;
        }
    }

    /**
     * Selects goal-based mode and cycles the goal target between 3, 5, and 7 goals.
     */
    public void selectOrCycleGoalsMatch() {
        if (matchType == MatchType.GOALS) {
            targetGoals = (targetGoals == 3) ? 5 : (targetGoals == 5 ? 7 : 3);
        } else {
            matchType = MatchType.GOALS;
        }
    }

    /**
     * Builds a validated MatchConfig from the current UI selection values.
     *
     * @return Immutable MatchConfig instance
     * @throws DOPOSoccerException if any configuration field is invalid
     */
    public MatchConfig toConfig() throws DOPOSoccerException {
        int target = (matchType == MatchType.TIME) ? matchDurationMinutes : targetGoals;
        
         MatchConfig.Builder builder = MatchConfig.builder()
                    .modality(this.modality)
                    .leftCountry(this.leftCountry)
                    .rightCountry(this.rightCountry)
                    .matchType(this.matchType)
                    .targetValue(target)
                    .machineProfile(this.machineProfile)
                    .leftPlayerName(this.leftPlayerName);

        if (this.modality == Modality.PVP) {
                builder.rightPlayerName(this.rightPlayerName);
        }
        return builder.build();

    }

    /**
     * Returns the selected window resolution.
     *
     * @return Field resolution
     */
    public FieldResolution getResolution() {
        return resolution;
    }

    /**
     * Sets the window resolution preference.
     *
     * @param resolution Target field resolution
     */
    public void setResolution(FieldResolution resolution) {
        if (resolution != null) {
            this.resolution = resolution;
        }
    }
}
