package presentation;

import domain.Circle;
import domain.PlayerEffect;
import domain.PlayerView;
import domain.Rectangle;
import domain.Side;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Renders a player's limbs, country colors, active power aura, and status effect indicator.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class PlayerRenderer {
    private static final Font EFFECT_FONT = new Font("Arial", Font.BOLD, 8);
    private int animFrame;

    /**
     * Draws the given player in scaled field coordinates.
     *
     * @param g2d Scaled field graphics context
     * @param p Read-only player view
     */
    public void draw(Graphics2D g2d, PlayerView p) {
        if (p == null || p.getHeadShape() == null) return;
        animFrame++;

        Circle hc = p.getHeadShape();
        int hx = (int) hc.getX();
        int hy = (int) hc.getY();
        int hr = (int) hc.getRadius();

        Color baseColor = Theme.countryColor(p.getCountry());

        drawAura(g2d, p, hx, hy, hr);

        Rectangle br = p.getBodyShape();
        if (br != null) {
            g2d.setColor(baseColor.darker());
            g2d.fillRoundRect((int) br.getX(), (int) br.getY(), (int) br.getWidth(), (int) br.getHeight(), 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1f));
            g2d.drawRoundRect((int) br.getX(), (int) br.getY(), (int) br.getWidth(), (int) br.getHeight(), 5, 5);
        }

        g2d.setColor(baseColor);
        g2d.fillOval(hx - hr, hy - hr, hr * 2, hr * 2);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawOval(hx - hr, hy - hr, hr * 2, hr * 2);

        g2d.setColor(Color.WHITE);
        int eyeX = (p.getSide() == Side.LEFT) ? hx + hr / 3 : hx - hr / 3;
        g2d.fillOval(eyeX - 2, hy - 2, 4, 5);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(eyeX - (p.getSide() == Side.LEFT ? 1 : 2), hy - 1, 2, 3);

        Circle fc = p.getFootShape();
        if (fc != null) {
            int fx = (int) fc.getX();
            int fy = (int) fc.getY();
            int fr = (int) fc.getRadius();
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillOval(fx - fr, fy - fr, fr * 2, fr * 2);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(fx - fr, fy - fr, fr * 2, fr * 2);
        }

        drawStatusEffect(g2d, p, hx, hy, hr);
    }

    /*
     * Draws a pulsing country-colored aura around the player while a power is active.
     */
    private void drawAura(Graphics2D g2d, PlayerView p, int hx, int hy, int hr) {
        if (!p.hasAura()) return;
        double pulse = 0.5 + 0.5 * Math.sin(animFrame * 0.25);
        int pad = (int) Math.round(7 + pulse * 5);
        int fillAlpha = (int) Math.round(55 + pulse * 55);
        Color pc = Theme.powerColor(p.getCountry());

        g2d.setColor(new Color(pc.getRed(), pc.getGreen(), pc.getBlue(), fillAlpha));
        g2d.fillOval(hx - hr - pad, hy - hr - pad, (hr + pad) * 2, (hr + pad) * 2);
        g2d.setColor(new Color(pc.getRed(), pc.getGreen(), pc.getBlue(), 190));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval(hx - hr - pad + 2, hy - hr - pad + 2, (hr + pad - 2) * 2, (hr + pad - 2) * 2);
    }

    /*
     * Draws the pulsing head tint, short status symbol, and remaining duration bar for active effects.
     */
    private void drawStatusEffect(Graphics2D g2d, PlayerView p, int hx, int hy, int hr) {
        PlayerEffect effect = p.getActiveEffect();
        String effectName = p.getActiveEffectName();
        boolean hasEffect = p.isStunned()
                || (effect != null && effect != PlayerEffect.NONE)
                || (effectName != null && !effectName.isEmpty());
        if (!hasEffect) return;

        Color ec = Theme.effectColor(effect);

        double pulse = 0.5 + 0.5 * Math.sin(animFrame * 0.2);
        int alpha = (int) (90 + 60 * pulse);
        g2d.setColor(new Color(ec.getRed(), ec.getGreen(), ec.getBlue(), alpha));
        g2d.fillOval(hx - hr, hy - hr, hr * 2, hr * 2);

        String symbol = getEffectSymbol(effect, p.getSide());
        g2d.setFont(EFFECT_FONT);
        int textX = hx - g2d.getFontMetrics().stringWidth(symbol) / 2;
        int textY = hy - hr - 10;
        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.drawString(symbol, textX + 1, textY + 1);
        g2d.setColor(ec);
        g2d.drawString(symbol, textX, textY);

        double progress = Math.max(0.0, Math.min(1.0, p.getEffectProgress()));
        if (progress > 0.0) {
            int barW = 28;
            int barH = 3;
            int barX = hx - barW / 2;
            int barY = hy - hr - 6;
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(barX, barY, barW, barH);
            g2d.setColor(ec);
            g2d.fillRect(barX, barY, (int) Math.round(barW * progress), barH);
        }
    }

    /*
     * Resolves the compact indicator symbol for the active status effect or stun state.
     */
    private String getEffectSymbol(PlayerEffect effect, Side side) {
        if (effect == null || effect == PlayerEffect.NONE) {
            return "***";
        }
        switch (effect) {
            case PUSHED:
                return (side == Side.LEFT) ? "<<" : ">>";
            case ELECTROCUTED:
                return "!!";
            case KNOCKED_OUT:
                return "Z z";
            case CONFUSED:
                return "?";
            default:
                return "***";
        }
    }
}
