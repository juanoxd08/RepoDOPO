package test;

import domain.*;
import org.junit.Before;
import org.junit.Test;
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
    public void setUp() {
        p1 = new Player("Jugador 1", Country.SOUTH_KOREA, Side.LEFT, new AuraAzulPower(), new PlayerStats(1, 1, 1, 1));
        p2 = new Player("Jugador 2", Country.CAMEROON, Side.RIGHT, new LightningPower(), new PlayerStats(1, 1, 1, 1));
        ball = new Ball(Field.WIDTH / 2.0, 50.0, 10.0);
        field = new Field();
    }

    /**
     * Verifies the survival maximum health formula across levels 1 to 10.
     */
    @Test
    public void testSurvivalFormula() {
        PlayerStats stats1 = new PlayerStats(1, 1, 1, 1);
        assertEquals(100.0, stats1.getVidaMax(), 0.001);

        PlayerStats stats3 = new PlayerStats(1, 1, 1, 3);
        assertEquals(120.0, stats3.getVidaMax(), 0.001);

        PlayerStats stats5 = new PlayerStats(1, 1, 1, 5);
        assertEquals(140.0, stats5.getVidaMax(), 0.001);

        PlayerStats stats10 = new PlayerStats(1, 1, 1, 10);
        assertEquals(190.0, stats10.getVidaMax(), 0.001);
    }

    /**
     * Verifies the kick damage multiplier formula across levels 1 to 10.
     */
    @Test
    public void testKickMultiplierFormula() {
        PlayerStats stats1 = new PlayerStats(1, 1, 1, 1);
        assertEquals(1.0, stats1.getMultiplicadorKick(), 0.001);

        PlayerStats stats5 = new PlayerStats(1, 1, 5, 1);
        assertEquals(1.4, stats5.getMultiplicadorKick(), 0.001);

        PlayerStats stats10 = new PlayerStats(1, 1, 10, 1);
        assertEquals(1.9, stats10.getMultiplicadorKick(), 0.001);
    }

    /**
     * Verifies normal kick damage computation from kick level.
     */
    @Test
    public void testKickDamageFormula() {
        PlayerStats stats1 = new PlayerStats(1, 1, 1, 1);
        assertEquals(10.0, stats1.getDanioPatada(), 0.001);

        PlayerStats stats5 = new PlayerStats(1, 1, 5, 1);
        assertEquals(14.0, stats5.getDanioPatada(), 0.001);
    }

    /**
     * Verifies that setting a statistic below 1 throws an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testStatsBoundsValidationBelowMin() {
        PlayerStats stats = new PlayerStats();
        stats.setKick(0);
    }

    /**
     * Verifies that setting a statistic above 10 throws an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testStatsBoundsValidationAboveMax() {
        PlayerStats stats = new PlayerStats();
        stats.setSurvival(11);
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
    public void testKoreaAuraAzulPushesOpponent() {
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
     */
    @Test
    public void testCameroonLightningElectrocutesOpponent() {
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
     */
    @Test
    public void testNigeriaTornadoKnocksOutOpponent() {
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
     */
    @Test
    public void testUSAIllusionDividesBallIntoEight() {
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
    public void testMatchAwardsPointsOnCompletion() throws DOPOSoccerException {
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.CAMEROON)
                .matchType(MatchType.GOALS)
                .targetValue(1)
                .build();
        Match match = new Match(config);

        match.onGoal(Side.RIGHT);

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
}
