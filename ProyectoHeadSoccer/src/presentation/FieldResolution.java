package presentation;

import domain.Field;

/**
 * Enumerates field resolution scale factors available in DOPOSoccer:
 * SMALL (840x480), MEDIUM (1050x600), LARGE (1260x720).
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public enum FieldResolution {
    SMALL("Pequeña (840 x 480)", 2.4),
    MEDIUM("Mediana (1050 x 600)", 3.0),
    LARGE("Grande (1260 x 720)", 3.6);

    private final String label;
    private final double scaleFactor;

    /*
     * Constructs a field resolution option with its display label and scale multiplier.
     */
    FieldResolution(String label, double scaleFactor) {
        this.label = label;
        this.scaleFactor = scaleFactor;
    }

    /**
     * Returns the localized display label for this resolution preset.
     *
     * @return Display label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the scale multiplier relative to logical field coordinates.
     *
     * @return Scale factor
     */
    public double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Computes the scaled canvas width in screen pixels.
     *
     * @return Width in pixels
     */
    public int getPixelWidth() {
        return (int) (Field.WIDTH * scaleFactor);
    }

    /**
     * Computes the scaled canvas height in screen pixels.
     *
     * @return Height in pixels
     */
    public int getPixelHeight() {
        return (int) (Field.HEIGHT * scaleFactor);
    }
}
