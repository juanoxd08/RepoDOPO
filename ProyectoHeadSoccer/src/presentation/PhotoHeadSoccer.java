package presentation;

import domain.*;
import javax.swing.JPanel;
import java.awt.*;

/**
 * Graphical canvas panel rendering the HeadSoccer pitch, players, ball, and HUD elements.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class PhotoHeadSoccer extends JPanel {
    private HeadSoccer game;
    private static final double SCALE = 4.8;

    /**
     * Constructs the visual panel configuring scaled dimensions and background color.
     * 
     * @param game HeadSoccer game facade instance
     */
    public PhotoHeadSoccer(HeadSoccer game) {
        this.game = game;
        int screenW = (int) (Field.WIDTH * SCALE);
        int screenH = (int) (Field.HEIGHT * SCALE);
        this.setPreferredSize(new Dimension(screenW, screenH));
        this.setBackground(new Color(135, 206, 235));
    }

    /**
     * Renders all visual match components using anti-aliased graphics.
     * 
     * @param g Graphics painting context
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double scaleX = (double) getWidth() / Field.WIDTH;
        double scaleY = (double) getHeight() / Field.HEIGHT;
        double currentScale = Math.min(scaleX, scaleY);

        Graphics2D gField = (Graphics2D) g2d.create();
        gField.scale(currentScale, currentScale);

        drawField(gField);

        if (game.getPlayer1() != null) drawPlayer(gField, game.getPlayer1());
        if (game.getPlayer2() != null) drawPlayer(gField, game.getPlayer2());
        drawBall(gField);

        gField.dispose();

        drawScoreboard(g2d);

        if (game.getState() == GameStatus.PAUSED) {
            drawOverlay(g2d, "PAUSE", "Press 'P' to continue", new Color(0, 0, 0, 160));
        } else if (!game.getMessage().isEmpty() && game.getState() != GameStatus.PLAYING) {
            drawOverlay(g2d, game.getMessage(), "Press 'R' to restart", new Color(20, 100, 20, 180));
        }
    }

    /**
     * Draws the pitch field, markings, and goal structures.
     * 
     * @param g Graphics context
     */
    public void drawField(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(new Color(100, 120, 140));
        g2d.fillRect(0, 0, Field.WIDTH, 70);

        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRect(0, 70, Field.WIDTH, 12);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 7));
        g2d.drawString("POOBSoccer 2026-2  •  HEAD SOCCER", 20, 79);
        g2d.drawString("POOBSoccer 2026-2  •  HEAD SOCCER", 190, 79);

        g2d.setColor(new Color(46, 139, 87));
        g2d.fillRect(0, 82, Field.WIDTH, Field.HEIGHT - 82);

        g2d.setColor(new Color(56, 155, 98));
        for (int i = 0; i < Field.WIDTH; i += 30) {
            g2d.fillRect(i, 82, 15, Field.HEIGHT - 82);
        }

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawLine(0, Field.HEIGHT - 1, Field.WIDTH, Field.HEIGHT - 1);
        g2d.drawLine(Field.WIDTH / 2, 82, Field.WIDTH / 2, Field.HEIGHT);
        g2d.drawOval(Field.WIDTH / 2 - 25, 120, 50, 40);

        if (game.getField() != null) {
            drawGoal(g2d, game.getField().getLeftGoal());
            drawGoal(g2d, game.getField().getRightGoal());
        }
    }

    /*
     * Draws a goal structure including net meshes and crossbar rectangle.
     */
    private void drawGoal(Graphics2D g2d, Goal goal) {
        if (goal == null) return;
        domain.Rectangle area = goal.getGoalArea();
        if (area != null) {
            int rx = (int) area.getX();
            int ry = (int) area.getY();
            int rw = (int) area.getWidth();
            int rh = (int) area.getHeight();

            g2d.setColor(new Color(240, 240, 240, 170));
            g2d.fillRect(rx, ry, rw, rh);
            g2d.setColor(new Color(160, 160, 160, 200));
            g2d.setStroke(new BasicStroke(0.6f));
            for (int x = rx; x <= rx + rw; x += 5) {
                g2d.drawLine(x, ry, x, ry + rh);
            }
            for (int y = ry; y <= ry + rh; y += 5) {
                g2d.drawLine(rx, y, rx + rw, y);
            }
        }

        domain.Rectangle bar = goal.getCrossBar();
        if (bar != null) {
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawRect((int) bar.getX(), (int) bar.getY(), (int) bar.getWidth(), (int) bar.getHeight());
        }
    }

    /**
     * Draws a player including head, body, foot, stun indicator, and power bar.
     * 
     * @param g Graphics context
     * @param p Player instance to render
     */
    public void drawPlayer(Graphics g, Player p) {
        if (p == null || p.getHead() == null) return;
        Graphics2D g2d = (Graphics2D) g;

        Color countryColor = getColorByCountry(p.getCountry());

        if (p.getBody() != null && p.getBody().getRectangle() != null) {
            domain.Rectangle br = p.getBody().getRectangle();
            int bx = (int) br.getX();
            int by = (int) br.getY();
            int bw = (int) br.getWidth();
            int bh = (int) br.getHeight();
            g2d.setColor(countryColor.darker());
            g2d.fillRoundRect(bx, by, bw, bh, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1f));
            g2d.drawRoundRect(bx, by, bw, bh, 5, 5);
        }

        Circle hc = p.getHead().getCircle();
        int hx = (int) hc.getX();
        int hy = (int) hc.getY();
        int hr = (int) hc.getRadius();
        g2d.setColor(countryColor);
        g2d.fillOval(hx - hr, hy - hr, hr * 2, hr * 2);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawOval(hx - hr, hy - hr, hr * 2, hr * 2);

        g2d.setColor(Color.WHITE);
        int eyeX = (p.getSide() == Side.LEFT) ? hx + hr / 3 : hx - hr / 3;
        g2d.fillOval(eyeX - 3, hy - 3, 6, 7);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(eyeX - (p.getSide() == Side.LEFT ? 1 : 2), hy - 2, 3, 4);

        if (p.getFoot() != null && p.getFoot().getCircle() != null) {
            Circle fc = p.getFoot().getCircle();
            int fx = (int) fc.getX();
            int fy = (int) fc.getY();
            int fr = (int) fc.getRadius();
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillOval(fx - fr, fy - fr, fr * 2, fr * 2);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(fx - fr, fy - fr, fr * 2, fr * 2);
        }

        if (p.getHead().isStunned()) {
            g2d.setColor(new Color(150, 220, 255, 170));
            g2d.fillRoundRect(hx - 22, hy - 22, 44, 58, 12, 12);
            g2d.setColor(new Color(0, 200, 255));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(hx - 22, hy - 22, 44, 58, 12, 12);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 7));
            g2d.drawString("*FROZEN*", hx - 18, hy - 24);
        }

        if (p.getPower() != null) {
            int barW = 24;
            int barH = 3;
            g2d.setColor(Color.BLACK);
            g2d.fillRect(hx - barW / 2 - 1, hy - hr - 7, barW + 2, barH + 2);
            g2d.setColor(p.getPower().isReady() ? Color.YELLOW : Color.ORANGE);
            int percent = Math.max(0, Math.min(100, p.getPower().getCharge()));
            int fill = (int) (barW * (percent / 100.0));
            g2d.fillRect(hx - barW / 2, hy - hr - 6, fill, barH);
        }
    }

    /*
     * Resolves representative kit colors according to player nationality.
     */
    private Color getColorByCountry(Country c) {
        if (c == null) return Color.RED;
        switch (c) {
            case COLOMBIA: return new Color(240, 200, 30);
            case CANADA: return new Color(220, 40, 40);
            case KOREA: return new Color(240, 240, 240);
            case BRAZIL: return new Color(30, 160, 60);
            case ARGENTINA: return new Color(110, 180, 240);
            case CAMEROON: return new Color(20, 130, 70);
            default: return Color.ORANGE;
        }
    }

    /**
     * Draws the soccer ball with aura effects when special power shot is active.
     * 
     * @param g Graphics context
     */
    public void drawBall(Graphics g) {
        Ball ball = game.getBall();
        if (ball == null || ball.getCircle() == null) return;
        Graphics2D g2d = (Graphics2D) g;

        Circle bc = ball.getCircle();
        int bx = (int) bc.getX();
        int by = (int) bc.getY();
        int r = (int) bc.getRadius();

        boolean isPowerShot = (game.getPlayer1() != null && game.getPlayer1().getPower() != null && game.getPlayer1().getPower().isActive()) ||
                              (game.getPlayer2() != null && game.getPlayer2().getPower() != null && game.getPlayer2().getPower().isActive());

        if (isPowerShot) {
            g2d.setColor(new Color(0, 220, 255, 120));
            g2d.fillOval(bx - r - 6, by - r - 6, (r + 6) * 2, (r + 6) * 2);
            g2d.setColor(new Color(100, 240, 255, 200));
            g2d.fillOval(bx - r - 3, by - r - 3, (r + 3) * 2, (r + 3) * 2);
        }

        g2d.setColor(isPowerShot ? new Color(200, 245, 255) : Color.WHITE);
        g2d.fillOval(bx - r, by - r, r * 2, r * 2);
        g2d.setColor(isPowerShot ? new Color(0, 150, 220) : Color.BLACK);
        g2d.setStroke(new BasicStroke(isPowerShot ? 1.8f : 1.2f));
        g2d.drawOval(bx - r, by - r, r * 2, r * 2);
        g2d.drawLine(bx - r + 2, by, bx + r - 2, by);
        g2d.drawLine(bx, by - r + 2, bx, by + r - 2);
    }

    /**
     * Draws the upper scoreboard panel displaying player scores and remaining match time.
     * 
     * @param g Graphics context
     */
    public void drawScoreboard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int screenW = getWidth();

        g2d.setColor(new Color(20, 20, 20, 220));
        g2d.fillRoundRect(screenW / 2 - 160, 12, 320, 56, 12, 12);
        g2d.setColor(new Color(240, 200, 50));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(screenW / 2 - 160, 12, 320, 56, 12, 12);

        int[] points = (game.getScoreBoard() != null) ? game.getScoreBoard().getPoints() : new int[]{0, 0};
        g2d.setFont(new Font("Impact", Font.PLAIN, 32));
        g2d.setColor(Color.WHITE);
        String scoreText = points[0] + "  -  " + points[1];
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(scoreText, screenW / 2 - fm.stringWidth(scoreText) / 2, 48);

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(new Color(255, 120, 120));
        String name1 = game.getPlayer1() != null ? game.getPlayer1().getName() : "P1";
        g2d.drawString(name1, screenW / 2 - 145, 34);

        g2d.setColor(new Color(120, 180, 255));
        String name2 = game.getPlayer2() != null ? game.getPlayer2().getName() : "P2";
        g2d.drawString(name2, screenW / 2 + 75, 34);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.setColor(Color.GREEN);
        int t = (game.getTimer() != null) ? game.getTimer().getTimeRemaining() : 60;
        String tStr = String.format("%02d:%02d", t / 60, t % 60);
        g2d.drawString(tStr, screenW / 2 - g2d.getFontMetrics().stringWidth(tStr) / 2, 62);
    }

    /*
     * Draws a semi-transparent overlay screen with header and subtitle text.
     */
    private void drawOverlay(Graphics2D g2d, String title, String subtitle, Color bg) {
        int w = getWidth();
        int h = getHeight();

        g2d.setColor(bg);
        g2d.fillRect(0, 0, w, h);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Impact", Font.BOLD, 46));
        int tx = w / 2 - g2d.getFontMetrics().stringWidth(title) / 2;
        g2d.drawString(title, tx, h / 2 - 10);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        int sx = w / 2 - g2d.getFontMetrics().stringWidth(subtitle) / 2;
        g2d.drawString(subtitle, sx, h / 2 + 35);
    }
}
