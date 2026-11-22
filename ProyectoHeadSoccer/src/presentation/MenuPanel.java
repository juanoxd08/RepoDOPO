package presentation;

import javax.swing.*;
import java.awt.*;

/**
 * Main menu screen built with standard Swing components.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class MenuPanel extends JPanel {

    /**
     * Constructs the main menu panel with New Game and Open Match actions.
     *
     * @param onNewGame Action invoked when the user clicks New Game
     * @param onOpenMatch Action invoked when the user clicks Open Match
     */
    public MenuPanel(Runnable onNewGame, Runnable onOpenMatch) {
        setLayout(new GridBagLayout());
        setBackground(new Color(22, 34, 52));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("HEAD SOCCER GAME");
        title.setFont(new Font("Impact", Font.BOLD, 48));
        title.setForeground(new Color(255, 220, 40));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("DOPOSoccer 2026-2  •  Proyecto Final POOB");
        subtitle.setFont(new Font("Arial", Font.BOLD, 16));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        buttonRow.setOpaque(false);

        JButton btnNewGame = createStyledButton("NEW GAME", new Color(245, 175, 25), Color.BLACK, 20);
        btnNewGame.addActionListener(e -> onNewGame.run());

        JButton btnOpen = createStyledButton("ABRIR PARTIDA", new Color(70, 150, 220), Color.WHITE, 18);
        btnOpen.addActionListener(e -> onOpenMatch.run());

        buttonRow.add(btnNewGame);
        buttonRow.add(btnOpen);

        JPanel controlsBox = new JPanel();
        controlsBox.setLayout(new BoxLayout(controlsBox, BoxLayout.Y_AXIS));
        controlsBox.setBackground(new Color(12, 18, 28, 210));
        controlsBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(245, 195, 35), 2),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)
        ));
        controlsBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel ctrlTitle = new JLabel("CONTROLES DE JUEGO (Mismo Teclado):");
        ctrlTitle.setFont(new Font("Arial", Font.BOLD, 15));
        ctrlTitle.setForeground(Color.YELLOW);

        JLabel p1Ctrl = new JLabel("• Jugador 1: A / D (Mover), W (Saltar), S (Patear), Q (Activar Poder)");
        p1Ctrl.setFont(new Font("Arial", Font.PLAIN, 14));
        p1Ctrl.setForeground(Color.WHITE);

        JLabel p2Ctrl = new JLabel("• Jugador 2: ← / → (Mover), ↑ (Saltar), ↓ (Patear), Ctrl Der (Activar Poder)");
        p2Ctrl.setFont(new Font("Arial", Font.PLAIN, 14));
        p2Ctrl.setForeground(Color.WHITE);

        controlsBox.add(ctrlTitle);
        controlsBox.add(Box.createVerticalStrut(8));
        controlsBox.add(p1Ctrl);
        controlsBox.add(Box.createVerticalStrut(6));
        controlsBox.add(p2Ctrl);

        content.add(title);
        content.add(Box.createVerticalStrut(10));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(36));
        content.add(buttonRow);
        content.add(Box.createVerticalStrut(32));
        content.add(controlsBox);

        add(content);
    }

    /*
     * Creates a styled primary menu button with consistent typography and hand cursor.
     */
    private JButton createStyledButton(String text, Color bg, Color fg, int fontSize) {
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
     * Paints a stadium-style backdrop behind the Swing menu controls.
     *
     * @param g Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        g.setColor(new Color(45, 72, 105));
        g.fillRect(0, 0, w, (int) (h * 0.72));
        g.setColor(new Color(30, 34, 40));
        g.fillRect(0, (int) (h * 0.72), w, (int) (h * 0.04));
        g.setColor(new Color(38, 120, 74));
        g.fillRect(0, (int) (h * 0.76), w, h - (int) (h * 0.76));
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, w, h);
    }
}
