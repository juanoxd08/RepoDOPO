package test;

import domain.AuraAzulPower;
import domain.Ball;
import domain.BallView;
import domain.Circle;
import domain.Controller;
import domain.Country;
import domain.DOPOSoccer;
import domain.DOPOSoccerException;
import domain.Effect;
import domain.Field;
import domain.GameStatus;
import domain.IllusionPower;
import domain.LightningPower;
import domain.MachineProfile;
import domain.Match;
import domain.MatchConfig;
import domain.MatchListener;
import domain.MatchType;
import domain.Modality;
import domain.Physics;
import domain.Player;
import domain.PlayerAction;
import domain.PlayerBody;
import domain.PlayerEffect;
import domain.PlayerProfile;
import domain.PlayerStats;
import domain.PlayerView;
import domain.Power;
import domain.PowerHit;
import domain.Rectangle;
import domain.Side;
import domain.StatType;
import domain.TornadoPower;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Domain unit tests validating the specifications, mathematical formulas, and rules of DOPOSoccer.
 * Covers:
 * - Stats formulas (Kick multiplier, kick damage, Survival maximum health).
 * - Health depletion, bounds clamping, and zero-health 3-second stun recovery.
 * - Powers charging, +25% on receiving goal, aura activation, and status effects.
 * - All 4 country powers from Anexo 1 (Aura Azul, Rayo, Tornado, IlusiÃ³n).
 * - Match scoring, sudden death (golden goal), and end-of-match point distribution.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class DOPOSoccerDomainTest {

    private Player p1;
    private Player p2;
    private Ball ball;
    private Field field;

    /**
     * Initializes baseline players, ball, and field before each test.
     */
    @Before
    public void setUp() throws DOPOSoccerException {
        p1 = new Player("Jugador 1", Country.SOUTH_KOREA, Side.LEFT, new AuraAzulPower(), new PlayerStats(1, 1, 1, 1));
        p2 = new Player("Jugador 2", Country.CAMEROON, Side.RIGHT, new LightningPower(), new PlayerStats(1, 1, 1, 1));
        ball = new Ball(Field.WIDTH / 2.0, 50.0, 10.0);
        field = new Field();
    }

    /**
     * Verifies the survival maximum health formula across levels 1 to 10.
     *
     * @throws DOPOSoccerException if stat values are invalid
     */
    @Test
    public void testSurvivalFormula() throws DOPOSoccerException {
        PlayerStats stats1 = new PlayerStats(1, 1, 1, 1);
        assertEquals(100.0, stats1.getMaxLife(), 0.001);

        PlayerStats stats3 = new PlayerStats(1, 1, 1, 3);
        assertEquals(120.0, stats3.getMaxLife(), 0.001);

        PlayerStats stats5 = new PlayerStats(1, 1, 1, 5);
        assertEquals(140.0, stats5.getMaxLife(), 0.001);

        PlayerStats stats10 = new PlayerStats(1, 1, 1, 10);
        assertEquals(190.0, stats10.getMaxLife(), 0.001);
    }

    /**
     * Verifies the kick damage multiplier formula across levels 1 to 10.
     *
     * @throws DOPOSoccerException if stat values are invalid
     */
    @Test
    public void testKickMultiplierFormula() throws DOPOSoccerException {
        PlayerStats stats1 = new PlayerStats(1, 1, 1, 1);
        assertEquals(1.0, stats1.getKickMultiplier(), 0.001);

        PlayerStats stats5 = new PlayerStats(1, 1, 5, 1);
        assertEquals(1.4, stats5.getKickMultiplier(), 0.001);

        PlayerStats stats10 = new PlayerStats(1, 1, 10, 1);
        assertEquals(1.9, stats10.getKickMultiplier(), 0.001);
    }

    /**
     * Verifies normal kick damage computation from kick level.
     *
     * @throws DOPOSoccerException if stat values are invalid
     */
    @Test
    public void testKickDamageFormula() throws DOPOSoccerException {
        PlayerStats stats1 = new PlayerStats(1, 1, 1, 1);
        assertEquals(10.0, stats1.getKickDamage(), 0.001);

        PlayerStats stats5 = new PlayerStats(1, 1, 5, 1);
        assertEquals(14.0, stats5.getKickDamage(), 0.001);
    }

    /**
     * Verifies that constructing stats below 1 throws DOPOSoccerException.
     *
     * @throws DOPOSoccerException expected validation exception
     */
    @Test(expected = DOPOSoccerException.class)
    public void testStatsBoundsValidationBelowMin() throws DOPOSoccerException {
        new PlayerStats(1, 1, 0, 1);
    }

    /**
     * Verifies that constructing stats above 10 throws DOPOSoccerException.
     *
     * @throws DOPOSoccerException expected validation exception
     */
    @Test(expected = DOPOSoccerException.class)
    public void testStatsBoundsValidationAboveMax() throws DOPOSoccerException {
        new PlayerStats(1, 1, 1, 11);
    }

    /**
     * Verifies that taking damage reduces current player health accordingly.
     */
    @Test
    public void testPlayerTakesDamage() {
        double initialHp = p1.getMaxLife();
        p1.takeDamage(30.0);
        assertEquals(initialHp - 30.0, p1.getLife(), 0.001);
    }

    /**
     * Verifies that player health is clamped at zero on excessive damage.
     */
    @Test
    public void testHealthCannotDropBelowZero() {
        p1.takeDamage(200.0);
        assertEquals(0.0, p1.getLife(), 0.001);
    }

    /**
     * Verifies that reaching zero health stuns and disables the player.
     */
    @Test
    public void testPlayerStunnedWhenHealthReachesZero() {
        p1.takeDamage(p1.getMaxLife());
        assertTrue(p1.isStunned());
        assertTrue(p1.isDisabled());
    }

    /**
     * Verifies that after the 3-second stun duration expires, full health is restored.
     */
    @Test
    public void testPlayerRecoversFullHealthAfterStunDuration() {
        p1.takeDamage(p1.getMaxLife());
        assertTrue(p1.isStunned());

        for (int i = 0; i < 185; i++) {
            p1.moveVertical();
        }

        assertFalse(p1.isStunned());
        assertEquals(p1.getMaxLife(), p1.getLife(), 0.001);
        assertFalse(p1.isDisabled());
    }

    /**
     * Verifies that the power bar charges progressively on each tick.
     */
    @Test
    public void testPowerChargesOverTime() {
        for (int i = 0; i < 60; i++) {
            p1.tick();
        }

        assertTrue(p1.getPowerProgress() > 0.0);
    }

    /**
     * Verifies that conceding a goal immediately adds 25% to the power bar.
     */
    @Test
    public void testPowerGainsOneQuarterOnReceivingGoal() {
        p1.onGoalReceived();
        assertEquals(25.0, p1.getPowerProgress(), 0.001);
    }

    /**
     * Verifies that activating a full power bar drains the charge and enables the aura.
     */
    @Test
    public void testActivatingPowerDrainsBarAndStartsEnduringAura() {
        for (int i = 0; i < 4; i++) {
            p1.onGoalReceived();
        }

        p1.activatePower();

        assertTrue(p1.hasAura());
        assertEquals(0.0, p1.getPowerProgress(), 0.001);
    }

    /**
     * Verifies that South Korea's Aura Azul power applies the PUSHED effect and damage on hit.
     */
    @Test
    public void testKoreaAuraAzulPushesOpponent() throws DOPOSoccerException {
        AuraAzulPower koreaPower = new AuraAzulPower();
        p1 = new Player("Jugador 1", Country.SOUTH_KOREA, Side.LEFT, koreaPower, new PlayerStats(1, 1, 1, 1));
        koreaPower.setCharge(100.0);
        p1.activatePower();

        koreaPower.executePowerShot(ball, p1.getSide());
        assertTrue(koreaPower.isPowerShotActive());
        for (int i = 0; i < Power.TICKS_PER_POWER; i++) {
            koreaPower.updateShot(ball);
        }

        p2.receiveHit(koreaPower.onHitRival(p1.getKickMultiplier()));
        assertEquals(PlayerEffect.PUSHED.name(), p2.getActiveEffectName());
        assertTrue(p2.getLife() < p2.getMaxLife());
    }

    /**
     * Verifies that Cameroon's Rayo power applies the ELECTROCUTED effect on hit.
     *
     * @throws DOPOSoccerException if stat values are invalid
     */
    @Test
    public void testCameroonLightningElectrocutesOpponent() throws DOPOSoccerException {
        LightningPower lightning = new LightningPower();
        p2 = new Player("Jugador 2", Country.CAMEROON, Side.RIGHT, lightning, new PlayerStats(1, 1, 1, 1));
        lightning.setCharge(100.0);
        p2.activatePower();

        lightning.executePowerShot(ball, p2.getSide());
        for (int i = 0; i < Power.TICKS_PER_POWER; i++) {
            lightning.updateShot(ball);
        }
        p1.receiveHit(lightning.onHitRival(p2.getKickMultiplier()));

        assertEquals(PlayerEffect.ELECTROCUTED.name(), p1.getActiveEffectName());
    }

    /**
     * Verifies that Nigeria's Tornado power applies the KNOCKED_OUT effect and disables the opponent.
     *
     * @throws DOPOSoccerException if stat values are invalid
     */
    @Test
    public void testNigeriaTornadoKnocksOutOpponent() throws DOPOSoccerException {
        TornadoPower tornado = new TornadoPower();
        p1 = new Player("Jugador 1", p1.getCountry(), Side.LEFT, tornado, new PlayerStats(1, 1, 1, 1));
        tornado.setCharge(100.0);
        p1.activatePower();

        tornado.executePowerShot(ball, p1.getSide());
        for (int i = 0; i < Power.TICKS_PER_POWER; i++) {
            tornado.updateShot(ball);
        }
        p2.receiveHit(tornado.onHitRival(p1.getKickMultiplier()));

        assertEquals(PlayerEffect.KNOCKED_OUT.name(), p2.getActiveEffectName());
        assertTrue(p2.isDisabled());
    }

    /**
     * Verifies that USA's Illusion power spawns 7 decoy balls around the real ball.
     *
     * @throws DOPOSoccerException if stat values are invalid
     */
    @Test
    public void testUSAIllusionDividesBallIntoEight() throws DOPOSoccerException {
        IllusionPower illusion = new IllusionPower();
        p1 = new Player("Jugador 1", p1.getCountry(), Side.LEFT, illusion, new PlayerStats(1, 1, 1, 1));
        illusion.setCharge(100.0);
        p1.activatePower();

        illusion.executePowerShot(ball, p1.getSide());
        for (int i = 0; i < Power.TICKS_PER_POWER; i++) {
            illusion.updateShot(ball);
        }

        assertEquals(7, illusion.getFakeBalls().size());
        assertTrue(illusion.hasActiveIllusion());
    }

    /**
     * Verifies that applying a new status effect replaces any previously active effect.
     */
    @Test
    public void testNewPowerEffectReplacesPreviousEffect() {
        p2.applyEffect(PlayerEffect.ELECTROCUTED, 3.0);
        assertEquals(PlayerEffect.ELECTROCUTED.name(), p2.getActiveEffectName());

        p2.applyEffect(PlayerEffect.PUSHED, 1.5);
        assertEquals(PlayerEffect.PUSHED.name(), p2.getActiveEffectName());
    }

    /**
     * Verifies that completing a match awards 100 points to the winner and 25 points to the loser.
     *
     * @throws DOPOSoccerException if match configuration is invalid
     */
    @Test
    public void testMatchAwardsPointsOnCompletion() throws Exception {
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.CAMEROON)
                .matchType(MatchType.GOALS)
                .targetValue(1)
                .build();
        Match match = new Match(config);

        Rectangle rightGoal = match.getGoalArea(Side.RIGHT);
        java.lang.reflect.Field ballField = Match.class.getDeclaredField("ball");
        ballField.setAccessible(true);
        Ball rawBall = (Ball) ballField.get(match);
        rawBall.reset(rightGoal.getX() + rightGoal.getWidth() / 2.0, rightGoal.getY() + rightGoal.getHeight() / 2.0);
        match.tick();

        assertEquals(GameStatus.FINISHED, match.getStatus());
        assertEquals(100, match.getPlayer(Side.LEFT).getPoints());
        assertEquals(25, match.getPlayer(Side.RIGHT).getPoints());
    }

    /**
     * Verifies that a time-based match tied at regular time expiration enters sudden death.
     *
     * @throws DOPOSoccerException if match configuration is invalid
     */
    @Test
    public void testSuddenDeathActivatesOnTimeTie() throws DOPOSoccerException {
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.CAMEROON)
                .matchType(MatchType.TIME)
                .targetValue(1)
                .build();
        Match match = new Match(config);

        int totalTicks = 60 * Physics.FPS + 1;
        for (int i = 0; i < totalTicks; i++) {
            match.tick();
        }

        assertEquals(GameStatus.SUDDEN_DEATH, match.getStatus());
    }

    /**
     * Verifies that upgrading a stat with enough points deducts cost, increments level, and doubles future cost.
     *
     * @throws DOPOSoccerException if upgrade unexpectedly fails
     */
    @Test
    public void testProfileUpgradeDeductsPointsAndDoublesCost() throws DOPOSoccerException {
        PlayerProfile profile = new PlayerProfile("Juan");
        profile.addPoints(2000);

        assertEquals(500, profile.upgradeCost(StatType.SPEED));
        profile.upgrade(StatType.SPEED);
        assertEquals(2, profile.getStat(StatType.SPEED));
        assertEquals(1500, profile.getPoints());

        assertEquals(1000, profile.upgradeCost(StatType.SPEED));
        profile.upgrade(StatType.SPEED);
        assertEquals(3, profile.getStat(StatType.SPEED));
        assertEquals(500, profile.getPoints());

        assertEquals(2000, profile.upgradeCost(StatType.SPEED));
    }

    /**
     * Verifies that upgrading without enough points throws NOT_ENOUGH_POINTS without mutating state.
     */
    @Test
    public void testProfileUpgradeWithoutEnoughPointsThrowsException() {
        PlayerProfile profile = new PlayerProfile("Juan");
        profile.addPoints(400);

        try {
            profile.upgrade(StatType.KICK);
            fail("Expected DOPOSoccerException when points are insufficient");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.NOT_ENOUGH_POINTS, e.getMessage());
            assertEquals(1, profile.getStat(StatType.KICK));
            assertEquals(400, profile.getPoints());
        }
    }

    /**
     * Verifies that upgrading a stat already at level 10 throws MAX_STAT_REACHED and returns cost 0.
     *
     * @throws DOPOSoccerException if stat setup fails
     */
    @Test
    public void testProfileUpgradeAtMaxLevelThrowsException() throws DOPOSoccerException {
        PlayerProfile profile = new PlayerProfile("Juan", new PlayerStats(10, 1, 1, 1));
        profile.addPoints(100000);

        assertEquals(0, profile.upgradeCost(StatType.SPEED));
        try {
            profile.upgrade(StatType.SPEED);
            fail("Expected DOPOSoccerException when stat is already at max level");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.MAX_STAT_REACHED, e.getMessage());
            assertEquals(10, profile.getStat(StatType.SPEED));
            assertEquals(100000, profile.getPoints());
        }
    }

    /**
     * Verifies that PlayerView exposes active PlayerEffect and normalized remaining progress [0.0, 1.0].
     */
    @Test
    public void testPlayerViewEffectAndProgressTrackRemainingFraction() {
        assertEquals(PlayerEffect.NONE, p2.getActiveEffect());
        assertEquals(0.0, p2.getEffectProgress(), 0.001);

        p2.applyEffect(PlayerEffect.ELECTROCUTED, 3.0);
        assertEquals(PlayerEffect.ELECTROCUTED, p2.getActiveEffect());
        assertEquals(1.0, p2.getEffectProgress(), 0.001);

        for (int i = 0; i < 90; i++) {
            p2.moveVertical();
        }
        assertEquals(0.5, p2.getEffectProgress(), 0.01);

        for (int i = 0; i < 90; i++) {
            p2.moveVertical();
        }
        assertEquals(PlayerEffect.NONE, p2.getActiveEffect());
        assertEquals(0.0, p2.getEffectProgress(), 0.001);
    }

    /**
     * Verifies that BallView exposes power shot country, charging state, and normalized charge progress.
     */
    @Test
    public void testBallViewPowerShotCountryAndChargingProgress() {
        for (int i = 0; i < 605; i++) {
            p1.tick();
        }
        p1.activatePower();

        double footX = p1.getFootShape().getX();
        Ball shotBall = new Ball(footX + 16.0, Field.HEIGHT - 10.0, 10.0);
        p1.kick();
        p1.tick();
        assertTrue(p1.collideWithBall(shotBall));

        assertTrue(shotBall.hasPowerShot());
        assertEquals(Country.SOUTH_KOREA, shotBall.getPowerShotCountry());
        assertTrue(shotBall.isPowerShotCharging());
        assertEquals(0.0, shotBall.getPowerShotChargeProgress(), 0.001);

        int halfTicks = Power.TICKS_PER_POWER / 2;
        for (int i = 0; i < halfTicks; i++) {
            p1.updatePowerShot(shotBall);
        }
        assertTrue(shotBall.isPowerShotCharging());
        assertEquals(0.5, shotBall.getPowerShotChargeProgress(), 0.02);

        for (int i = halfTicks; i < Power.TICKS_PER_POWER; i++) {
            p1.updatePowerShot(shotBall);
        }
        assertFalse(shotBall.isPowerShotCharging());
        assertEquals(0.0, shotBall.getPowerShotChargeProgress(), 0.001);
        assertTrue(shotBall.getHorizontalSpeed() > 0.0);
    }

    /**
     * Verifies that MatchListener events onPowerActivated, onBallKicked, onPowerShot, and onPowerHit
     * fire once each at the expected moments during a match.
     *
     * @throws DOPOSoccerException if match configuration is invalid
     */
    @Test
    public void testMatchEventsFireOnceAtExpectedMoments() throws DOPOSoccerException {
        DOPOSoccer game = new DOPOSoccer();
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.CAMEROON)
                .matchType(MatchType.TIME)
                .targetValue(2)
                .build();

        AtomicInteger powerActivatedCount = new AtomicInteger(0);
        AtomicInteger ballKickedCount = new AtomicInteger(0);
        AtomicInteger powerShotCount = new AtomicInteger(0);
        AtomicInteger powerHitCount = new AtomicInteger(0);
        AtomicReference<Country> shotCountry = new AtomicReference<>();
        AtomicReference<Side> hitVictim = new AtomicReference<>();
        AtomicReference<Double> hitDamage = new AtomicReference<>(0.0);

        game.addListener(new MatchListener() {
            @Override
            public void onPowerActivated(Side side) {
                if (side == Side.LEFT) powerActivatedCount.incrementAndGet();
            }

            @Override
            public void onBallKicked(Side kicker) {
                if (kicker == Side.LEFT) ballKickedCount.incrementAndGet();
            }

            @Override
            public void onPowerShot(Side shooter, Country country) {
                if (shooter == Side.LEFT) {
                    powerShotCount.incrementAndGet();
                    shotCountry.set(country);
                }
            }

            @Override
            public void onPowerHit(Side victim, double damage) {
                powerHitCount.incrementAndGet();
                hitVictim.set(victim);
                hitDamage.set(damage);
            }
        });

        game.newMatch(config);

        for (int i = 0; i < 605; i++) {
            game.tick();
        }
        assertEquals(0, powerActivatedCount.get());

        game.setIntention(Side.LEFT, EnumSet.of(PlayerAction.POWER));
        game.tick();
        game.tick();
        assertEquals(1, powerActivatedCount.get());

        for (int i = 0; i < 120 && powerShotCount.get() == 0; i++) {
            double px = game.getMatch().getPlayer(Side.LEFT).getFootShape().getX();
            double bx = game.getMatch().getBall().getX();
            if (Math.abs(bx - px) < 28.0) {
                game.setIntention(Side.LEFT, EnumSet.of(PlayerAction.RIGHT, PlayerAction.KICK));
            } else {
                game.setIntention(Side.LEFT, EnumSet.of(PlayerAction.RIGHT));
            }
            game.tick();
        }

        assertEquals(1, powerShotCount.get());
        assertEquals(Country.SOUTH_KOREA, shotCountry.get());
        assertTrue(ballKickedCount.get() >= 1);

        game.setIntention(Side.LEFT, EnumSet.noneOf(PlayerAction.class));
        for (int i = 0; i < Power.TICKS_PER_POWER + 60 && powerHitCount.get() == 0; i++) {
            game.tick();
        }

        assertEquals(1, powerHitCount.get());
        assertEquals(Side.RIGHT, hitVictim.get());
        assertTrue(hitDamage.get() > 0.0);
        assertFalse(game.getMatch().getBall().hasPowerShot());
    }

    /**
     * Verifies that an active power shot expires and clears the ball power flag after MAX_FLIGHT_TICKS without a hit.
     */
    @Test
    public void testPowerShotExpiresAfterMaxFlightTicks() {
        AuraAzulPower power = new AuraAzulPower();
        power.setCharge(100.0);
        power.activateAura();
        power.executePowerShot(ball, Side.LEFT);

        for (int i = 0; i < Power.TICKS_PER_POWER + Power.MAX_FLIGHT_TICKS; i++) {
            power.updateShot(ball);
        }

        assertFalse(power.isPowerShotActive());
        assertFalse(ball.hasPowerShot());
    }

    /**
     * Verifies that touching the ball with the head while aura is active executes the power shot without kicking.
     */
    @Test
    public void testPowerShotTriggersOnHeadContactWithoutKicking() {
        AuraAzulPower power = new AuraAzulPower();
        Player header = new Player("Jugador 1", Country.SOUTH_KOREA, Side.LEFT, power, new PlayerStats());
        power.setCharge(100.0);
        header.activatePower();

        Circle head = header.getHeadShape();
        Ball fallingBall = new Ball(head.getX(), head.getY() - head.getRadius() - 4.0, 8.0);
        fallingBall.setVerticalSpeed(4.0);

        header.collideWithBall(fallingBall);

        assertFalse(header.hasAura());
        assertTrue(power.isPowerShotActive());
        assertTrue(fallingBall.hasPowerShot());
    }

    /**
     * Verifies that when a power shot hits an opponent who has an active aura, the opponent
     * receives damage and immediately counter-triggers their own power shot on ball contact.
     *
     * @throws DOPOSoccerException if match configuration is invalid
     */
    @Test
    public void testPowerShotAgainstRivalWithAuraAppliesHitAndTriggersCounterShot() throws DOPOSoccerException {
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.CAMEROON)
                .matchType(MatchType.TIME)
                .targetValue(1)
                .build();
        Match match = new Match(config);
        PlayerView pLeft = match.getPlayer(Side.LEFT);
        PlayerView pRight = match.getPlayer(Side.RIGHT);

        for (int i = 0; i < 600; i++) {
            match.tick();
        }
        match.setIntention(Side.LEFT, EnumSet.of(PlayerAction.POWER));
        match.setIntention(Side.RIGHT, EnumSet.of(PlayerAction.POWER));
        match.tick();
        assertTrue(pLeft.hasAura());
        assertTrue(pRight.hasAura());

        for (int i = 0; i < 120 && pLeft.hasAura(); i++) {
            match.setIntention(Side.LEFT, EnumSet.of(PlayerAction.RIGHT, PlayerAction.KICK));
            match.tick();
        }
        assertFalse(pLeft.hasAura());

        double rightInitialLife = pRight.getLife();
        for (int i = 0; i < Power.TICKS_PER_POWER + 60 && pRight.hasAura(); i++) {
            match.setIntention(Side.LEFT, EnumSet.noneOf(PlayerAction.class));
            match.setIntention(Side.RIGHT, EnumSet.noneOf(PlayerAction.class));
            match.tick();
        }

        assertEquals("Opponent with aura must not lose life from rival power shot",
                rightInitialLife, pRight.getLife(), 0.001);
        assertFalse("Opponent's aura should trigger counter-shot on ball contact", pRight.hasAura());
        assertEquals("Opponent with aura must not receive rival status effect",
                PlayerEffect.NONE, pRight.getActiveEffect());
    }

    /**
     * Verifies BUG-15: while charging a power shot, the shooter cannot move horizontally.
     *
     * @throws DOPOSoccerException if match configuration is invalid
     */
    @Test
    public void testShooterStaysStillWhileChargingPowerShot() throws DOPOSoccerException {
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.CAMEROON)
                .matchType(MatchType.TIME)
                .targetValue(1)
                .build();
        Match match = new Match(config);
        PlayerView pLeft = match.getPlayer(Side.LEFT);
        BallView ball = match.getBall();

        for (int i = 0; i < 600; i++) {
            match.tick();
        }
        match.setIntention(Side.LEFT, EnumSet.of(PlayerAction.POWER));
        match.tick();

        for (int i = 0; i < 120 && !ball.isPowerShotCharging(); i++) {
            match.setIntention(Side.LEFT, EnumSet.of(PlayerAction.RIGHT));
            match.tick();
        }
        assertTrue("Ball should enter charging phase", ball.isPowerShotCharging());

        double startX = pLeft.getHeadShape().getX();
        for (int i = 0; i < 30; i++) {
            match.setIntention(Side.LEFT, EnumSet.of(PlayerAction.RIGHT, PlayerAction.JUMP, PlayerAction.KICK));
            match.tick();
        }
        assertEquals("Shooter should remain stationary while charging", startX, pLeft.getHeadShape().getX(), 0.001);
    }

    /**
     * Verifies BUG-11 and BUG-14: a rival with active aura cannot steal or disturb a charging power shot.
     *
     * @throws DOPOSoccerException if match configuration is invalid
     */
    @Test
    public void testRivalWithAuraCannotStealOrDisturbChargingPowerShot() throws DOPOSoccerException {
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.NIGERIA)
                .matchType(MatchType.TIME)
                .targetValue(1)
                .build();
        Match match = new Match(config);
        PlayerView pRight = match.getPlayer(Side.RIGHT);
        BallView ball = match.getBall();

        for (int i = 0; i < 600; i++) {
            match.tick();
        }
        match.setIntention(Side.LEFT, EnumSet.of(PlayerAction.POWER));
        match.setIntention(Side.RIGHT, EnumSet.of(PlayerAction.POWER));
        match.tick();

        for (int i = 0; i < 120 && !ball.isPowerShotCharging(); i++) {
            match.setIntention(Side.LEFT, EnumSet.of(PlayerAction.RIGHT));
            match.setIntention(Side.RIGHT, EnumSet.noneOf(PlayerAction.class));
            match.tick();
        }
        assertTrue("Left player should start charging power shot", ball.isPowerShotCharging());
        double holdX = ball.getX();
        double holdY = ball.getY();

        for (int i = 0; i < 50 && ball.isPowerShotCharging(); i++) {
            match.setIntention(Side.LEFT, EnumSet.noneOf(PlayerAction.class));
            match.setIntention(Side.RIGHT, EnumSet.of(PlayerAction.LEFT, PlayerAction.KICK));
            match.tick();
            assertEquals("Charging ball X should stay steady", holdX, ball.getX(), 0.001);
            assertEquals("Charging ball Y should stay steady", holdY, ball.getY(), 0.001);
        }

        assertEquals("Original shooter's country should still own the charging shot",
                Country.SOUTH_KOREA, ball.getPowerShotCountry());
        assertTrue("Rival's aura should remain unconsumed while original shot is charging", pRight.hasAura());
    }

    /**
     * Verifies BUG-16: when a player with aura touches the ball behind, above, or in front of
     * the head on either side, the charging ball is always held in front of the head toward the rival goal.
     */
    @Test
    public void testPowerShotAlwaysHoldsBallInFrontOfHeadForBothSides() {
        double[] offsetMultipliers = {-0.8, 0.0, 0.8};
        for (Side side : Side.values()) {
            for (double mult : offsetMultipliers) {
                Power power = new AuraAzulPower();
                power.setCharge(100.0);
                Player player = new Player("Shooter", Country.SOUTH_KOREA, side, power, new PlayerStats());
                player.activatePower();
                assertTrue(player.hasAura());

                Circle head = player.getHeadShape();
                double bx = head.getX() + side.dir() * (head.getRadius() * mult);
                double by = head.getY() - head.getRadius() * 0.6;
                Ball ball = new Ball(bx, by, 8.0);

                player.collideWithBall(ball);
                assertTrue("Ball should enter charging phase for " + side + " mult=" + mult,
                        ball.isPowerShotCharging());
                double forwardOffset = (ball.getX() - head.getX()) * side.dir();
                assertTrue("Charging ball must be strictly in front of head (" + side + ", mult=" + mult + ")",
                        forwardOffset > head.getRadius());
            }
        }
    }

    /**
     * Verifies IllusionPower full lifecycle: spawns 7 decoy balls during flight and clears them on hit.
     */
    @Test
    public void testIllusionPowerSpawnsSevenDecoysDuringFlightAndClearsOnHit() {
        IllusionPower illusion = new IllusionPower();
        Ball ball = new Ball(100, Field.HEIGHT - 20, 8.0);
        illusion.setCharge(100.0);
        illusion.activateAura();
        illusion.executePowerShot(ball, Side.LEFT);

        for (int i = 0; i < Power.TICKS_PER_POWER; i++) {
            illusion.updateShot(ball);
        }
        assertTrue("Illusion should be active after charging finishes", illusion.hasActiveIllusion());
        assertEquals("Illusion should spawn 7 decoy balls", 7, illusion.getDecoyBalls().size());

        illusion.updateShot(ball);
        assertEquals(7, illusion.getFakeBalls().size());

        PowerHit hit = illusion.onHitRival(1.0);
        assertNotNull(hit);
        assertEquals(30.0, hit.getDamage(), 0.001);
        assertFalse("Decoy balls must clear when shot hits rival", illusion.hasActiveIllusion());
        assertTrue(illusion.getDecoyBalls().isEmpty());
    }

    /**
     * Verifies runtime behavior of PushedEffect, ElectrocutedEffect, and ConfusedEffect on a player.
     */
    @Test
    public void testStatusEffectsPushedElectrocutedAndConfused() {
        Player pushedPlayer = new Player("P1", Country.SOUTH_KOREA, Side.LEFT, new AuraAzulPower(), new PlayerStats());
        double initialX = pushedPlayer.getHeadShape().getX();
        pushedPlayer.applyEffect(PlayerEffect.PUSHED, 1.5);
        assertEquals(PlayerEffect.PUSHED, pushedPlayer.getActiveEffect());
        pushedPlayer.tick();
        assertTrue("Pushed left player should be pushed backward toward left goal",
                pushedPlayer.getHeadShape().getX() < initialX);

        Player shockedPlayer = new Player("P2", Country.CAMEROON, Side.LEFT, new LightningPower(), new PlayerStats());
        shockedPlayer.applyEffect(PlayerEffect.ELECTROCUTED, 3.0);
        double normalStep = new PlayerStats().speedPx();
        double beforeShockX = shockedPlayer.getHeadShape().getX();
        shockedPlayer.moveRight();
        assertEquals("Electrocuted player should move at half speed",
                normalStep * 0.5, shockedPlayer.getHeadShape().getX() - beforeShockX, 0.01);
        double beforeJumpY = shockedPlayer.getHeadShape().getY();
        shockedPlayer.jump();
        shockedPlayer.tick();
        assertEquals("Electrocuted player must not jump", beforeJumpY, shockedPlayer.getHeadShape().getY(), 0.01);

        Player confusedPlayer = new Player("P3", Country.USA, Side.LEFT, new IllusionPower(), new PlayerStats());
        confusedPlayer.applyEffect(PlayerEffect.CONFUSED, 2.5);
        double beforeConfX = confusedPlayer.getHeadShape().getX();
        confusedPlayer.moveRight();
        assertTrue("Confused player moving right should actually move left",
                confusedPlayer.getHeadShape().getX() < beforeConfX);
    }

    /**
     * Verifies incrementing all four statistics up to level 10 and rejecting null or capped increments.
     *
     * @throws DOPOSoccerException if an unexpected validation error occurs
     */
    @Test
    public void testPlayerStatsIncrementAllFourAndEnforceCap() throws DOPOSoccerException {
        PlayerStats stats = new PlayerStats();
        for (StatType type : StatType.values()) {
            for (int level = 2; level <= 10; level++) {
                stats.increment(type);
            }
        }
        assertEquals(10, stats.getSpeed());
        assertEquals(10, stats.getJump());
        assertEquals(10, stats.getKick());
        assertEquals(10, stats.getSurvival());

        for (StatType type : StatType.values()) {
            try {
                stats.increment(type);
                fail("Expected MAX_STAT_REACHED for " + type);
            } catch (DOPOSoccerException ex) {
                assertEquals(DOPOSoccerException.MAX_STAT_REACHED, ex.getMessage());
            }
        }

        try {
            stats.increment(null);
            fail("Expected NULL_ARGUMENT for null stat type");
        } catch (DOPOSoccerException ex) {
            assertEquals(DOPOSoccerException.NULL_ARGUMENT, ex.getMessage());
        }
    }

    /**
     * Verifies Controller's default receive method is safely ignored by autonomous machines.
     */
    @Test
    public void testControllerDefaultReceiveIsIgnoredByMachine() {
        Controller machine = MachineProfile.DEFENSIVA.create(Side.RIGHT);
        machine.receive(EnumSet.of(PlayerAction.KICK, PlayerAction.JUMP));
        assertTrue(machine.decide(null).isEmpty());
    }

    /**
     * Verifies MatchRule's default winner method returns empty while in progress and the leader when over.
     *
     * @throws Exception if match setup or reflection fails
     */
    @Test
    public void testMatchRuleDefaultWinnerForTimeMatch() throws Exception {
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.CAMEROON)
                .matchType(MatchType.TIME)
                .targetValue(1)
                .build();
        Match match = new Match(config);
        assertEquals(Optional.empty(), match.getWinner());

        Rectangle rightGoal = match.getGoalArea(Side.RIGHT);
        java.lang.reflect.Field ballField = Match.class.getDeclaredField("ball");
        ballField.setAccessible(true);
        Ball rawBall = (Ball) ballField.get(match);
        rawBall.reset(rightGoal.getX() + rightGoal.getWidth() / 2.0, rightGoal.getY() + rightGoal.getHeight() / 2.0);
        match.tick();

        for (int t = 0; t < Physics.seconds(65) && match.getStatus() != GameStatus.FINISHED; t++) {
            match.tick();
        }
        assertEquals(GameStatus.FINISHED, match.getStatus());
        assertEquals(Optional.of(Side.LEFT), match.getWinner());
    }

    /**
     * Verifies all eight default no-op methods of MatchListener execute safely without overrides.
     */
    @Test
    public void testMatchListenerDefaultMethodsHandleAllEventsSafely() {
        MatchListener empty = new MatchListener() { };
        empty.onGoal(Side.LEFT);
        empty.onStunned(Side.RIGHT);
        empty.onMatchFinished(Side.LEFT, 100, 25);
        empty.onBallKicked(Side.LEFT);
        empty.onPowerActivated(Side.LEFT);
        empty.onPowerShot(Side.LEFT, Country.SOUTH_KOREA);
        empty.onPowerHit(Side.RIGHT, 40.0);
        empty.onProfileSaveFailed("error");
    }

    /**
     * Verifies Effect interface default methods across all status effect implementations.
     */
    @Test
    public void testEffectDefaultMethodsAcrossAllImplementations() {
        PlayerBody body = new PlayerBody(Side.LEFT);
        for (PlayerEffect type : PlayerEffect.values()) {
            Effect eff = type.createEffect();
            assertEquals(type, eff.getType());
            eff.onStart(body);
            eff.onTick(body, Side.LEFT);
            eff.canMove();
            eff.canJump();
            eff.canKick();
            eff.isConfused();
            assertTrue(eff.speedFactor() > 0.0);
        }
    }

    /**
     * Verifies BUG-18: while a power shot is charging, an opponent walking into the ball
     * from either side cannot pass through or remain overlapping the charging ball.
     *
     * @throws DOPOSoccerException if match configuration is invalid
     */
    @Test
    public void testRivalCannotWalkThroughChargingBallFromEitherSide() throws DOPOSoccerException {
        for (Side shooterSide : Side.values()) {
            Side rivalSide = shooterSide.opposite();
            MatchConfig config = MatchConfig.builder()
                    .modality(Modality.PVP)
                    .leftCountry(Country.SOUTH_KOREA)
                    .rightCountry(Country.CAMEROON)
                    .matchType(MatchType.TIME)
                    .targetValue(1)
                    .build();
            Match match = new Match(config);
            BallView ball = match.getBall();
            PlayerView rival = match.getPlayer(rivalSide);

            for (int i = 0; i < 600; i++) {
                match.tick();
            }
            match.setIntention(shooterSide, EnumSet.of(PlayerAction.POWER));
            match.tick();

            PlayerAction shooterWalk = (shooterSide == Side.LEFT) ? PlayerAction.RIGHT : PlayerAction.LEFT;
            for (int i = 0; i < 120 && !ball.isPowerShotCharging(); i++) {
                match.setIntention(shooterSide, EnumSet.of(shooterWalk));
                match.setIntention(rivalSide, EnumSet.noneOf(PlayerAction.class));
                match.tick();
            }
            assertTrue("Shooter should start charging power shot for " + shooterSide, ball.isPowerShotCharging());

            double ballX = ball.getX();
            PlayerAction rivalWalk = (rivalSide == Side.LEFT) ? PlayerAction.RIGHT : PlayerAction.LEFT;
            for (int i = 0; i < Power.TICKS_PER_POWER - 5 && ball.isPowerShotCharging(); i++) {
                match.setIntention(shooterSide, EnumSet.noneOf(PlayerAction.class));
                match.setIntention(rivalSide, EnumSet.of(rivalWalk));
                match.tick();

                double rivalHeadX = rival.getHeadShape().getX();
                if (rivalSide == Side.RIGHT) {
                    assertTrue("Right rival must not cross behind left-charging ball", rivalHeadX > ballX);
                } else {
                    assertTrue("Left rival must not cross behind right-charging ball", rivalHeadX < ballX);
                }
            }
        }
    }

    /**
     * Verifies that a power shot hitting a rival protected by an active aura does not
     * notify onPowerHit, because the rival takes no damage or effect.
     *
     * @throws DOPOSoccerException if match configuration is invalid
     */
    @Test
    public void testPowerShotOnRivalWithAuraDoesNotNotifyPowerHit() throws DOPOSoccerException {
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.CAMEROON)
                .matchType(MatchType.TIME)
                .targetValue(1)
                .build();
        Match match = new Match(config);
        PlayerView pLeft = match.getPlayer(Side.LEFT);
        PlayerView pRight = match.getPlayer(Side.RIGHT);
        AtomicInteger hitsOnRight = new AtomicInteger();
        match.addListener(new MatchListener() {
            @Override
            public void onPowerHit(Side victim, double damage) {
                if (victim == Side.RIGHT) {
                    hitsOnRight.incrementAndGet();
                }
            }
        });

        for (int i = 0; i < 600; i++) {
            match.tick();
        }
        match.setIntention(Side.LEFT, EnumSet.of(PlayerAction.POWER));
        match.setIntention(Side.RIGHT, EnumSet.of(PlayerAction.POWER));
        match.tick();
        for (int i = 0; i < 120 && pLeft.hasAura(); i++) {
            match.setIntention(Side.LEFT, EnumSet.of(PlayerAction.RIGHT, PlayerAction.KICK));
            match.tick();
        }
        for (int i = 0; i < Power.TICKS_PER_POWER + 60 && pRight.hasAura(); i++) {
            match.setIntention(Side.LEFT, EnumSet.noneOf(PlayerAction.class));
            match.setIntention(Side.RIGHT, EnumSet.noneOf(PlayerAction.class));
            match.tick();
        }

        assertFalse("Right player should have used its aura on the counter-shot", pRight.hasAura());
        assertEquals("No onPowerHit must be notified for a rival protected by its aura", 0, hitsOnRight.get());
    }
}
