package presentation;

import domain.BallView;
import domain.Circle;
import domain.GameStatus;
import domain.MatchView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Renders the soccer ball and its power-shot effects in scaled field coordinates.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class BallRenderer {
    private final ImageAssets assets;
    private double spinAngle;

    /**
     * Constructs a ball renderer using shared image assets.
     *
     * @param assets Loaded image assets
     */
    public BallRenderer(ImageAssets assets) {
        this.assets = assets;
    }

    /**
     * Draws the soccer ball and its power-shot glow when active.
     *
     * @param g2d Scaled field graphics context
     * @param match Read-only match view
     */
    public void draw(Graphics2D g2d, MatchView match) {
        if (match == null) return;
        BallView ball = match.getBall();
        if (ball == null) return;

        for(Circle fake : match.getFakeBalls()){
            drawBallSprite(g2d, (int) fake.getX(), (int) fake.getY(), (int) fake.getRadius(), false);
        }

        

        GameStatus status = match.getStatus();
        if ((status == GameStatus.PLAYING || status == GameStatus.SUDDEN_DEATH) && ball.getRadius() > 0) {
            spinAngle += ball.getHorizontalSpeed() / ball.getRadius();
        }

        int bx = (int) ball.getX();
        int by = (int) ball.getY();
        int r = (int) ball.getRadius();

        boolean isPower = ball.hasPowerShot();

        if (isPower) {
            g2d.setColor(new Color(255, 200, 0, 120));
            g2d.fillOval(bx - r - 6, by - r - 6, (r + 6) * 2, (r + 6) * 2);
        }

        drawChargingRing(g2d, ball, bx, by, r);

        AffineTransform prevTransform = g2d.getTransform();
        g2d.rotate(spinAngle, bx, by);
        drawBallSprite(g2d, bx, by, r, isPower);
        g2d.setTransform(prevTransform);

    }

    /*
     * Draws a shrinking energy ring around the ball while a power shot is charging.
     */
    private void drawChargingRing(Graphics2D g2d, BallView ball, int bx, int by, int r) {
        if (!ball.isPowerShotCharging()) return;
        double progress = Math.max(0.0, Math.min(1.0, ball.getPowerShotChargeProgress()));
        int ringRadius = (int) Math.round(r + 18.0 * (1.0 - progress) + 2.0);
        Color pc = Theme.powerColor(ball.getPowerShotCountry());
        g2d.setColor(new Color(pc.getRed(), pc.getGreen(), pc.getBlue(), 200));
        g2d.setStroke(new BasicStroke(2.2f));
        g2d.drawOval(bx - ringRadius, by - ringRadius, ringRadius * 2, ringRadius * 2);
    }

    /*
     * Draws the ball sprite (or geometric fallback) centered at the given coordinates.
     */
    private void drawBallSprite(Graphics2D g2d, int bx, int by, int r, boolean isPower) {
        BufferedImage ballImg = (assets != null) ? assets.getBall() : null;
        if (ballImg != null) {
            g2d.drawImage(ballImg, bx - r, by - r, r * 2, r * 2, null);
        } else {
            g2d.setColor(isPower ? new Color(255, 240, 200) : Color.WHITE);
            g2d.fillOval(bx - r, by - r, r * 2, r * 2);
            g2d.setColor(isPower ? new Color(255, 120, 0) : Color.BLACK);
            g2d.setStroke(new BasicStroke(isPower ? 1.8f : 1.2f));
            g2d.drawOval(bx - r, by - r, r * 2, r * 2);
            g2d.drawLine(bx - r + 2, by, bx + r - 2, by);
            g2d.drawLine(bx, by - r + 2, bx, by + r - 2);
        }
    }

}
