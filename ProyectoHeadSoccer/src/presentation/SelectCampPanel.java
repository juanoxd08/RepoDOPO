package presentation;

import javax.swing.*;
import java.awt.*;

/**
 * Field resolution selection screen built with standard Swing buttons.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class SelectCampPanel extends JPanel {
    private final PhotoHeadSoccer photo;
    private final JButton btnSmall;
    private final JButton btnMedium;
    private final JButton btnLarge;

    /**
     * Constructs the resolution selection screen.
     *
     * @param photo Match panel holding the selected resolution
     * @param onResolutionChanged Callback invoked when a resolution button is clicked
     * @param onBack Action invoked to return to player selection
     * @param onStart Action invoked to launch the configured match
     */
    public SelectCampPanel(
            PhotoHeadSoccer photo,
            java.util.function.Consumer<FieldResolution> onResolutionChanged,
            Runnable onBack,
            Runnable onStart
    ) {
        this.photo = photo;
        setLayout(new GridBagLayout());
        setBackground(new Color(22, 34, 52));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(25, 30, 42, 235));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 200, 50), 2),
                BorderFactory.createEmptyBorder(24, 34, 24, 34)
        ));

        JLabel title = new JLabel("SELECT THE CAMP");
        title.setFont(new Font("Impact", Font.BOLD, 38));
        title.setForeground(Color.YELLOW);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Tamaño de ventana / resolución:");
        subtitle.setFont(new Font("Arial", Font.BOLD, 16));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel resRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        resRow.setOpaque(false);

        btnSmall = createResButton("Pequeño (700×400)");
        btnMedium = createResButton("Mediano (1050×600)");
        btnLarge = createResButton("Grande (1400×800)");

        btnSmall.addActionListener(e -> {
            photo.setResolution(FieldResolution.SMALL);
            onResolutionChanged.accept(FieldResolution.SMALL);
            refreshView();
        });
        btnMedium.addActionListener(e -> {
            photo.setResolution(FieldResolution.MEDIUM);
            onResolutionChanged.accept(FieldResolution.MEDIUM);
            refreshView();
        });
        btnLarge.addActionListener(e -> {
            photo.setResolution(FieldResolution.LARGE);
            onResolutionChanged.accept(FieldResolution.LARGE);
            refreshView();
        });

        resRow.add(btnSmall);
        resRow.add(btnMedium);
        resRow.add(btnLarge);

        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        navRow.setOpaque(false);

        JButton btnBack = new JButton("← VOLVER");
        btnBack.setFont(new Font("Arial", Font.BOLD, 15));
        btnBack.setBackground(new Color(80, 90, 105));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.setPreferredSize(new Dimension(150, 48));
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> onBack.run());

        JButton btnStart = new JButton("⚽ START ⚽");
        btnStart.setFont(new Font("Arial", Font.BOLD, 20));
        btnStart.setBackground(new Color(40, 180, 40));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFocusPainted(false);
        btnStart.setPreferredSize(new Dimension(220, 48));
        btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnStart.addActionListener(e -> onStart.run());

        navRow.add(btnBack);
        navRow.add(btnStart);

        card.add(title);
        card.add(Box.createVerticalStrut(18));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(12));
        card.add(resRow);
        card.add(Box.createVerticalStrut(26));
        card.add(navRow);

        add(card);
        refreshView();
    }

    /**
     * Highlights the currently selected field resolution button.
     */
    public final void refreshView() {
        FieldResolution res = photo.getResolution();
        styleToggle(btnSmall, res == FieldResolution.SMALL);
        styleToggle(btnMedium, res == FieldResolution.MEDIUM);
        styleToggle(btnLarge, res == FieldResolution.LARGE);
    }

    /*
     * Creates a resolution toggle button with uniform typography and hand cursor.
     */
    private JButton createResButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(175, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /*
     * Applies selected or unselected visual styling to a toggle button.
     */
    private void styleToggle(JButton btn, boolean selected) {
        btn.setBackground(selected ? new Color(245, 175, 25) : new Color(55, 62, 76));
        btn.setForeground(selected ? Color.BLACK : Color.WHITE);
    }
}
