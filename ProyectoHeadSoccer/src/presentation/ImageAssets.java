package presentation;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import domain.Country;
import persistence.Log;

/**
 * Loads and caches PNG sprites for country flags and the ball once at startup.
 * Missing assets are logged and return null so the renderer falls back to vector shapes.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class ImageAssets {
    private static final int FLAG_WIDTH = 40;
    private static final int FLAG_HEIGHT = 26;
    private static final int BALL_SIZE = 48;

    private final Map<Country, BufferedImage> flags = new EnumMap<>(Country.class);
    private final BufferedImage ballImage;

    /**
     * Loads and pre-scales all country flag and ball sprites from classpath resources.
     */
    public ImageAssets() {
        for (Country c : Country.values()) {
            String key = c.name().toLowerCase();
            BufferedImage flag = load("/presentation/resources/flag_" + key + ".png", FLAG_WIDTH, FLAG_HEIGHT);
            if (flag != null) flags.put(c, flag);
        }
        this.ballImage = load("/presentation/resources/Ball.png", BALL_SIZE, BALL_SIZE);
    }

    /*
     * Loads a PNG sprite from the classpath and pre-scales it to the target dimensions.
     */
    private BufferedImage load(String resourcePath, int targetW, int targetH) {
        URL url = getClass().getResource(resourcePath);
        if (url == null) {
            Log.record(new IOException("Imagen no encontrada: " + resourcePath));
            return null;
        }

        try {
            BufferedImage raw = ImageIO.read(url);
            return (raw != null) ? scale(raw, targetW, targetH) : null;
        } catch (IOException io) {
            Log.record(io);
            return null;
        }
    }

    /*
     * Creates a bilinear-scaled ARGB copy of the source sprite for fast per-frame rendering.
     */
    private BufferedImage scale(BufferedImage src, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(src, 0, 0, width, height, null);
        g2d.dispose();
        return scaled;
    }

    /**
     * Returns the cached flag sprite for the given country, or null if unavailable.
     *
     * @param country Playable country
     * @return Flag sprite or null
     */
    public BufferedImage getFlag(Country country) {
        return (country != null) ? flags.get(country) : null;
    }

    /**
     * Returns the cached ball sprite, or null if unavailable.
     *
     * @return Ball sprite or null
     */
    public BufferedImage getBall() {
        return ballImage;
    }
}
