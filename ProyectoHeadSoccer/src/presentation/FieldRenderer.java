package presentation;

import domain.MatchView;
import domain.Rectangle;
import domain.Side;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Renders the soccer pitch, stadium stands, turf lines, and both goals.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class FieldRenderer {

    /**
     * Draws the pitch background, turf markings, and left/right goals in field coordinates.
     *
     * @param g2d Scaled field graphics context
     * @param match Read-only match view
     */
    public void draw(Graphics2D g2d, MatchView match) {
        if (match == null) return;
        int fieldW = match.getFieldWidth();
        int fieldH = match.getFieldHeight();
        int turfTop = fieldH - 40;
        int bannerTop = turfTop - 12;

        g2d.setColor(new Color(62, 92, 128));
        g2d.fillRect(0, 0, fieldW, bannerTop);

        g2d.setColor(new Color(74, 104, 140));
        for (int row = 20; row < bannerTop - 8; row += 16) {
            g2d.fillRect(0, row, fieldW, 6);
        }

        g2d.setColor(new Color(30, 34, 42));
        g2d.fillRect(0, bannerTop, fieldW, 12);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 6));
        g2d.drawString("DOPOSoccer 2026-2  •  HEAD SOCCER", 16, bannerTop + 8);
        g2d.drawString("ESCUELA COLOMBIANA DE INGENIERÍA", 192, bannerTop + 8);

        g2d.setColor(new Color(46, 139, 87));
        g2d.fillRect(0, turfTop, fieldW, fieldH - turfTop);

        g2d.setColor(new Color(56, 155, 98));
        for (int i = 0; i < fieldW; i += 28) {
            g2d.fillRect(i, turfTop, 14, fieldH - turfTop);
        }

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawLine(0, turfTop, fieldW, turfTop);
        g2d.drawLine(0, fieldH - 1, fieldW, fieldH - 1);
        g2d.drawLine(fieldW / 2, turfTop, fieldW / 2, fieldH);
        g2d.drawOval(fieldW / 2 - 22, turfTop + 6, 44, 28);

        drawGoal(g2d, match, Side.LEFT);
        drawGoal(g2d, match, Side.RIGHT);
    }

    /*
     * Renders the goal net, thin horizontal crossbar, and vertical goalpost for the specified side.
     */
    private void drawGoal(Graphics2D g2d, MatchView match, Side side) {
        Rectangle area = match.getGoalArea(side);
        Rectangle bar = match.getCrossBar(side);

        if (area != null) {
            int rx = (int) area.getX();
            int ry = (int) area.getY();
            int rw = (int) area.getWidth();
            int rh = (int) area.getHeight();

            g2d.setColor(new Color(245, 245, 245, 95));
            g2d.fillRect(rx, ry, rw, rh);
            g2d.setColor(new Color(210, 215, 225, 190));
            g2d.setStroke(new BasicStroke(0.6f));
            for (int x = rx; x <= rx + rw; x += 5) g2d.drawLine(x, ry, x, ry + rh);
            for (int y = ry; y <= ry + rh; y += 6) g2d.drawLine(rx, y, rx + rw, y);

            int postW = 3;
            int frontPostX = (side == Side.LEFT) ? (rx + rw - postW) : rx;
            int backPostX = (side == Side.LEFT) ? rx : (rx + rw - 2);

            g2d.setColor(new Color(220, 225, 230));
            g2d.fillRect(backPostX, ry, 2, rh);

            g2d.setColor(Color.WHITE);
            g2d.fillRect(frontPostX, ry, postW, rh);
            g2d.setColor(new Color(90, 95, 105));
            g2d.setStroke(new BasicStroke(0.7f));
            g2d.drawRect(frontPostX, ry, postW, rh);
        }

        if (bar != null) {
            int bx = (int) bar.getX();
            int by = (int) bar.getY();
            int bw = (int) bar.getWidth();
            int bh = (int) bar.getHeight();

            g2d.setColor(Color.WHITE);
            g2d.fillRect(bx, by, bw, bh);
            g2d.setColor(new Color(90, 95, 105));
            g2d.setStroke(new BasicStroke(0.8f));
            g2d.drawRect(bx, by, bw, bh);
        }
    }
}
