package presentation;

import domain.MatchView;
import domain.Modality;
import domain.PlayerView;
import domain.Side;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Renders the top HUD including scoreboard, timer or goal target, and player bars.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class HudRenderer {
    private final ImageAssets assets;

    /**
     * Constructs a HUD renderer using shared image assets for country flags.
     *
     * @param assets Loaded image assets
     */
    public HudRenderer(ImageAssets assets) {
        this.assets = assets;
    }

    /**
     * Draws the scoreboard, match mode/timer, and both player HUD panels.
     *
     * @param g2d Screen-space graphics context
     * @param match Read-only match view
     * @param setup Match configuration holding goal target and machine profile
     * @param panelWidth Current width of the match canvas in pixels
     */
    public void draw(Graphics2D g2d, MatchView match, MatchSetup setup, int panelWidth) {
        if (match == null) return;

        g2d.setColor(new Color(15, 20, 25, 230));
        g2d.fillRoundRect(panelWidth / 2 - 160, 10, 320, 74, 14, 14);
        g2d.setColor(new Color(245, 195, 35));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(panelWidth / 2 - 160, 10, 320, 74, 14, 14);

        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(new Color(180, 220, 255));
        String modeTag = (match.getModality() == Modality.PVC)
                ? "PvM • " + setup.getMachineProfile().getDisplayName()
                : "PvP";
        g2d.drawString(modeTag, panelWidth / 2 - g2d.getFontMetrics().stringWidth(modeTag) / 2, 23);

        int leftGoals = match.getGoals(Side.LEFT);
        int rightGoals = match.getGoals(Side.RIGHT);
        g2d.setFont(new Font("Impact", Font.PLAIN, 36));
        g2d.setColor(Color.WHITE);
        String scoreText = leftGoals + "  -  " + rightGoals;
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(scoreText, panelWidth / 2 - fm.stringWidth(scoreText) / 2, 48);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.setColor(Color.GREEN);
        String centerInfo;
        if (match.isSuddenDeath()) {
            centerInfo = "GOL DE ORO";
            g2d.setColor(Color.YELLOW);
        } else if (match.getRemainingSeconds() < 0) {
            centerInfo = "META: " + setup.getTargetGoals() + " GOLES";
        } else {
            int sec = match.getRemainingSeconds();
            centerInfo = String.format("%02d:%02d", sec / 60, sec % 60);
        }
        g2d.drawString(centerInfo, panelWidth / 2 - g2d.getFontMetrics().stringWidth(centerInfo) / 2, 68);

        PlayerView p1 = match.getPlayer(Side.LEFT);
        if (p1 != null) {
            drawPlayerHUD(g2d, p1, 25, 14, 220);
        }

        PlayerView p2 = match.getPlayer(Side.RIGHT);
        if (p2 != null) {
            drawPlayerHUD(g2d, p2, panelWidth - 245, 14, 220);
        }
    }

    /*
     * Renders a single player's name, country flag, health bar, and power gauge in the HUD.
     */
    private void drawPlayerHUD(Graphics2D g2d, PlayerView p, int x, int y, int barW) {
        int textX = x;
        BufferedImage flagImg = (assets != null) ? assets.getFlag(p.getCountry()) : null;
        if (flagImg != null) {
            g2d.drawImage(flagImg, x, y + 1, 20, 13, null);
            textX = x + 25;
        }
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        g2d.setColor(Color.WHITE);
        String name = p.getName() + " (" + p.getCountry().getDisplayName() + ")";
        g2d.drawString(name, textX, y + 12);

        int hpY = y + 18;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x, hpY, barW, 14);

        double hpRatio = Math.max(0.0, Math.min(1.0, p.getLife() / p.getMaxLife()));
        g2d.setColor(healthColor(hpRatio));
        g2d.fillRect(x + 1, hpY + 1, (int) ((barW - 2) * hpRatio), 12);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, hpY, barW, 14);

        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        String hpText = (int) p.getLife() + "/" + (int) p.getMaxLife() + " HP";
        g2d.drawString(hpText, x + barW / 2 - g2d.getFontMetrics().stringWidth(hpText) / 2, hpY + 11);

        int powY = hpY + 16;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x, powY, barW, 10);

        double powRatio = Math.max(0.0, Math.min(1.0, p.getPowerProgress() / 100.0));
        g2d.setColor(powRatio >= 1.0 ? Color.YELLOW : new Color(240, 140, 0));
        g2d.fillRect(x + 1, powY + 1, (int) ((barW - 2) * powRatio), 8);
        g2d.setColor(Color.GRAY);
        g2d.drawRect(x, powY, barW, 10);

        if (p.hasAura()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 9));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("⚡ AURA ACTIVA ⚡", x + barW / 2 - 40, powY + 9);
        }
    }

    /*
     * Chooses the health bar color: green above half life, orange above a quarter, red below.
     */
    private Color healthColor(double hpRatio) {
        if (hpRatio > 0.5) {
            return new Color(40, 200, 40);
        }
        if (hpRatio > 0.25) {
            return Color.ORANGE;
        }
        return Color.RED;
    }
}
