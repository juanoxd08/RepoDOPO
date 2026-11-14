package test;

import domain.*;
import org.junit.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
        assertTrue(Collidable.class.isInterface());
        assertTrue(Controller.class.isInterface());
        assertTrue(ReaderMatch.class.isInterface());
        assertTrue(Modifier.isAbstract(Power.class.getModifiers()));
        assertTrue(Modifier.isAbstract(Figure.class.getModifiers()));
    }

    @Test
    public void testStaticMethodSignatures() throws NoSuchMethodException {
        // Verificar que Match implementa ReaderMatch y contiene los métodos requeridos
        Method getPlayer1 = Match.class.getMethod("getPlayer1");
        assertEquals(Player.class, getPlayer1.getReturnType());

        Method getBall = Match.class.getMethod("getBall");
        assertEquals(Ball.class, getBall.getReturnType());

        Method getScoreBoard = Match.class.getMethod("getScoreBoard");
        assertEquals(ScoreBoard.class, getScoreBoard.getReturnType());

        Method tick = Match.class.getMethod("tick");
        assertEquals(void.class, tick.getReturnType());
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
    public void testDynamicGameFlowSimulation() {
        HeadSoccer game = new HeadSoccer();
        assertEquals(GameStatus.MENU, game.getState());

        // Transición 1: Iniciar configuración
        game.startNewGameFlow();
        assertEquals(GameStatus.SELECT_PLAYERS, game.getState());

        // Configuración de partido
        game.setVsMachine(true);
        game.setMachineProfile(MachineProfile.OFENSIVA);
        game.setPlayer1Country(Country.SOUTH_KOREA);
        game.setPlayer2Country(Country.USA);
        game.setMatchType(MatchType.GOALS);
        game.setTargetGoals(2);

        // Transición 2: Continuar a selección de campo
        game.confirmPlayersAndProceedToCamp();
        assertEquals(GameStatus.SELECT_CAMP, game.getState());

        // Selección de resolución
        game.setResolution(FieldResolution.MEDIUM);

        // Transición 3: Iniciar partido activo
        game.startActiveMatch();
        assertEquals(GameStatus.PLAYING, game.getState());
        assertNotNull(game.getCurrentMatch());

        // Simular ejecución activa de 30 fotogramas
        for (int i = 0; i < 30; i++) {
            game.tick();
        }

        assertEquals(GameStatus.PLAYING, game.getState());

        // Simular pausa y reanudación
        game.togglePause();
        assertEquals(GameStatus.PAUSED, game.getState());
        game.togglePause();
        assertEquals(GameStatus.PLAYING, game.getState());
    }

    @Test
    public void testDynamicMachineControllersReaction() {
        for (MachineProfile profile : MachineProfile.values()) {
            Player p1 = new Player("Humano", Country.SOUTH_KOREA, Side.LEFT, new AuraAzulPower(), new PlayerStats());
            Player p2 = new Player("Bot", Country.CAMEROON, Side.RIGHT, new LightningPower(), new PlayerStats());
            COMController com = new COMController(Side.RIGHT, profile);

            Match match = new Match(p1, p2, new HumanController(), com, MatchType.TIME, 60, 3);

            // Simular interacción durante 50 ticks
            for (int t = 0; t < 50; t++) {
                Set<PlayerAction> actions = com.decide(match);
                assertNotNull("El controlador " + profile + " debe retornar decisiones válidas", actions);
                match.tick();
            }
        }
    }

    @Test
    public void testDynamicBallPhysicsAndGoalSequence() {
        Player p1 = new Player("Jugador 1", Country.SOUTH_KOREA, Side.LEFT, new AuraAzulPower(), new PlayerStats());
        Player p2 = new Player("Jugador 2", Country.CAMEROON, Side.RIGHT, new LightningPower(), new PlayerStats());
        Match match = new Match(p1, p2, new HumanController(), new HumanController(), MatchType.GOALS, 60, 2);

        Ball ball = match.getBall();
        double startY = ball.getCircle().getY();

        // Dejar caer el balón con gravedad durante 10 frames
        for (int i = 0; i < 10; i++) {
            ball.tick();
        }
        assertTrue("El balón debe descender debido a la gravedad", ball.getCircle().getY() > startY);

        // Forzar gol directo en arco izquierdo (anota Jugador 2)
        match.onGoal(Side.LEFT);
        assertEquals(GameStatus.GOAL_SCORED, match.getState());
        assertEquals(1, match.getScoreBoard().getPoints()[1]);

        // Simular cuenta regresiva de celebración para que vuelva a PLAYING
        int delay = match.getGoalCelebrationTicks();
        for (int i = 0; i <= delay + 5; i++) {
            match.tick();
        }
        assertEquals(GameStatus.PLAYING, match.getState());

        // Forzar segundo gol para alcanzar la meta de 2 goles
        match.onGoal(Side.LEFT);
        assertEquals(GameStatus.FINISHED, match.getState());
        assertEquals(p2, match.getScoreBoard().getWinner());
    }
}
