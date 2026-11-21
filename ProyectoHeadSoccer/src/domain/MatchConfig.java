package domain;

/**
 * Immutable match configuration created via Builder with centralized validation.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public final class MatchConfig {
    private final Modality modality;
    private final Country leftCountry;
    private final Country rightCountry;
    private final MatchType matchType;
    private final int targetValue;
    private final MachineProfile machineProfile;
    private final String leftPlayerName;
    private final String rightPlayerName;

    private MatchConfig(Builder builder) {
        this.modality = builder.modality;
        this.leftCountry = builder.leftCountry;
        this.rightCountry = builder.rightCountry;
        this.matchType = builder.matchType;
        this.targetValue = builder.targetValue;
        this.machineProfile = builder.machineProfile;
        this.leftPlayerName = builder.leftPlayerName;
        this.rightPlayerName = builder.rightPlayerName;
    }

    /**
     * Returns the configured match modality.
     *
     * @return Match modality
     */
    public Modality getModality() {
        return modality;
    }

    /**
     * Returns the country selected for the left player.
     *
     * @return Left player country
     */
    public Country getLeftCountry() {
        return leftCountry;
    }

    /**
     * Returns the country selected for the right player.
     *
     * @return Right player country
     */
    public Country getRightCountry() {
        return rightCountry;
    }

    /**
     * Returns the match termination rule type.
     *
     * @return Match type
     */
    public MatchType getMatchType() {
        return matchType;
    }

    /**
     * Returns the target minutes or goals depending on the match type.
     *
     * @return Target value
     */
    public int getTargetValue() {
        return targetValue;
    }

    /**
     * Returns the configured machine AI profile for PVC matches.
     *
     * @return Machine profile
     */
    public MachineProfile getMachineProfile() {
        return machineProfile;
    }

    /**
     * Returns the left player name.
     *
     * @return Left player name
     */
    public String getLeftPlayerName() {
        return leftPlayerName;
    }

    /**
     * Returns the right player name.
     *
     * @return Right player name
     */
    public String getRightPlayerName() {
        return rightPlayerName;
    }

    /**
     * Creates a new builder for MatchConfig.
     *
     * @return MatchConfig builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for constructing validated MatchConfig instances.
     *
     * @author Juan Espitia
     * @author Cristian Salamanca
     */
    public static final class Builder {
        private Modality modality = Modality.PVP;
        private Country leftCountry = Country.SOUTH_KOREA;
        private Country rightCountry = Country.CAMEROON;
        private MatchType matchType = MatchType.TIME;
        private int targetValue = 2;
        private MachineProfile machineProfile = MachineProfile.EXPERTA;
        private String leftPlayerName = "jugador 1";
        private String rightPlayerName = "jugador 2";

        /**
         * Sets the match modality.
         *
         * @param m Modality
         * @return This builder
         */
        public Builder modality(Modality m) {
            this.modality = m;
            return this;
        }

        /**
         * Sets the left player country.
         *
         * @param c Country
         * @return This builder
         */
        public Builder leftCountry(Country c) {
            this.leftCountry = c;
            return this;
        }

        /**
         * Sets the right player country.
         *
         * @param c Country
         * @return This builder
         */
        public Builder rightCountry(Country c) {
            this.rightCountry = c;
            return this;
        }

        /**
         * Sets the match rule type.
         *
         * @param mt Match type
         * @return This builder
         */
        public Builder matchType(MatchType mt) {
            this.matchType = mt;
            return this;
        }

        /**
         * Sets the target minutes or goals.
         *
         * @param tv Positive target value
         * @return This builder
         */
        public Builder targetValue(int tv) {
            this.targetValue = tv;
            return this;
        }

        /**
         * Sets the machine AI profile for PVC matches.
         *
         * @param mp Machine profile
         * @return This builder
         */
        public Builder machineProfile(MachineProfile mp) {
            this.machineProfile = mp;
            return this;
        }

        /**
         * Sets the left player name.
         *
         * @param n Left player name
         * @return This builder
         */
        public Builder leftPlayerName(String n) {
            this.leftPlayerName = n;
            return this;
        }

        /**
         * Sets the right player name.
         *
         * @param n Right player name
         * @return This builder
         */
        public Builder rightPlayerName(String n) {
            this.rightPlayerName = n;
            return this;
        }

        /**
         * Validates fields and builds an immutable MatchConfig.
         *
         * @return Validated MatchConfig instance
         * @throws DOPOSoccerException if any configuration value is invalid or unsupported
         */
        public MatchConfig build() throws DOPOSoccerException {
            if (modality == null || leftCountry == null || rightCountry == null || matchType == null) {
                throw new DOPOSoccerException(DOPOSoccerException.INVALID_MATCH_CONFIG);
            }
            if (modality == Modality.PVC && machineProfile == null) {
                throw new DOPOSoccerException(DOPOSoccerException.INVALID_MATCH_CONFIG);
            }
            if (targetValue <= 0) {
                throw new DOPOSoccerException(DOPOSoccerException.INVALID_MATCH_CONFIG);
            }
            if (leftPlayerName == null || leftPlayerName.trim().isEmpty()
                    || rightPlayerName == null || rightPlayerName.trim().isEmpty()) {
                throw new DOPOSoccerException(DOPOSoccerException.INVALID_MATCH_CONFIG);
            }
            if (modality == Modality.PVC && this.rightPlayerName.equals("jugador 2")) {
                this.rightPlayerName = "COM " + machineProfile.getDisplayName();
            }
            return new MatchConfig(this);
        }
    }
}