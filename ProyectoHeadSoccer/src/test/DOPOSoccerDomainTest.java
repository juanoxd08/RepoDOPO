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
 * - All 4 country powers from Anexo 1 (Aura Azul, Rayo, Tornado, Ilusión).
 * - Match scoring, sudden death (golden goal), and end-of-match point distribution.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class DOPOSoccerDomainTest {

    private Player p1;
    private Player p2;
    private Ball ball;
    private Field field;

    @Before
    public void setUp() {
        p1 = new Player("Jugador 1", Country.SOUTH_KOREA, Side.LEFT, new AuraAzulPower(), new PlayerStats(1, 1, 1, 1));
        p2 = new Player("Jugador 2", Country.CAMEROON, Side.RIGHT, new LightningPower(), new PlayerStats(1, 1, 1, 1));
        ball = new Ball(Field.WIDTH / 2.0, 50.0, 10.0);
        field = new Field();
    }

    // =========================================================================
    // 1. PRUEBAS DE ESTADÍSTICAS Y FÓRMULAS MATEMÁTICAS (ANEXO 2)
    // =========================================================================

    @Test
    public void testSurvivalFormula() {
        // vidaMax = 100 * (1 + 0.10 * (Survival - 1))
        PlayerStats stats1 = new PlayerStats(1, 1, 1, 1);
        assertEquals(100.0, stats1.getVidaMax(), 0.001);

        PlayerStats stats3 = new PlayerStats(1, 1, 1, 3);
        assertEquals(120.0, stats3.getVidaMax(), 0.001);

        PlayerStats stats5 = new PlayerStats(1, 1, 1, 5);
        assertEquals(140.0, stats5.getVidaMax(), 0.001);

        PlayerStats stats10 = new PlayerStats(1, 1, 1, 10);
        assertEquals(190.0, stats10.getVidaMax(), 0.001);
    }

    @Test
    public void testKickMultiplierFormula() {
        // multiplicadorKick = 1 + 0.10 * (Kick - 1)
        PlayerStats stats1 = new PlayerStats(1, 1, 1, 1);
        assertEquals(1.0, stats1.getMultiplicadorKick(), 0.001);

        PlayerStats stats5 = new PlayerStats(1, 1, 5, 1);
        assertEquals(1.4, stats5.getMultiplicadorKick(), 0.001);

        PlayerStats stats10 = new PlayerStats(1, 1, 10, 1);
        assertEquals(1.9, stats10.getMultiplicadorKick(), 0.001);
    }

    @Test
    public void testKickDamageFormula() {
        // danioPatada = 10 * multiplicadorKick
        PlayerStats stats1 = new PlayerStats(1, 1, 1, 1);
        assertEquals(10.0, stats1.getDanioPatada(), 0.001);

        PlayerStats stats5 = new PlayerStats(1, 1, 5, 1);
        assertEquals(14.0, stats5.getDanioPatada(), 0.001);
    }

    @Test(expected = HeadSoccerException.class)
    public void testStatsBoundsValidationBelowMin() throws HeadSoccerException {
        PlayerStats stats = new PlayerStats();
        stats.setKick(0);
    }

    @Test(expected = HeadSoccerException.class)
    public void testStatsBoundsValidationAboveMax() throws HeadSoccerException {
        PlayerStats stats = new PlayerStats();
        stats.setSurvival(11);
    }

    // =========================================================================
    // 2. PRUEBAS DE SALUD, DAÑO Y ATURDIMIENTO
    // =========================================================================

    @Test
    public void testPlayerTakesDamage() {
        double initialHp = p1.getVidaMax();
        p1.takeDamage(30.0);
        assertEquals(initialHp - 30.0, p1.getVidaActual(), 0.001);
    }

    @Test
    public void testHealthCannotDropBelowZero() {
        p1.takeDamage(200.0);
        assertEquals(0.0, p1.getVidaActual(), 0.001);
    }

    @Test
    public void testPlayerStunnedWhenHealthReachesZero() {
        p1.takeDamage(p1.getVidaMax());
        assertTrue(p1.isStunnedByZeroHealth());
        assertTrue(p1.isDisabled());
    }

    @Test
    public void testPlayerRecoversFullHealthAfterStunDuration() {
        p1.takeDamage(p1.getVidaMax());
        assertTrue(p1.isStunnedByZeroHealth());

        // Simular paso de 3 segundos (180 ticks a 60 FPS)
        for (int i = 0; i < 185; i++) {
            p1.moveVertical();
        }

        assertFalse(p1.isStunnedByZeroHealth());
        assertEquals(p1.getVidaMax(), p1.getVidaActual(), 0.001);
        assertFalse(p1.isDisabled());
    }

    // =========================================================================
    // 3. PRUEBAS DE PODERES Y REGLAS DE RECARGA
    // =========================================================================

    @Test
    public void testPowerChargesOverTime() {
        Power pow = p1.getPower();
        pow.setCharge(0.0);

        for (int i = 0; i < 60; i++) {
            pow.tickCharge();
        }

        assertTrue(pow.getCharge() > 0.0);
    }

    @Test
    public void testPowerGainsOneQuarterOnReceivingGoal() {
        Power pow = p1.getPower();
        pow.setCharge(20.0);

        p1.onGoalReceived();
        assertEquals(45.0, pow.getCharge(), 0.001);
    }

    @Test
    public void testActivatingPowerDrainsBarAndStartsEnduringAura() {
        Power pow = p1.getPower();
        pow.setCharge(100.0);

        p1.activatePower();

        assertTrue(pow.isAuraActive());
        assertEquals(0.0, pow.getCharge(), 0.001);
    }

    @Test
    public void testKoreaAuraAzulPushesOpponent() {
        AuraAzulPower koreaPower = new AuraAzulPower();
        p1.setPower(koreaPower);
        koreaPower.setCharge(100.0);
        p1.activatePower();

        // Al tocar el balón se libera el tiro
        koreaPower.executePowerShot(ball, p1);
        assertTrue(koreaPower.isPowerShotActive());

        // Al impactar al rival
        koreaPower.onHitRival(p2, p1);
        assertEquals(PlayerEffect.PUSHED, p2.getCurrentEffect());
        assertTrue(p2.getVidaActual() < p2.getVidaMax());
    }

    @Test
    public void testCameroonLightningElectrocutesOpponent() {
        LightningPower lightning = new LightningPower();
        p2.setPower(lightning);
        lightning.setCharge(100.0);
        p2.activatePower();

        lightning.executePowerShot(ball, p2);
        lightning.onHitRival(p1, p2);

        assertEquals(PlayerEffect.ELECTROCUTED, p1.getCurrentEffect());
    }

    @Test
    public void testNigeriaTornadoKnocksOutOpponent() {
        TornadoPower tornado = new TornadoPower();
        p1.setPower(tornado);
        tornado.setCharge(100.0);
        p1.activatePower();

        tornado.executePowerShot(ball, p1);
        tornado.onHitRival(p2, p1);

        assertEquals(PlayerEffect.KNOCKED_OUT, p2.getCurrentEffect());
        assertTrue(p2.isDisabled());
    }

    @Test
    public void testUSAIllusionDividesBallIntoEight() {
        IllusionPower illusion = new IllusionPower();
        p1.setPower(illusion);
        illusion.setCharge(100.0);
        p1.activatePower();

        illusion.executePowerShot(ball, p1);

        // 7 falsos + 1 real = 8 balones
        assertEquals(7, illusion.getFakeBalls().size());
        assertTrue(illusion.hasActiveIllusion());
    }

    @Test
    public void testNewPowerEffectReplacesPreviousEffect() {
        p2.applyEffect(PlayerEffect.ELECTROCUTED, 3.0);
        assertEquals(PlayerEffect.ELECTROCUTED, p2.getCurrentEffect());

        // Nuevo efecto reemplaza inmediatamente
        p2.applyEffect(PlayerEffect.PUSHED, 1.5);
        assertEquals(PlayerEffect.PUSHED, p2.getCurrentEffect());
    }

    // =========================================================================
    // 4. PRUEBAS DE PARTIDO, GOL DE ORO Y PUNTOS
    // =========================================================================

    @Test
    public void testMatchAwardsPointsOnCompletion() {
        Match match = new Match(p1, p2, new HumanController(), new HumanController(), MatchType.GOALS, 60, 1);
        
        // Simular gol de Player 1
        match.onGoal(Side.RIGHT);

        assertEquals(GameStatus.FINISHED, match.getState());
        assertEquals(100, p1.getPoints());
        assertEquals(25, p2.getPoints());
    }

    @Test
    public void testSuddenDeathActivatesOnTimeTie() {
        Match match = new Match(p1, p2, new HumanController(), new HumanController(), MatchType.TIME, 1, 3);
        match.getTimer().setTimeRemaining(0);

        match.tick();

        assertEquals(GameStatus.SUDDEN_DEATH, match.getState());
    }
}
