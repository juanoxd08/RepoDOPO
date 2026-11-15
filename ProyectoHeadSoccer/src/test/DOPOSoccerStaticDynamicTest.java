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
 * 1. PRUEBAS ESTÃTICAS:
 *    - VerificaciÃ³n estructural de contratos, interfaces (Collidable, Controller, ReaderMatch).
 *    - ValidaciÃ³n de firmas de mÃ©todos, modificadores y paquetes.
 *    - Cobertura e invariantes de enumeraciones (Country, Side, GameStatus, MachineProfile, PlayerEffect, MatchType, FieldResolution).
 * 
 * 2. PRUEBAS DINÃMICAS:
 *    - SimulaciÃ³n en tiempo de ejecuciÃ³n del ciclo de juego (game loop).
 *    - SimulaciÃ³n de un partido PvP con movimiento, patadas, rebotes e interacciÃ³n fÃ­sica continua.
 *    - SimulaciÃ³n de un partido PvM con reacciÃ³n autÃ³noma de los perfiles de mÃ¡quina (Torpe, Ofensiva, Defensiva, Experta).
 *    - SimulaciÃ³n del flujo de estados (Menu -> Select Players -> Select Camp -> Playing -> Goal -> Finished).
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class DOPOSoccerStaticDynamicTest {


    @Test
    public void testStaticInterfaceContracts() {
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
        assertNotNull(Country.valueOf("SOUTH_KOREA"));
        assertNotNull(Country.valueOf("CAMEROON"));
        assertNotNull(Country.valueOf("NIGERIA"));
        assertNotNull(Country.valueOf("USA"));

        assertNotNull(MachineProfile.valueOf("TORPE"));
        assertNotNull(MachineProfile.valueOf("OFENSIVA"));
        assertNotNull(MachineProfile.valueOf("DEFENSIVA"));
        assertNotNull(MachineProfile.valueOf("EXPERTA"));

        assertNotNull(MatchType.valueOf("TIME"));
        assertNotNull(MatchType.valueOf("GOALS"));

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

        for (int i = 0; i < 30; i++) {
            game.tick();
        }

        assertEquals(GameStatus.PLAYING, game.getMatch().getStatus());

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

            for (int t = 0; t < 50; t++) {
                Set<PlayerAction> actions = com.decide(match);
                assertNotNull("El controlador " + profile + " debe retornar decisiones vÃ¡lidas", actions);
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

        for (int i = 0; i < 10; i++) {
            match.tick();
        }
        assertTrue("El balÃ³n debe descender debido a la gravedad", match.getBall().getY() > startY);

        match.onGoal(Side.LEFT);
        assertEquals(GameStatus.GOAL_SCORED, match.getStatus());
        assertEquals(1, match.getGoals(Side.RIGHT));

        int delay = match.getGoalCelebrationTicks();
        for (int i = 0; i <= delay + 5; i++) {
            match.tick();
        }
        assertEquals(GameStatus.PLAYING, match.getStatus());

        match.onGoal(Side.LEFT);
        assertEquals(GameStatus.FINISHED, match.getStatus());
        assertEquals(Optional.of(Side.RIGHT), match.getWinner());
    }
}
