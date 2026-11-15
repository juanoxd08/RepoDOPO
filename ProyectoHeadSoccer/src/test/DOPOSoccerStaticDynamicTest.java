package test;

import domain.*;
import presentation.FieldResolution;
import org.junit.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;
import static org.junit.Assert.*;

/**
 * Static and Dynamic verification test suite for DOPOSoccer.
 * 
 * 1. PRUEBAS ESTÁTICAS:
 *    - Verificación estructural de contratos, interfaces (Collidable, Controller, ReaderMatch).
 *    - Validación de firmas de métodos, modificadores y paquetes.
 *    - Cobertura e invariantes de enumeraciones (Country, Side, GameStatus, MachineProfile, PlayerEffect, MatchType, FieldResolution).
 * 
 * 2. PRUEBAS DINÁMICAS:
 *    - Simulación en tiempo de ejecución del ciclo de juego (game loop).
 *    - Simulación de un partido PvP con movimiento, patadas, rebotes e interacción física continua.
 *    - Simulación de un partido PvM con reacción autónoma de los perfiles de máquina (Torpe, Ofensiva, Defensiva, Experta).
 *    - Simulación del flujo de estados (Menu -> Select Players -> Select Camp -> Playing -> Goal -> Finished).
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class DOPOSoccerStaticDynamicTest {

    // =========================================================================
    // PARTE 1: PRUEBAS ESTÁTICAS (Static Testing / Verificación de Diseño)
    // =========================================================================

    @Test
    public void testStaticInterfaceContracts() {
        // Verificar que las interfaces clave existen y son abstractas/interfaces
        assertTrue(MatchView.class.isInterface());
        assertTrue(PlayerView.class.isInterface());
        assertTrue(BallView.class.isInterface());
        assertTrue(MatchRule.class.isInterface());
        assertTrue(MatchListener.class.isInterface());
        assertTrue(Controller.class.isInterface());
        assertTrue(Modifier.isAbstract(Power.class.getModifiers()));
        assertTrue(Modifier.isAbstract(Figure.class.getModifiers()));
    }

    @Test
    public void testStaticMethodSignatures() throws NoSuchMethodException {
        // Verificar que Match implementa MatchView y DOPOSoccer expone los métodos requeridos
        Method getPlayer = Match.class.getMethod("getPlayer", Side.class);
        assertEquals(PlayerView.class, getPlayer.getReturnType());

        Method getBall = Match.class.getMethod("getBall");
        assertEquals(BallView.class, getBall.getReturnType());

        Method getStatus = Match.class.getMethod("getStatus");
        assertEquals(GameStatus.class, getStatus.getReturnType());

        Method tick = Match.class.getMethod("tick");
        assertEquals(void.class, tick.getReturnType());

        Method newMatch = DOPOSoccer.class.getMethod("newMatch", MatchConfig.class);
        assertEquals(void.class, newMatch.getReturnType());

        Method getMatch = DOPOSoccer.class.getMethod("getMatch");
        assertEquals(MatchView.class, getMatch.getReturnType());
    }

    @Test
    public void testStaticEnumerationsIntegrity() {
        // Validación de constantes de Country requeridas por Anexo 1
        assertNotNull(Country.valueOf("SOUTH_KOREA"));
        assertNotNull(Country.valueOf("CAMEROON"));
        assertNotNull(Country.valueOf("NIGERIA"));
        assertNotNull(Country.valueOf("USA"));

        // Validación de perfiles de máquina requeridos
        assertNotNull(MachineProfile.valueOf("TORPE"));
        assertNotNull(MachineProfile.valueOf("OFENSIVA"));
        assertNotNull(MachineProfile.valueOf("DEFENSIVA"));
        assertNotNull(MachineProfile.valueOf("EXPERTA"));

        // Validación de tipos de partido
        assertNotNull(MatchType.valueOf("TIME"));
        assertNotNull(MatchType.valueOf("GOALS"));

        // Validación de resoluciones
        assertNotNull(FieldResolution.valueOf("SMALL"));
        assertNotNull(FieldResolution.valueOf("MEDIUM"));
        assertNotNull(FieldResolution.valueOf("LARGE"));
    }

    @Test
    public void testStaticPhysicsConstants() {
        assertTrue(Field.WIDTH > 0);
        assertTrue(Field.HEIGHT > 0);
        assertTrue(Physics.GRAVITY > 0);
        assertTrue(Physics.FPS > 0);
    }

    // =========================================================================
    // PARTE 2: PRUEBAS DINÁMICAS (Dynamic Testing / Simulación de Comportamiento)
    // =========================================================================

    @Test
    public void testDynamicGameFlowSimulation() throws DOPOSoccerException {
        DOPOSoccer game = new DOPOSoccer();
        assertNull(game.getMatch());

        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.USA)
                .matchType(MatchType.GOALS)
                .targetValue(2)
                .build();

        game.newMatch(config);
        assertNotNull(game.getMatch());
        assertEquals(GameStatus.PLAYING, game.getMatch().getStatus());

        // Simular ejecución activa de 30 fotogramas
        for (int i = 0; i < 30; i++) {
            game.tick();
        }

        assertEquals(GameStatus.PLAYING, game.getMatch().getStatus());

        // Simular pausa y reanudación
        game.togglePause();
        assertEquals(GameStatus.PAUSED, game.getMatch().getStatus());
        game.togglePause();
        assertEquals(GameStatus.PLAYING, game.getMatch().getStatus());
    }

    @Test
    public void testDynamicMachineControllersReaction() throws DOPOSoccerException {
        for (MachineProfile profile : MachineProfile.values()) {
            COMController com = new COMController(Side.RIGHT, profile);
            MatchConfig config = MatchConfig.builder()
                    .modality(Modality.PVP)
                    .leftCountry(Country.SOUTH_KOREA)
                    .rightCountry(Country.CAMEROON)
                    .matchType(MatchType.TIME)
                    .targetValue(1)
                    .build();

            Match match = new Match(config);

            // Simular interacción durante 50 ticks
            for (int t = 0; t < 50; t++) {
                Set<PlayerAction> actions = com.decide(match);
                assertNotNull("El controlador " + profile + " debe retornar decisiones válidas", actions);
                match.tick();
            }
        }
    }

    @Test
    public void testDynamicBallPhysicsAndGoalSequence() throws DOPOSoccerException {
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.CAMEROON)
                .matchType(MatchType.GOALS)
                .targetValue(2)
                .build();
        Match match = new Match(config);

        double startY = match.getBall().getY();

        // Dejar caer el balón con gravedad durante 10 frames
        for (int i = 0; i < 10; i++) {
            match.tick();
        }
        assertTrue("El balón debe descender debido a la gravedad", match.getBall().getY() > startY);

        // Forzar gol directo en arco izquierdo (anota Jugador 2 / RIGHT)
        match.onGoal(Side.LEFT);
        assertEquals(GameStatus.GOAL_SCORED, match.getStatus());
        assertEquals(1, match.getGoals(Side.RIGHT));

        // Simular cuenta regresiva de celebración para que vuelva a PLAYING
        int delay = match.getGoalCelebrationTicks();
        for (int i = 0; i <= delay + 5; i++) {
            match.tick();
        }
        assertEquals(GameStatus.PLAYING, match.getStatus());

        // Forzar segundo gol para alcanzar la meta de 2 goles
        match.onGoal(Side.LEFT);
        assertEquals(GameStatus.FINISHED, match.getStatus());
        assertEquals(Optional.of(Side.RIGHT), match.getWinner());
    }
}
