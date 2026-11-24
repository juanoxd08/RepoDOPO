package presentation;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

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

    private Theme() {
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
