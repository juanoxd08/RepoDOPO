package presentation;

import java.awt.*;
import javax.swing.*;

import domain.DOPOSoccer;
import domain.DOPOSoccerException;
import domain.Modality;
import domain.ProfileView;
import domain.StatType;
import persistence.Log;

/**
 * Screen allowing players to spend accumulated points to upgrade their statistics.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class UpgradesPanel extends JPanel {
    private final DOPOSoccer game;
    private final MatchSetup setup;
    private final Runnable onBack;
    private final JLabel title;
    private final JPanel playersContainer;
    private final JButton btnBack;

    /**
     * Constructs the upgrades panel layout and back navigation button.
     *
     * @param game Domain facade instance
     * @param photo Match panel holding configured player names and modality
     * @param onBack Callback executed when the user clicks Back
     */
    public UpgradesPanel(DOPOSoccer game, MatchSetup setup, Runnable onBack) {
        this.game = game;
        this.setup = setup;
        this.onBack = onBack;

        setLayout(new GridBagLayout());
        setBackground(new Color(22, 34, 52));

        this.title = new JLabel("MEJORAR ESTADÍSTICAS");
        this.title.setFont(new Font("Impact", Font.BOLD, 34));
        this.title.setForeground(Color.YELLOW);
        this.title.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.playersContainer = new JPanel(new GridLayout(1, 0, 20, 0));
        this.playersContainer.setOpaque(false);

        this.btnBack = new JButton("← VOLVER");
        this.btnBack.setFont(new Font("Arial", Font.BOLD, 15));
        this.btnBack.setBackground(new Color(80, 90, 105));
        this.btnBack.setForeground(Color.WHITE);
        this.btnBack.setFocusPainted(false);
        this.btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.btnBack.addActionListener(e -> this.onBack.run());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(25, 30, 42, 235));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 200, 50), 2),
                BorderFactory.createEmptyBorder(20, 28, 20, 28)
        ));
        card.add(title);
        card.add(Box.createVerticalStrut(16));
        card.add(playersContainer);
        card.add(Box.createVerticalStrut(20));
        card.add(btnBack);

        add(card);
    }

    /**
     * Rebuilds the player upgrade cards with the latest profiles and points.
     */
    public void refreshView() {
        playersContainer.removeAll();
        playersContainer.add(buildPlayerCard(setup.getLeftPlayerName()));
        if (setup.getModality() == Modality.PVP) {
            playersContainer.add(buildPlayerCard(setup.getRightPlayerName()));
        }
        playersContainer.revalidate();
        playersContainer.repaint();
    }

    /*
     * Builds an upgrade card showing the player's points and stat upgrade controls.
     */
    private JPanel buildPlayerCard(String playerName) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(40, 46, 62));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 255), 2),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        try {
            ProfileView profile = game.getProfile(playerName);
            JLabel lblName = new JLabel(profile.getName());
            lblName.setFont(new Font("Arial", Font.BOLD, 18));
            lblName.setForeground(Color.WHITE);

            JLabel lblPoints = new JLabel("Puntos: " + profile.getPoints());
            lblPoints.setFont(new Font("Arial", Font.BOLD, 15));
            lblPoints.setForeground(new Color(255, 220, 80));

            card.add(lblName);
            card.add(Box.createVerticalStrut(6));
            card.add(lblPoints);
            card.add(Box.createVerticalStrut(12));

            for (StatType type : StatType.values()) {
                JPanel row = new JPanel(new BorderLayout(12, 0));
                row.setOpaque(false);
                JLabel statName = new JLabel(type.name() + ": " + profile.getStat(type) + "/10");
                statName.setForeground(Color.WHITE);
                statName.setFont(new Font("Arial", Font.PLAIN, 14));
                int cost = profile.upgradeCost(type);
                JButton upgrade = new JButton((cost > 0) ? "+ (" + cost + " pts)" : "MÁX");
                upgrade.setFocusPainted(false);
                if (cost == 0) {
                    upgrade.setEnabled(false);
                } else {
                    upgrade.addActionListener(e -> handleUpgrade(playerName, type));
                }
                row.add(statName, BorderLayout.WEST);
                row.add(upgrade, BorderLayout.EAST);
                card.add(row);
                card.add(Box.createVerticalStrut(8));
            }
        } catch (DOPOSoccerException e) {
            Log.record(e);
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error de perfil", JOptionPane.ERROR_MESSAGE);
        }

        return card;
    }

    /*
     * Requests a stat upgrade through the domain facade and refreshes the panel on success.
     */
    private void handleUpgrade(String playerName, StatType stat) {
        try {
            game.upgrade(playerName, stat);
            refreshView();
        } catch (DOPOSoccerException ex) {
            Log.record(ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Mejora", JOptionPane.WARNING_MESSAGE);
        }
    }
}
