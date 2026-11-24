package presentation;

import domain.Country;
import domain.MachineProfile;
import domain.MatchType;
import domain.Modality;

import javax.swing.*;
import java.awt.*;

/**
 * Match and player configuration screen built with Swing buttons and selectors.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class SelectPlayersPanel extends JPanel {
    private final MatchSetup setup;

    private final JButton btnPvP;
    private final JButton btnPvM;
    private final JLabel lblProfileTitle;
    private final JButton btnNextProfile;

    private final JLabel lblP1Country;
    private final JLabel lblP1Power;
    private final JLabel lblP2Title;
    private final JLabel lblP2Country;
    private final JLabel lblP2Power;

    private final JButton btnTypeTime;
    private final JButton btnTypeGoals;

    private final JTextField txtP1Name;
    private final JTextField txtP2Name;


    /**
     * Constructs the player selection screen backed by the configuration state in {@link PhotoHeadSoccer}.
     *
     * @param photo Match panel holding the selected match options
     * @param onBack Action invoked to return to the main menu
     * @param onContinue Action invoked to proceed to field resolution selection
     */
    public SelectPlayersPanel(MatchSetup setup, Runnable onBack, Runnable onContinue, Runnable onUpgrades) {
        this.setup = setup;
        setLayout(new GridBagLayout());
        setBackground(new Color(22, 34, 52));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(25, 30, 42, 235));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 200, 50), 2),
                BorderFactory.createEmptyBorder(20, 28, 20, 28)
        ));

        JLabel title = new JLabel("SELECT PLAYERS");
        title.setFont(new Font("Impact", Font.BOLD, 38));
        title.setForeground(Color.YELLOW);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        modeRow.setOpaque(false);
        JLabel lblMode = createLabel("Modalidad:", 15, true, Color.WHITE);
        btnPvP = createOptionButton("Jugador vs Jugador");
        btnPvM = createOptionButton("Jugador vs Máquina");
        lblProfileTitle = createLabel("Perfil IA:", 15, true, Color.LIGHT_GRAY);
        btnNextProfile = createOptionButton("");

        btnPvP.addActionListener(e -> {
            setup.setModality(Modality.PVP);
            refreshView();
        });
        btnPvM.addActionListener(e -> {
            setup.setModality(Modality.PVC);
            refreshView();
        });
        btnNextProfile.addActionListener(e -> {
            setup.cycleMachineProfile();
            refreshView();
        });

        modeRow.add(lblMode);
        modeRow.add(btnPvP);
        modeRow.add(btnPvM);
        modeRow.add(lblProfileTitle);
        modeRow.add(btnNextProfile);

        JPanel playersRow = new JPanel(new GridLayout(1, 2, 20, 0));
        playersRow.setOpaque(false);

        JPanel p1Box = createPlayerCard(new Color(255, 100, 100));
        JLabel lblP1Title = createLabel("JUGADOR 1 (Izquierda)", 16, true, Color.WHITE);
        lblP1Country = createLabel("", 14, false, Color.WHITE);
        lblP1Power = createLabel("", 14, false, new Color(255, 220, 120));
        JButton btnP1Country = createActionButton("Cambiar Personaje ↻", new Color(70, 130, 180), Color.WHITE);
        btnP1Country.addActionListener(e -> {
            setup.cycleLeftCountry();
            refreshView();
        });
        p1Box.add(lblP1Title);
        txtP1Name = new JTextField(setup.getLeftPlayerName(), 14);
        txtP1Name.setMaximumSize(new Dimension(220, 28));
        p1Box.add(Box.createVerticalStrut(8));
        p1Box.add(txtP1Name);
        p1Box.add(Box.createVerticalStrut(10));
        p1Box.add(lblP1Country);
        p1Box.add(Box.createVerticalStrut(6));
        p1Box.add(lblP1Power);
        p1Box.add(Box.createVerticalStrut(14));
        p1Box.add(btnP1Country);

        JPanel p2Box = createPlayerCard(new Color(100, 150, 255));
        lblP2Title = createLabel("JUGADOR 2 (Derecha)", 16, true, Color.WHITE);
        lblP2Country = createLabel("", 14, false, Color.WHITE);
        lblP2Power = createLabel("", 14, false, new Color(255, 220, 120));
        JButton btnP2Country = createActionButton("Cambiar Personaje ↻", new Color(70, 130, 180), Color.WHITE);
        btnP2Country.addActionListener(e -> {
            setup.cycleRightCountry();
            refreshView();
        });
        p2Box.add(lblP2Title);
        txtP2Name = new JTextField(setup.getRightPlayerName(), 14);
        txtP2Name.setMaximumSize(new Dimension(220, 28));
        p2Box.add(Box.createVerticalStrut(8));
        p2Box.add(txtP2Name);
        p2Box.add(Box.createVerticalStrut(10));
        p2Box.add(lblP2Country);
        p2Box.add(Box.createVerticalStrut(6));
        p2Box.add(lblP2Power);
        p2Box.add(Box.createVerticalStrut(14));
        p2Box.add(btnP2Country);

        playersRow.add(p1Box);
        playersRow.add(p2Box);

        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 4));
        typeRow.setOpaque(false);
        JLabel lblType = createLabel("Tipo de Partido:", 15, true, Color.WHITE);
        btnTypeTime = createOptionButton("");
        btnTypeGoals = createOptionButton("");

        btnTypeTime.addActionListener(e -> {
            setup.selectOrCycleTimeMatch();
            refreshView();
        });
        btnTypeGoals.addActionListener(e -> {
            setup.selectOrCycleGoalsMatch();
            refreshView();
        });

        typeRow.add(lblType);
        typeRow.add(btnTypeTime);
        typeRow.add(btnTypeGoals);

        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        navRow.setOpaque(false);
        JButton btnBack = createActionButton("← MENÚ", new Color(80, 90, 105), Color.WHITE);
        btnBack.setPreferredSize(new Dimension(150, 44));
        btnBack.addActionListener(e -> onBack.run());

        JButton btnContinue = createActionButton("CONTINUE ➔", new Color(245, 175, 25), Color.BLACK);
        btnContinue.setPreferredSize(new Dimension(210, 44));
        btnContinue.setFont(new Font("Arial", Font.BOLD, 18));
        btnContinue.addActionListener(e -> {
            savePlayerNames();
            onContinue.run();
        });

        JButton btnStats = createActionButton("MEJORAR STATS", new Color(70, 150, 220), Color.WHITE);
        btnStats.setPreferredSize(new Dimension(170, 44));
        btnStats.addActionListener(e -> {
            savePlayerNames();
            onUpgrades.run();
        });

        navRow.add(btnBack);
        navRow.add(btnContinue);
        navRow.add(btnStats);

        card.add(title);
        card.add(Box.createVerticalStrut(14));
        card.add(modeRow);
        card.add(Box.createVerticalStrut(14));
        card.add(playersRow);
        card.add(Box.createVerticalStrut(16));
        card.add(typeRow);
        card.add(Box.createVerticalStrut(20));
        card.add(navRow);

        add(card);
        refreshView();
    }

    /**
     * Synchronizes button states and labels with the current configuration in {@link PhotoHeadSoccer}.
     */
    public final void refreshView() {
        boolean vsMachine = setup.getModality() == Modality.PVC;
        Theme.styleToggle(btnPvP, !vsMachine);
        Theme.styleToggle(btnPvM, vsMachine);

        lblProfileTitle.setVisible(vsMachine);
        btnNextProfile.setVisible(vsMachine);
        MachineProfile profile = setup.getMachineProfile();
        btnNextProfile.setText(profile.getDisplayName() + " ↻");
        Theme.styleToggle(btnNextProfile, true);

        Country left = setup.getLeftCountry();
        Country right = setup.getRightCountry();
        lblP1Country.setText("País: " + left.getDisplayName());
        lblP1Power.setText("Poder: " + left.getPowerName());

        lblP2Title.setText(vsMachine ? "MÁQUINA (Derecha)" : "JUGADOR 2 (Derecha)");
        txtP2Name.setEnabled(!vsMachine);
        txtP2Name.setText(vsMachine ? ("COM " + profile.getDisplayName()) : setup.getRightPlayerName());
        lblP2Country.setText("País: " + right.getDisplayName());
        lblP2Power.setText("Poder: " + right.getPowerName());

        btnTypeTime.setText("Por Tiempo (" + setup.getMatchDurationMinutes() + " min) ↻");
        btnTypeGoals.setText("Por Goles (" + setup.getTargetGoals() + " goles) ↻");
        Theme.styleToggle(btnTypeTime, setup.getMatchType() == MatchType.TIME);
        Theme.styleToggle(btnTypeGoals, setup.getMatchType() == MatchType.GOALS);
    }

    /*
     * Builds a styled vertical card container for player configuration fields.
     */
    private JPanel createPlayerCard(Color borderColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 46, 62));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 2),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        return panel;
    }

    /*
     * Creates a styled text label with the given font size, weight, and color.
     */
    private JLabel createLabel(String text, int size, boolean bold, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, size));
        lbl.setForeground(color);
        return lbl;
    }

    /*
     * Creates a toggleable configuration option button.
     */
    private JButton createOptionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /*
     * Creates a colored action button for navigation or screen transitions.
     */
    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /*
     * Copies non-blank player names from the text fields into the shared configuration holder.
     */
    private void savePlayerNames() {
        String n1 = txtP1Name.getText().trim();
        if (!n1.isEmpty()) setup.setLeftPlayerName(n1);
        if (setup.getModality() == Modality.PVP) {
            String n2 = txtP2Name.getText().trim();
            if (!n2.isEmpty()) setup.setRightPlayerName(n2);
        }
    }
}
