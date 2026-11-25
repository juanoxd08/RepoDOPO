package test;

import domain.BallView;
import domain.Circle;
import domain.ClumsyMachine;
import domain.Country;
import domain.DOPOSoccerException;
import domain.DefensiveMachine;
import domain.ExpertMachine;
import domain.Field;
import domain.GameStatus;
import domain.MachineController;
import domain.MachineProfile;
import domain.Match;
import domain.MatchConfig;
import domain.MatchType;
import domain.MatchView;
import domain.Modality;
import domain.OffensiveMachine;
import domain.PlayerAction;
import domain.PlayerEffect;
import domain.PlayerView;
import domain.Rectangle;
import domain.Side;
import domain.StatType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests verifying all autonomous machine profiles, MachineProfile factory methods,
 * tactical branching, and crossbar roll-off recovery.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class DOPOSoccerMachineTest {

    /**
     * Verifies that MachineProfile.create returns the expected controller subclass for every profile.
     */
    @Test
    public void testMachineProfileCreatesExpectedControllerTypes() {
        assertTrue(MachineProfile.TORPE.create(Side.RIGHT) instanceof ClumsyMachine);
        assertTrue(MachineProfile.OFENSIVA.create(Side.RIGHT) instanceof OffensiveMachine);
        assertTrue(MachineProfile.DEFENSIVA.create(Side.RIGHT) instanceof DefensiveMachine);
        assertTrue(MachineProfile.EXPERTA.create(Side.RIGHT) instanceof ExpertMachine);

        assertTrue(MachineProfile.TORPE.create(Side.LEFT, new Random(7L)) instanceof ClumsyMachine);
        assertTrue(MachineProfile.EXPERTA.create(Side.LEFT, new Random(7L)) instanceof ExpertMachine);
        assertEquals(Side.LEFT, MachineProfile.DEFENSIVA.create(Side.LEFT).getSide());
    }

    /**
     * Verifies that DefensiveMachine returns to its home zone when the ball is far,
     * and intercepts, jumps, and kicks when the ball enters its defensive half.
     */
    @Test
    public void testDefensiveMachineReturnsHomeWhenBallFarAndDefendsWhenNear() {
        DefensiveMachine machine = new DefensiveMachine(Side.RIGHT);

        StubMatch farMatch = new StubMatch()
                .withPlayer(Side.RIGHT, new StubPlayer(Side.RIGHT, 200, Field.HEIGHT - 38))
                .withBall(new StubBall(80, Field.HEIGHT - 8, 0, 0, false));
        Set<PlayerAction> farActions = machine.decide(farMatch);
        assertTrue("Defensive machine should move right toward home position when ball is far",
                farActions.contains(PlayerAction.RIGHT));
        assertFalse(farActions.contains(PlayerAction.KICK));

        StubMatch nearLowMatch = new StubMatch()
                .withPlayer(Side.RIGHT, new StubPlayer(Side.RIGHT, 260, Field.HEIGHT - 38))
                .withBall(new StubBall(245, Field.HEIGHT - 12, -3, 0, false));
        Set<PlayerAction> nearLowActions = machine.decide(nearLowMatch);
        assertTrue("Defensive machine should kick a low ball in front of it",
                nearLowActions.contains(PlayerAction.KICK));

        StubMatch nearHighMatch = new StubMatch()
                .withPlayer(Side.RIGHT, new StubPlayer(Side.RIGHT, 270, Field.HEIGHT - 38))
                .withBall(new StubBall(265, Field.HEIGHT - 60, -2, 0, false));
        Set<PlayerAction> nearHighActions = machine.decide(nearHighMatch);
        assertTrue("Defensive machine should jump for a high ball in its zone",
                nearHighActions.contains(PlayerAction.JUMP));
    }

    /**
     * Verifies that ClumsyMachine is deterministic with a fixed seed, holds each chosen action
     * for at least 12 ticks, and activates power when the gauge is full.
     */
    @Test
    public void testClumsyMachineDeterministicAndHoldsActions() {
        MachineController m1 = MachineProfile.TORPE.create(Side.RIGHT, new Random(42L));
        MachineController m2 = MachineProfile.TORPE.create(Side.RIGHT, new Random(42L));

        StubPlayer me = new StubPlayer(Side.RIGHT, 250, Field.HEIGHT - 38).withPower(100.0);
        StubMatch match = new StubMatch()
                .withPlayer(Side.RIGHT, me)
                .withBall(new StubBall(175, Field.HEIGHT - 8, 0, 0, false));

        Set<PlayerAction> first = m1.decide(match);
        assertEquals(first, m2.decide(match));
        for (int t = 1; t < 12; t++) {
            assertEquals("ClumsyMachine must hold its buffered action for at least 12 ticks",
                    first, m1.decide(match));
            m2.decide(match);
        }

        boolean sawPower = false;
        boolean sawMove = false;
        for (int t = 0; t < 1000; t++) {
            Set<PlayerAction> actions = m1.decide(match);
            if (actions.contains(PlayerAction.POWER)) sawPower = true;
            if (actions.contains(PlayerAction.LEFT) || actions.contains(PlayerAction.RIGHT)) sawMove = true;
        }
        assertTrue("ClumsyMachine should eventually activate full power", sawPower);
        assertTrue("ClumsyMachine should explore movement choices", sawMove);
    }

    /**
     * Verifies that OffensiveMachine chases the ball in front or behind, jumps, kicks,
     * activates full power when close, and backs off when the ball rests on a crossbar.
     */
    @Test
    public void testOffensiveMachineChasesKicksPowersAndBacksOffCrossBar() {
        OffensiveMachine machine = new OffensiveMachine(Side.RIGHT);

        StubMatch chaseMatch = new StubMatch()
                .withPlayer(Side.RIGHT, new StubPlayer(Side.RIGHT, 270, Field.HEIGHT - 38).withPower(100.0))
                .withBall(new StubBall(245, Field.HEIGHT - 12, 0, 0, false));
        Set<PlayerAction> attackActions = machine.decide(chaseMatch);
        assertTrue(attackActions.contains(PlayerAction.LEFT));
        assertTrue(attackActions.contains(PlayerAction.KICK));
        assertTrue(attackActions.contains(PlayerAction.POWER));

        StubMatch behindMatch = new StubMatch()
                .withPlayer(Side.RIGHT, new StubPlayer(Side.RIGHT, 200, Field.HEIGHT - 38))
                .withBall(new StubBall(240, Field.HEIGHT - 55, 0, 0, false));
        Set<PlayerAction> behindActions = machine.decide(behindMatch);
        assertTrue("OffensiveMachine should backtrack behind a ball past it",
                behindActions.contains(PlayerAction.RIGHT));

        StubMatch crossbarMatch = new StubMatch()
                .withPlayer(Side.RIGHT, new StubPlayer(Side.RIGHT, 35, Field.HEIGHT - 38))
                .withBall(new StubBall(15, Field.HEIGHT - 96, 0, 0, false));
        Set<PlayerAction> backoffActions = machine.decide(crossbarMatch);
        assertTrue("OffensiveMachine should step back when ball lies on a crossbar",
                backoffActions.contains(PlayerAction.RIGHT));
        assertFalse(backoffActions.contains(PlayerAction.KICK));
    }

    /**
     * Verifies all tactical branches of ExpertMachine: power threat, rival aura, rival under effect,
     * sudden death, winning, losing, and close-range power activation.
     */
    @Test
    public void testExpertMachineCoversAllTacticalBranches() {
        ExpertMachine expert = new ExpertMachine(Side.RIGHT);

        StubMatch losingMatch = new StubMatch()
                .withGoals(2, 1)
                .withPlayer(Side.LEFT, new StubPlayer(Side.LEFT, 80, Field.HEIGHT - 38))
                .withPlayer(Side.RIGHT, new StubPlayer(Side.RIGHT, 270, Field.HEIGHT - 38))
                .withBall(new StubBall(100, Field.HEIGHT - 8, 0, 0, false));
        assertTrue("When losing, Expert should attack toward the ball on the left",
                expert.decide(losingMatch).contains(PlayerAction.LEFT));

        StubMatch winningMatch = new StubMatch()
                .withGoals(0, 2)
                .withPlayer(Side.LEFT, new StubPlayer(Side.LEFT, 80, Field.HEIGHT - 38))
                .withPlayer(Side.RIGHT, new StubPlayer(Side.RIGHT, 200, Field.HEIGHT - 38))
                .withBall(new StubBall(80, Field.HEIGHT - 8, 0, 0, false));
        assertTrue("When winning and ball is far, Expert should retreat defensively",
                expert.decide(winningMatch).contains(PlayerAction.RIGHT));

        StubMatch rivalAuraMatch = new StubMatch()
                .withGoals(2, 1)
                .withPlayer(Side.LEFT, new StubPlayer(Side.LEFT, 80, Field.HEIGHT - 38).withAura(true))
                .withPlayer(Side.RIGHT, new StubPlayer(Side.RIGHT, 200, Field.HEIGHT - 38))
                .withBall(new StubBall(80, Field.HEIGHT - 8, 0, 0, false));
        assertTrue("When rival has aura, Expert should switch to defense",
                expert.decide(rivalAuraMatch).contains(PlayerAction.RIGHT));

        StubMatch powerThreatMatch = new StubMatch()
                .withGoals(2, 1)
                .withPlayer(Side.LEFT, new StubPlayer(Side.LEFT, 80, Field.HEIGHT - 38))
                .withPlayer(Side.RIGHT, new StubPlayer(Side.RIGHT, 200, Field.HEIGHT - 38))
                .withBall(new StubBall(120, Field.HEIGHT - 40, 15, 0, true));
        assertTrue("When a power shot approaches its goal, Expert should defend",
                expert.decide(powerThreatMatch).contains(PlayerAction.RIGHT));

        StubMatch rivalEffectMatch = new StubMatch()
                .withGoals(0, 2)
                .withPlayer(Side.LEFT, new StubPlayer(Side.LEFT, 80, Field.HEIGHT - 38).withEffect(PlayerEffect.ELECTROCUTED))
                .withPlayer(Side.RIGHT, new StubPlayer(Side.RIGHT, 270, Field.HEIGHT - 38))
                .withBall(new StubBall(100, Field.HEIGHT - 8, 0, 0, false));
        assertTrue("When rival is under a power effect, Expert should attack even while winning",
                expert.decide(rivalEffectMatch).contains(PlayerAction.LEFT));

        StubMatch suddenDeathOwnHalf = new StubMatch()
                .withSuddenDeath(true)
                .withPlayer(Side.LEFT, new StubPlayer(Side.LEFT, 80, Field.HEIGHT - 38))
                .withPlayer(Side.RIGHT, new StubPlayer(Side.RIGHT, 240, Field.HEIGHT - 38))
                .withBall(new StubBall(260, Field.HEIGHT - 8, 0, 0, false));
        assertTrue("In sudden death with ball in own half, Expert should move behind ball to defend",
                expert.decide(suddenDeathOwnHalf).contains(PlayerAction.RIGHT));

        StubMatch powerReadyClose = new StubMatch()
                .withGoals(1, 1)
                .withPlayer(Side.LEFT, new StubPlayer(Side.LEFT, 80, Field.HEIGHT - 38))
                .withPlayer(Side.RIGHT, new StubPlayer(Side.RIGHT, 220, Field.HEIGHT - 38).withPower(100.0))
                .withBall(new StubBall(195, Field.HEIGHT - 20, 0, 0, false));
        assertTrue("Expert should activate full power when ball is close and in front",
                expert.decide(powerReadyClose).contains(PlayerAction.POWER));
    }

    /**
     * Verifies that a ball resting on top of a goal crossbar rolls back toward the pitch.
     *
     * @throws DOPOSoccerException if match configuration is invalid
     */
    @Test
    public void testBallRestingOnCrossBarRollsIntoPitch() throws Exception {
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.CAMEROON)
                .matchType(MatchType.TIME)
                .targetValue(1)
                .build();
        Match match = new Match(config);
        Rectangle leftBar = match.getCrossBar(Side.LEFT);
        double restX = leftBar.getX() + leftBar.getWidth() / 2.0;
        double restY = leftBar.getY() - match.getBall().getRadius();

        java.lang.reflect.Field ballField = Match.class.getDeclaredField("ball");
        ballField.setAccessible(true);
        domain.Ball rawBall = (domain.Ball) ballField.get(match);
        rawBall.reset(restX, restY);

        match.tick();
        assertTrue("Ball resting on left crossbar should gain positive speed toward pitch",
                match.getBall().getHorizontalSpeed() > 0.0);
    }

    /*
     * Minimal read-only PlayerView stub for deterministic AI unit tests.
     */
    private static class StubPlayer implements PlayerView {
        private final Side side;
        private final double x;
        private final double y;
        private double powerProgress;
        private boolean aura;
        private boolean stunned;
        private PlayerEffect effect = PlayerEffect.NONE;

        /*
         * Creates a stub player centered at the specified head coordinates.
         */
        StubPlayer(Side side, double x, double y) {
            this.side = side;
            this.x = x;
            this.y = y;
        }

        /*
         * Sets the power gauge charge percentage.
         */
        private StubPlayer withPower(double p) { this.powerProgress = p; return this; }

        /*
         * Sets whether the power aura is active.
         */
        private StubPlayer withAura(boolean a) { this.aura = a; return this; }

        /*
         * Sets the active status effect enum.
         */
        private StubPlayer withEffect(PlayerEffect e) { this.effect = e; return this; }

        @Override public String getName() { return "Stub"; }
        @Override public Country getCountry() { return Country.SOUTH_KOREA; }
        @Override public Side getSide() { return side; }
        @Override public double getLife() { return 100; }
        @Override public double getMaxLife() { return 100; }
        @Override public double getPowerProgress() { return powerProgress; }
        @Override public boolean hasAura() { return aura; }
        @Override public boolean isStunned() { return stunned; }
        @Override public boolean isKicking() { return false; }
        @Override public String getActiveEffectName() { return effect == PlayerEffect.NONE ? "" : effect.name(); }
        @Override public PlayerEffect getActiveEffect() { return effect; }
        @Override public double getEffectProgress() { return effect == PlayerEffect.NONE ? 0.0 : 0.5; }
        @Override public Circle getHeadShape() { return new Circle(x, y, 14); }
        @Override public Rectangle getBodyShape() { return new Rectangle(x - 9, y + 12, 18, 16); }
        @Override public Circle getFootShape() { return new Circle(x + side.dir() * 6, Field.HEIGHT - 8, 8); }
        @Override public int getStat(StatType type) { return 1; }
        @Override public int getPoints() { return 0; }
    }

    /*
     * Minimal read-only BallView stub for deterministic AI unit tests.
     */
    private static class StubBall implements BallView {
        private final double x;
        private final double y;
        private final double vx;
        private final double vy;
        private final boolean powerShot;

        /*
         * Creates a stub ball with the given position, velocity, and power-shot flag.
         */
        StubBall(double x, double y, double vx, double vy, boolean powerShot) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.powerShot = powerShot;
        }

        @Override public double getX() { return x; }
        @Override public double getY() { return y; }
        @Override public double getRadius() { return 8.0; }
        @Override public boolean hasPowerShot() { return powerShot; }
        @Override public double getHorizontalSpeed() { return vx; }
        @Override public double getVerticalSpeed() { return vy; }
        @Override public Country getPowerShotCountry() { return powerShot ? Country.SOUTH_KOREA : null; }
        @Override public boolean isPowerShotCharging() { return false; }
        @Override public double getPowerShotChargeProgress() { return 0.0; }
    }

    /*
     * Minimal read-only MatchView stub for deterministic AI unit tests.
     */
    private static class StubMatch implements MatchView {
        private PlayerView left;
        private PlayerView right;
        private BallView ball;
        private int leftGoals;
        private int rightGoals;
        private boolean suddenDeath;
        private int remainingSeconds = 60;

        /*
         * Assigns a stub player to the given side.
         */
        private StubMatch withPlayer(Side side, PlayerView p) {
            if (side == Side.LEFT) left = p; else right = p;
            return this;
        }

        /*
         * Assigns a stub ball to this match view.
         */
        private StubMatch withBall(BallView b) { this.ball = b; return this; }

        /*
         * Sets scoreboard goals for left and right sides.
         */
        private StubMatch withGoals(int l, int r) { this.leftGoals = l; this.rightGoals = r; return this; }

        /*
         * Configures whether sudden death is active.
         */
        private StubMatch withSuddenDeath(boolean sd) { this.suddenDeath = sd; return this; }

        @Override public GameStatus getStatus() { return suddenDeath ? GameStatus.SUDDEN_DEATH : GameStatus.PLAYING; }
        @Override public int getRemainingSeconds() { return remainingSeconds; }
        @Override public boolean isSuddenDeath() { return suddenDeath; }
        @Override public int getGoals(Side side) { return side == Side.LEFT ? leftGoals : rightGoals; }
        @Override public PlayerView getPlayer(Side side) { return side == Side.LEFT ? left : right; }
        @Override public BallView getBall() { return ball; }
        @Override public Optional<Side> getWinner() { return Optional.empty(); }
        @Override public int getFieldWidth() { return Field.WIDTH; }
        @Override public int getFieldHeight() { return Field.HEIGHT; }
        @Override public Rectangle getGoalArea(Side side) {
            return side == Side.LEFT ? new Rectangle(0, Field.HEIGHT - 85, 30, 85)
                    : new Rectangle(Field.WIDTH - 30, Field.HEIGHT - 85, 30, 85);
        }
        @Override public Rectangle getCrossBar(Side side) {
            return side == Side.LEFT ? new Rectangle(0, Field.HEIGHT - 90, 30, 5)
                    : new Rectangle(Field.WIDTH - 30, Field.HEIGHT - 90, 30, 5);
        }
        @Override public Modality getModality() { return Modality.PVC; }
        @Override public List<Circle> getFakeBalls() { return Collections.emptyList(); }
    }
}
