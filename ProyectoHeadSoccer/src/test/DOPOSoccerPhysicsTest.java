package test;

import domain.AuraAzulPower;
import domain.Ball;
import domain.BallView;
import domain.Circle;
import domain.Contact;
import domain.Country;
import domain.DOPOSoccerException;
import domain.Field;
import domain.Foot;
import domain.Goal;
import domain.LightningPower;
import domain.Match;
import domain.MatchConfig;
import domain.MatchType;
import domain.Modality;
import domain.Player;
import domain.PlayerAction;
import domain.PlayerBody;
import domain.PlayerProfile;
import domain.PlayerStats;
import domain.PlayerView;
import domain.Rectangle;
import domain.Side;
import java.util.EnumSet;
import java.util.Random;
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

    /**
     * Verifies that kicking a stationary ball on the ground launches it forward and upward.
     */
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

    /**
     * Verifies that kicking a rolling ball connects across at least 25 of 30 approach offsets.
     */
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

    /**
     * Verifies that kicking while the ball is behind the foot does not launch it toward own goal.
     */
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

    /**
     * Verifies that jumping directly under the goal crossbar does not trap the player's head inside.
     */
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

            double penetration = bar.contactWith(player.getHeadShape())
                    .map(Contact::getPenetration).orElse(0.0);
            if (penetration > 0.5) {
                consecutiveInsideTicks++;
                maxConsecutiveInside = Math.max(maxConsecutiveInside, consecutiveInsideTicks);
            } else {
                consecutiveInsideTicks = 0;
            }
        }

        assertTrue("Head stayed inside crossbar for " + maxConsecutiveInside + " consecutive ticks",
                maxConsecutiveInside <= 2);
    }

    /**
     * Verifies that landing on top of the goal crossbar keeps the player's foot stably supported.
     */
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

    /**
     * Verifies that walking horizontally into the goal crossbar never overlaps the player's head.
     */
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

    /**
     * Verifies that a ball falling near a stationary foot on either side never shoots upward to the ceiling.
     */
    @Test
    public void testBallFallingNearFootDoesNotShootToCeiling() {
        checkBallFallingNearFootForSide(Side.LEFT);
        checkBallFallingNearFootForSide(Side.RIGHT);
    }

    /*
     * Simulates a ball dropping near the player's stationary foot for a specific side
     * and asserts bounded upward rebound and no ceiling impact.
     */
    private void checkBallFallingNearFootForSide(Side side) {
        for (int dx = -8; dx <= 8; dx += 4) {
            Player player = new Player(new PlayerProfile("Player"), Country.SOUTH_KOREA, side);
            double footX = player.getFootShape().getX();
            Ball ball = new Ball(footX + dx * side.dir(), Field.HEIGHT - 25.0, 10.0);
            ball.setVerticalSpeed(4.0);

            for (int t = 0; t < 40; t++) {
                ball.tick();
                ball.collideWithEdges();
                player.collideWithBall(ball);

                assertTrue("Upward speed exceeded limit for " + side, ball.getVerticalSpeed() >= -15.0);
                assertTrue("Ball reached ceiling for " + side, ball.getY() - ball.getRadius() > 0.0);
            }
        }
    }

    /**
     * Verifies that a faster incoming ball rebounds faster off a player's head than a slower ball on both sides.
     */
    @Test
    public void testHeaderReboundScalesWithIncomingSpeed() {
        assertTrue(headerOutwardSpeed(Side.LEFT, 16.0) > headerOutwardSpeed(Side.LEFT, 4.0));
        assertTrue(headerOutwardSpeed(Side.RIGHT, 16.0) > headerOutwardSpeed(Side.RIGHT, 4.0));
    }

    /*
     * Drops a ball vertically onto the top of the player's head at a given downward speed
     * and returns the resulting upward rebound speed magnitude.
     */
    private double headerOutwardSpeed(Side side, double incomingDownwardSpeed) {
        Player player = new Player(new PlayerProfile("Player"), Country.SOUTH_KOREA, side);
        Circle head = player.getHeadShape();
        Ball ball = new Ball(head.getX(), head.getY() - head.getRadius() - 8.0, 10.0);
        ball.setVerticalSpeed(incomingDownwardSpeed);
        player.collideWithBall(ball);
        return -ball.getVerticalSpeed();
    }

    /**
     * Verifies that kicking a stationary ball placed 0 to 8 pixels in front of the resting foot
     * never sends the ball behind the player on either side.
     */
    @Test
    public void testKickBallCloseToFootNeverEndsBehindPlayer() {
        checkKickBallCloseToFootForSide(Side.LEFT);
        checkKickBallCloseToFootForSide(Side.RIGHT);
    }

    /*
     * Simulates kicking a ground ball from offsets 0..8 in front of the resting foot for a given side
     * and asserts the ball remains ahead of the player's head center.
     */
    private void checkKickBallCloseToFootForSide(Side side) {
        for (int dx = 0; dx <= 8; dx++) {
            Player player = new Player(new PlayerProfile("Player"), Country.SOUTH_KOREA, side);
            double footX = player.getFootShape().getX();
            double headX = player.getHeadShape().getX();
            Ball ball = new Ball(footX + dx * side.dir(), Field.HEIGHT - 10.0, 10.0);

            player.kick();
            for (int t = 0; t < 20; t++) {
                player.tick();
                ball.tick();
                ball.collideWithEdges();
                player.collideWithBall(ball);
            }

            double relativeForwardX = (ball.getX() - headX) * side.dir();
            assertTrue("Ball ended behind player for " + side + " at dx=" + dx, relativeForwardX > 0.0);
        }
    }

    /**
     * Verifies that two players sandwiching the ball on the ground never push the ball below the floor.
     */
    @Test
    public void testFloorDisputeNeverPushesBallBelowFloor() {
        Player left = new Player(new PlayerProfile("Left"), Country.SOUTH_KOREA, Side.LEFT);
        Player right = new Player(new PlayerProfile("Right"), Country.CAMEROON, Side.RIGHT);
        double midX = Field.WIDTH / 2.0;
        left.translate((midX - 22.0) - left.getHeadShape().getX(), 0.0);
        right.translate((midX + 22.0) - right.getHeadShape().getX(), 0.0);
        Ball ball = new Ball(midX, Field.HEIGHT - 10.0, 10.0);

        for (int t = 0; t < 120; t++) {
            left.moveRight();
            right.moveLeft();
            left.tick();
            right.tick();
            ball.tick();
            ball.collideWithEdges();
            left.collideWithPlayer(right);
            right.collideWithPlayer(left);
            left.collideWithBall(ball);
            right.collideWithBall(ball);

            assertTrue("Ball sank below floor at tick " + t,
                    ball.getY() + ball.getRadius() <= Field.HEIGHT + 0.001);
        }
    }

    /**
     * Verifies that walking continuously toward one's own goal while pushing the ball scores an own goal on both sides.
     */
    @Test
    public void testWalkingBallIntoOwnGoalScoresForBothSides() {
        assertTrue("Left player should push ball into left goal", canPushBallIntoOwnGoal(Side.LEFT));
        assertTrue("Right player should push ball into right goal", canPushBallIntoOwnGoal(Side.RIGHT));
    }

    /*
     * Simulates a player walking toward their own goal pushing a ground ball and returns true if a goal is detected.
     */
    private boolean canPushBallIntoOwnGoal(Side side) {
        Player player = new Player(new PlayerProfile("Player"), Country.SOUTH_KOREA, side);
        Goal ownGoal = new Goal(side);
        double goalEntryX = (side == Side.LEFT) ? 70.0 : Field.WIDTH - 70.0;
        player.translate((goalEntryX + side.dir() * 25.0) - player.getHeadShape().getX(), 0.0);
        Ball ball = new Ball(goalEntryX, Field.HEIGHT - 10.0, 10.0);

        for (int t = 0; t < 60; t++) {
            if (side == Side.LEFT) {
                player.moveLeft();
            } else {
                player.moveRight();
            }
            player.tick();
            ball.tick();
            ball.collideWithEdges();
            player.collideWithGoal(ownGoal);
            player.collideWithBall(ball);
            if (ownGoal.detectGoal(ball)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifies exact mirror symmetry between LEFT and RIGHT players when pushing and kicking a ground ball.
     */
    @Test
    public void testLeftAndRightPhysicsSymmetry() {
        Player left = new Player(new PlayerProfile("Left"), Country.SOUTH_KOREA, Side.LEFT);
        Player right = new Player(new PlayerProfile("Right"), Country.SOUTH_KOREA, Side.RIGHT);

        Ball leftBall = new Ball(left.getFootShape().getX() + 18.0, Field.HEIGHT - 10.0, 10.0);
        Ball rightBall = new Ball(right.getFootShape().getX() - 18.0, Field.HEIGHT - 10.0, 10.0);

        left.kick();
        right.kick();
        double leftMinVerticalSpeed = 0.0;
        double rightMinVerticalSpeed = 0.0;
        for (int t = 0; t < 10; t++) {
            left.tick();
            right.tick();
            leftBall.tick();
            rightBall.tick();
            leftBall.collideWithEdges();
            rightBall.collideWithEdges();
            left.collideWithBall(leftBall);
            right.collideWithBall(rightBall);
            leftMinVerticalSpeed = Math.min(leftMinVerticalSpeed, leftBall.getVerticalSpeed());
            rightMinVerticalSpeed = Math.min(rightMinVerticalSpeed, rightBall.getVerticalSpeed());
        }

        assertTrue("Left player kick should launch ball right", leftBall.getHorizontalSpeed() > 5.0);
        assertTrue("Right player kick should launch ball left", rightBall.getHorizontalSpeed() < -5.0);
        assertTrue("Left player kick should lift ball upward", leftMinVerticalSpeed < 0.0);
        assertTrue("Right player kick should lift ball upward", rightMinVerticalSpeed < 0.0);
    }

    /**
     * Verifies that when two adjacent players simultaneously kick a ball between them,
     * the ball pops upward and never remains overlapping either player's body.
     *
     * @throws DOPOSoccerException if match configuration is invalid
     */
    @Test
    public void testSimultaneousKickPinchPopsBallUpwardAndLeavesNoOverlap() throws DOPOSoccerException {
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.CAMEROON)
                .matchType(MatchType.TIME)
                .targetValue(1)
                .build();
        Match match = new Match(config);

        boolean poppedUp = false;
        for (int t = 0; t < 120; t++) {
            match.setIntention(Side.LEFT, EnumSet.of(PlayerAction.RIGHT, PlayerAction.KICK));
            match.setIntention(Side.RIGHT, EnumSet.of(PlayerAction.LEFT, PlayerAction.KICK));
            match.tick();

            BallView b = match.getBall();
            if (b.getVerticalSpeed() < -5.0 && Math.abs(b.getHorizontalSpeed()) < 0.01) {
                poppedUp = true;
                Circle ballCircle = new Circle(b.getX(), b.getY(), b.getRadius());
                PlayerView p1 = match.getPlayer(Side.LEFT);
                PlayerView p2 = match.getPlayer(Side.RIGHT);
                assertFalse("Ball should not remain inside left player after pinch escape",
                        ballCircle.detectCollisionWithCircle(p1.getHeadShape())
                                || ballCircle.detectCollisionWithRectangle(p1.getBodyShape())
                                || ballCircle.detectCollisionWithCircle(p1.getFootShape()));
                assertFalse("Ball should not remain inside right player after pinch escape",
                        ballCircle.detectCollisionWithCircle(p2.getHeadShape())
                                || ballCircle.detectCollisionWithRectangle(p2.getBodyShape())
                                || ballCircle.detectCollisionWithCircle(p2.getFootShape()));
                break;
            }
        }
        assertTrue("Ball pinched between both kicking players should pop upward", poppedUp);
    }

    /**
     * Verifies that walking into a stationary ground ball imparts forward dribbling velocity.
     */
    @Test
    public void testWalkingIntoStationaryBallImpartsDribbleSpeed() {
        Player player = new Player(new PlayerProfile("Dribbler"), Country.SOUTH_KOREA, Side.LEFT);
        double footX = player.getFootShape().getX();
        double footR = player.getFootShape().getRadius();
        Ball ball = new Ball(footX + footR + 9.0, Field.HEIGHT - 8.0, 8.0);

        for (int t = 0; t < 5; t++) {
            player.moveRight();
            player.tick();
            ball.tick();
            ball.collideWithEdges();
            player.collideWithBall(ball);
        }

        assertTrue("Ball should gain forward horizontal speed when dribbled",
                ball.getHorizontalSpeed() > 1.0);
    }

    /**
     * Verifies that the kick angle variation is reproducible with a fixed seed: two bodies with the
     * same seed, one on each side, launch the ball with exactly mirrored velocities.
     */
    @Test
    public void testSeededKickIsReproducibleAndMirrored() {
        PlayerBody left = new PlayerBody(Side.LEFT, new Random(42));
        PlayerBody right = new PlayerBody(Side.RIGHT, new Random(42));

        Ball leftBall = new Ball(left.getFootShape().getX() + 18.0, Field.HEIGHT - 10.0, 10.0);
        Ball rightBall = new Ball(right.getFootShape().getX() - 18.0, Field.HEIGHT - 10.0, 10.0);

        left.startKick();
        right.startKick();
        for (int t = 0; t < 10; t++) {
            left.tick();
            right.tick();
            leftBall.tick();
            rightBall.tick();
            leftBall.collideWithEdges();
            rightBall.collideWithEdges();
            left.collideWithBall(leftBall, null);
            right.collideWithBall(rightBall, null);
        }

        assertTrue("Left kick should launch the ball right", leftBall.getHorizontalSpeed() > 5.0);
        assertEquals(leftBall.getHorizontalSpeed(), -rightBall.getHorizontalSpeed(), 0.001);
        assertEquals(leftBall.getVerticalSpeed(), rightBall.getVerticalSpeed(), 0.001);
    }
}
