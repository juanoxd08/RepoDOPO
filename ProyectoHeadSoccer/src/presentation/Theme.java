package presentation;

import javax.swing.JButton;

import domain.Country;
import domain.PlayerEffect;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map;

/**
 * Shared visual constants and Swing component styling helpers for presentation panels.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public final class Theme {
    public static final Color ACCENT_GOLD = new Color(245, 175, 25);
    public static final Color TOGGLE_UNSELECTED = new Color(55, 62, 76);
    public static final Color ACTION_BLUE = new Color(70, 150, 220);
    private static final Map<Country, Color> COUNTRY_COLORS = new EnumMap<>(Country.class);
    private static final Map<Country, Color> POWER_COLORS = new EnumMap<>(Country.class);
    private static final Map<PlayerEffect, Color> EFFECT_COLORS = new EnumMap<>(PlayerEffect.class);
    private static final Color DEFAULT_POWER_COLOR = new Color(255, 200, 50);

    /*
     * Private constructor to prevent instantiation of this utility class.
     */
    private Theme() {
    }

    static {
        COUNTRY_COLORS.put(Country.SOUTH_KOREA, new Color(230, 60, 60));
        COUNTRY_COLORS.put(Country.CAMEROON, new Color(30, 160, 80));
        COUNTRY_COLORS.put(Country.NIGERIA, new Color(20, 130, 60));
        COUNTRY_COLORS.put(Country.USA, new Color(60, 100, 220));

        POWER_COLORS.put(Country.SOUTH_KOREA, new Color(60, 170, 255));
        POWER_COLORS.put(Country.CAMEROON, new Color(255, 220, 40));
        POWER_COLORS.put(Country.NIGERIA, new Color(50, 210, 110));
        POWER_COLORS.put(Country.USA, new Color(230, 80, 110));

        EFFECT_COLORS.put(PlayerEffect.PUSHED, new Color(80, 190, 255));
        EFFECT_COLORS.put(PlayerEffect.ELECTROCUTED, new Color(255, 230, 50));
        EFFECT_COLORS.put(PlayerEffect.KNOCKED_OUT, new Color(175, 110, 240));
        EFFECT_COLORS.put(PlayerEffect.CONFUSED, new Color(245, 110, 195));
    }

    /**
     * Resolves the base display color associated with a playable country.
     *
     * @param c Playable country
     * @return Country base color
     */
    public static Color countryColor(Country c) {
        if (c == null) return Color.RED;
        return COUNTRY_COLORS.getOrDefault(c, Color.ORANGE);
    }

    /**
     * Resolves the power aura and charging ring color associated with a country.
     *
     * @param c Playable country
     * @return Power accent color
     */
    public static Color powerColor(Country c) {
        if (c == null) return DEFAULT_POWER_COLOR;
        return POWER_COLORS.getOrDefault(c, DEFAULT_POWER_COLOR);
    }

    /**
     * Resolves the indicator color for an active player status effect.
     *
     * @param effect Active status effect
     * @return Effect indicator color, defaulting to white for stun or none
     */
    public static Color effectColor(PlayerEffect effect) {
        if (effect == null) return Color.WHITE;
        return EFFECT_COLORS.getOrDefault(effect, Color.WHITE);
    }

    /**
     * Creates a styled primary button with consistent typography and hand cursor.
     *
     * @param text Button label
     * @param bg Background color
     * @param fg Foreground color
     * @param fontSize Bold Arial font size
     * @return Styled Swing button
     */
    public static JButton createStyledButton(String text, Color bg, Color fg, int fontSize) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, fontSize));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(230, 52));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Applies selected or unselected visual styling to a toggle button.
     *
     * @param btn Target button
     * @param selected Whether the option is currently active
     */
    public static void styleToggle(JButton btn, boolean selected) {
        btn.setBackground(selected ? ACCENT_GOLD : TOGGLE_UNSELECTED);
        btn.setForeground(selected ? Color.BLACK : Color.WHITE);
    }
}
