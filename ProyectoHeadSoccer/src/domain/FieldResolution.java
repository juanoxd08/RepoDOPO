package domain;

/**
 * Enumerates field resolution scale factors available in DOPOSoccer:
 * SMALL (840x480), MEDIUM (1050x600), LARGE (1260x720).
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public enum FieldResolution {
    SMALL("Pequeña (840 x 480)", 2.4),
    MEDIUM("Mediana (1050 x 600)", 3.0),
    LARGE("Grande (1260 x 720)", 3.6);

    private final String label;
    private final double scaleFactor;

    FieldResolution(String label, double scaleFactor) {
        this.label = label;
        this.scaleFactor = scaleFactor;
    }

    public String getLabel() {
        return label;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public int getPixelWidth() {
        return (int) (Field.WIDTH * scaleFactor);
    }

    public int getPixelHeight() {
        return (int) (Field.HEIGHT * scaleFactor);
    }
}
