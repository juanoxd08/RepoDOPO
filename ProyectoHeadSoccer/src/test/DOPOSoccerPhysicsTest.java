package test;

import domain.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Physics simulation unit tests verifying kick sweep, shot direction,
 * solid crossbar collision resolution, and stable support behavior.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class DOPOSoccerPhysicsTest {

    @Test
    public void testKickHitsStationaryBallOnGroundForwardAndUpward() {
        Player player = new Player("Left", Country.SOUTH_KOREA, Side.LEFT, new AuraAzulPower(), new PlayerStats());
        double footRestX = player.getFootShape().getX();
        Ball ball = new Ball(footRestX + 20.0, Field.HEIGHT - 10.0, 10.0);

        player.kick();
        for (int i = 0; i < Foot.KICK_SWEEP_TICKS; i++) {
            player.tick();
            player.collideWithBall(ball);
            if (ball.getHorizontalSpeed() > 5.0) {
                break;
            }
        }

        assertTrue("Ball should move toward opponent goal (vx > 0)", ball.getHorizontalSpeed() > 5.0);
        assertTrue("Ball should lift upward (vy < 0)", ball.getVerticalSpeed() < 0.0);
    }

    @Test
    public void testKickHitsRollingBallInAtLeast25Of30StartOffsets() {
        int shots = 0;
        for (int offset = 0; offset < 30; offset++) {
            Player player = new Player("Left", Country.SOUTH_KOREA, Side.LEFT, new AuraAzulPower(), new PlayerStats());
            double footRestX = player.getFootShape().getX();
            Ball ball = new Ball(footRestX + 10.0 + offset * 1.2, Field.HEIGHT - 10.0, 10.0);
            ball.setHorizontalSpeed(-3.0);

            player.kick();
            for (int t = 0; t < Foot.KICK_SWEEP_TICKS + Foot.KICK_HOLD_TICKS; t++) {
                player.tick();
                ball.tick();
                ball.collideWithEdges();
                player.collideWithBall(ball);
                if (ball.getHorizontalSpeed() > 5.0) {
                    shots++;
                    break;
                }
            }
        }

        assertTrue("Expected at least 25 of 30 shots, got " + shots, shots >= 25);
    }

    @Test
    public void testKickWithBallBehindFootDoesNotFireTowardOwnGoal() {
        Player player = new Player("Left", Country.SOUTH_KOREA, Side.LEFT, new AuraAzulPower(), new PlayerStats());
        player.translate(100.0, 0.0);
        double footRestX = player.getFootShape().getX();
        Ball ball = new Ball(footRestX - 24.0, Field.HEIGHT - 10.0, 10.0);

        player.kick();
        for (int i = 0; i < Foot.KICK_SWEEP_TICKS; i++) {
            player.tick();
            player.collideWithBall(ball);
        }

        assertTrue("Ball behind foot should not be launched toward own goal at kick speed",
                ball.getHorizontalSpeed() > -5.0);
    }

    @Test
    public void testJumpingUnderCrossbarDoesNotTrapHeadInside() {
        Player player = new Player("Left", Country.SOUTH_KOREA, Side.LEFT, new AuraAzulPower(), new PlayerStats());
        Goal leftGoal = new Goal(Side.LEFT);
        Rectangle bar = leftGoal.getCrossBar();

        double barCenterX = (bar.left() + bar.right()) / 2.0;
        player.translate(barCenterX - player.getHeadShape().getX(), 0.0);
        player.jump();

        int consecutiveInsideTicks = 0;
        int maxConsecutiveInside = 0;

        for (int t = 0; t < 120; t++) {
            player.clearSupport();
            player.tick();
            player.collideWithEdges();
            player.collideWithGoal(leftGoal);

            if (bar.detectCollisionWithCircle(player.getHeadShape())) {
                consecutiveInsideTicks++;
                maxConsecutiveInside = Math.max(maxConsecutiveInside, consecutiveInsideTicks);
            } else {
                consecutiveInsideTicks = 0;
            }
        }

        assertTrue("Head stayed inside crossbar for " + maxConsecutiveInside + " consecutive ticks",
                maxConsecutiveInside <= 2);
    }

    @Test
    public void testStandingOnCrossbarKeepsFootOnTopSurface() {
        Player player = new Player("Left", Country.SOUTH_KOREA, Side.LEFT, new AuraAzulPower(), new PlayerStats());
        Goal leftGoal = new Goal(Side.LEFT);
        Rectangle bar = leftGoal.getCrossBar();

        double barCenterX = (bar.left() + bar.right()) / 2.0;
        double footBottom = player.getFootShape().getY() + player.getFootShape().getRadius();
        player.translate(barCenterX - player.getFootShape().getX(), bar.top() - footBottom - 10.0);

        for (int t = 0; t < 30; t++) {
            player.clearSupport();
            player.tick();
            player.collideWithGoal(leftGoal);
        }

        double settledFootBottom = player.getFootShape().getY() + player.getFootShape().getRadius();
        assertEquals("Foot bottom should rest on crossbar top", bar.top(), settledFootBottom, 1.0);
    }

    @Test
    public void testWalkingIntoCrossbarFromSideNeverOverlapsHead() {
        Player player = new Player("Right", Country.CAMEROON, Side.RIGHT, new LightningPower(), new PlayerStats());
        Goal leftGoal = new Goal(Side.LEFT);
        Rectangle bar = leftGoal.getCrossBar();

        double barMidY = (bar.top() + bar.bottom()) / 2.0;
        player.translate((bar.right() + 40.0) - player.getHeadShape().getX(), barMidY - player.getHeadShape().getY());

        for (int t = 0; t < 60; t++) {
            player.clearSupport();
            player.moveLeft();
            player.collideWithEdges();
            player.collideWithGoal(leftGoal);

            assertFalse("Head must not overlap crossbar from the side at tick " + t,
                    bar.contactWith(player.getHeadShape()).isPresent());
        }
    }
}
