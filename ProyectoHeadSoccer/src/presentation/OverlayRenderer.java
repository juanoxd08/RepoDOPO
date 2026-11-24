package presentation;

import domain.GameStatus;
import domain.MatchView;
import domain.PlayerView;
import domain.Side;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Renders match state overlays (pause, goal celebration, sudden death, and match finished).
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class OverlayRenderer {
    private final Rectangle btnSaveGame;
    private final Rectangle btnMenu;
    private final Rectangle btnUpgrades;

    /**
     * Constructs an overlay renderer sharing clickable button bounds with the match panel.
     *
     * @param btnSaveGame Bounds rectangle for the Save Match button on pause
     * @param btnMenu Bounds rectangle for the Return to Menu button on match end
     * @param btnUpgrades Bounds rectangle for the Upgrade Stats button on match end
     */
    public OverlayRenderer(Rectangle btnSaveGame, Rectangle btnMenu, Rectangle btnUpgrades) {
        this.btnSaveGame = btnSaveGame;
        this.btnMenu = btnMenu;
        this.btnUpgrades = btnUpgrades;
    }

    /**
     * Draws the overlay corresponding to the current match status, if applicable.
     *
     * @param g2d Screen-space graphics context
     * @param match Read-only match view
     * @param width Current panel width in pixels
     * @param height Current panel height in pixels
     * @param lastScorerText Banner text for goal celebration
     * @param winnerNotification Banner text for match end
     */
    public void draw(Graphics2D g2d, MatchView match, int width, int height,
                     String lastScorerText, String winnerNotification) {
        if (match == null) return;
        GameStatus status = match.getStatus();
        if (status == GameStatus.PAUSED) {
            drawPauseOverlay(g2d, width, height);
        } else if (status == GameStatus.GOAL_SCORED) {
            drawGoalOverlay(g2d, width, height, lastScorerText);
        } else if (status == GameStatus.SUDDEN_DEATH) {
            drawDrawOverlay(g2d, width, height);
        } else if (status == GameStatus.FINISHED) {
            drawFinishedOverlay(g2d, match, width, height, winnerNotification);
        }
    }

    /*
     * Renders the pause overlay with resume, menu, and save match controls.
     */
    private void drawPauseOverlay(Graphics2D g2d, int w, int h) {
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, w, h);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Impact", Font.BOLD, 54));
        String text = "PAUSE";
        g2d.drawString(text, w / 2 - g2d.getFontMetrics().stringWidth(text) / 2, h / 2 - 30);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        String s1 = "Presiona 'P' para continuar jugando";
        String s2 = "Presiona 'M' para salir al Menú Principal";
        g2d.drawString(s1, w / 2 - g2d.getFontMetrics().stringWidth(s1) / 2, h / 2 + 15);
        g2d.drawString(s2, w / 2 - g2d.getFontMetrics().stringWidth(s2) / 2, h / 2 + 45);

        btnSaveGame.setBounds(w / 2 - 130, h / 2 + 75, 260, 48);
        drawGlossyButton(g2d, btnSaveGame, "GUARDAR PARTIDA", new Color(40, 170, 80), Color.WHITE, 18);
    }

    /*
     * Renders the goal celebration banner overlay.
     */
    private void drawGoalOverlay(Graphics2D g2d, int w, int h, String lastScorerText) {
        g2d.setColor(new Color(20, 140, 20, 180));
        g2d.fillRect(0, h / 2 - 90, w, 180);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Impact", Font.BOLD, 68));
        String goal = "¡¡¡ GOOOOOL !!!";
        g2d.drawString(goal, w / 2 - g2d.getFontMetrics().stringWidth(goal) / 2, h / 2 - 10);

        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.setColor(Color.WHITE);
        String desc = (lastScorerText != null && !lastScorerText.isEmpty()) ? lastScorerText : "¡Anotación!";
        g2d.drawString(desc, w / 2 - g2d.getFontMetrics().stringWidth(desc) / 2, h / 2 + 40);
    }

    /*
     * Renders the sudden-death golden goal banner overlay.
     */
    private void drawDrawOverlay(Graphics2D g2d, int w, int h) {
        g2d.setColor(new Color(200, 120, 20, 190));
        g2d.fillRect(0, h / 2 - 80, w, 160);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Impact", Font.BOLD, 54));
        String title = "¡EMPATE! - GOL DE ORO";
        g2d.drawString(title, w / 2 - g2d.getFontMetrics().stringWidth(title) / 2, h / 2);

        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(Color.WHITE);
        String sub = "Muerte súbita: El primer jugador que marque gana el partido";
        g2d.drawString(sub, w / 2 - g2d.getFontMetrics().stringWidth(sub) / 2, h / 2 + 45);
    }

    /*
     * Renders the match-finished overlay with winner announcement, total points, and action buttons.
     */
    private void drawFinishedOverlay(Graphics2D g2d, MatchView match, int w, int h, String winnerNotification) {
        g2d.setColor(new Color(0, 0, 0, 210));
        g2d.fillRect(0, 0, w, h);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Impact", Font.BOLD, 52));
        String title = "PARTIDO FINALIZADO";
        g2d.drawString(title, w / 2 - g2d.getFontMetrics().stringWidth(title) / 2, h / 2 - 60);

        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(new Color(100, 255, 100));
        String winner = (winnerNotification != null && !winnerNotification.isEmpty()) ? winnerNotification : "Fin del juego";
        g2d.drawString(winner, w / 2 - g2d.getFontMetrics().stringWidth(winner) / 2, h / 2 - 10);

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        String pts = "Recompensa: Ganador +100 puntos | Perdedor +25 puntos";
        g2d.drawString(pts, w / 2 - g2d.getFontMetrics().stringWidth(pts) / 2, h / 2 + 25);

        if (match.getPlayer(Side.LEFT) != null && match.getPlayer(Side.RIGHT) != null) {
            PlayerView p1 = match.getPlayer(Side.LEFT);
            PlayerView p2 = match.getPlayer(Side.RIGHT);
            String totals = p1.getName() + ": " + p1.getPoints() + " pts totales  |  "
                    + p2.getName() + ": " + p2.getPoints() + " pts totales";
            g2d.setFont(new Font("Arial", Font.BOLD, 15));
            g2d.setColor(new Color(255, 225, 120));
            g2d.drawString(totals, w / 2 - g2d.getFontMetrics().stringWidth(totals) / 2, h / 2 + 52);
        }

        btnUpgrades.setBounds(w / 2 - 235, h / 2 + 78, 220, 48);
        drawGlossyButton(g2d, btnUpgrades, "MEJORAR STATS", new Color(70, 150, 220), Color.WHITE, 17);

        btnMenu.setBounds(w / 2 + 15, h / 2 + 78, 220, 48);
        drawGlossyButton(g2d, btnMenu, "VOLVER AL MENÚ", new Color(245, 175, 25), Color.BLACK, 17);
    }

    /*
     * Renders a rounded action button with centered label text.
     */
    private void drawGlossyButton(Graphics2D g2d, Rectangle r, String text, Color bg, Color fg, int fontSize) {
        g2d.setColor(bg);
        g2d.fillRoundRect(r.x, r.y, r.width, r.height, 16, 16);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(r.x, r.y, r.width, r.height, 16, 16);

        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        g2d.setColor(fg);
        FontMetrics fm = g2d.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
        int ty = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, tx, ty);
    }
}
