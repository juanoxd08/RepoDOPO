package domain;

/**
 * Immutable match configuration created via Builder with centralized validation.
 */
public final class MatchConfig {
    private final Modality modality;
    private final Country leftCountry;
    private final Country rightCountry;
    private final MatchType matchType;
    private final int targetValue;

    private MatchConfig(Builder builder) {
        this.modality = builder.modality;
        this.leftCountry = builder.leftCountry;
        this.rightCountry = builder.rightCountry;
        this.matchType = builder.matchType;
        this.targetValue = builder.targetValue;
    }

    public Modality getModality() {
        return modality;
    }

    public Country getLeftCountry() {
        return leftCountry;
    }

    public Country getRightCountry() {
        return rightCountry;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Modality modality = Modality.PVP;
        private Country leftCountry = Country.SOUTH_KOREA;
        private Country rightCountry = Country.CAMEROON;
        private MatchType matchType = MatchType.TIME;
        private int targetValue = 2;

        public Builder modality(Modality m) {
            this.modality = m;
            return this;
        }

        public Builder leftCountry(Country c) {
            this.leftCountry = c;
            return this;
        }

        public Builder rightCountry(Country c) {
            this.rightCountry = c;
            return this;
        }

        public Builder matchType(MatchType mt) {
            this.matchType = mt;
            return this;
        }

        public Builder targetValue(int tv) {
            this.targetValue = tv;
            return this;
        }

        public MatchConfig build() throws DOPOSoccerException {
            if (modality == null || leftCountry == null || rightCountry == null || matchType == null) {
                throw new DOPOSoccerException(DOPOSoccerException.INVALID_MATCH_CONFIG);
            }
            if (modality == Modality.PVC) {
                throw new DOPOSoccerException(DOPOSoccerException.MODALITY_NOT_SUPPORTED);
            }
            if (targetValue <= 0) {
                throw new DOPOSoccerException(DOPOSoccerException.INVALID_MATCH_CONFIG);
            }
            return new MatchConfig(this);
        }
    }
}