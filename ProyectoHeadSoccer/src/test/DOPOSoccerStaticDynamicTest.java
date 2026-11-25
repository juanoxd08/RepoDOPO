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
 * Static and dynamic verification test suite for DOPOSoccer.
 *
 * 1. STATIC VERIFICATION:
 *    - Structural validation of domain interfaces, contracts, method signatures, and modifiers.
 *    - Enumeration coverage and invariants across Country, Side, GameStatus, MachineProfile,
 *      PlayerEffect, MatchType, and FieldResolution.
 *
 * 2. DYNAMIC VERIFICATION:
 *    - Runtime simulation of the game loop in both PvP and PvM modalities.
 *    - Autonomous machine profile execution and match lifecycle state transitions.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class DOPOSoccerStaticDynamicTest {


    /**
     * Verifies structural interface and abstract class contracts in the domain.
     */
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

    /**
     * Verifies public method signatures on Match and DOPOSoccer facade.
     *
     * @throws NoSuchMethodException if an expected method signature is missing
     */
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

    /**
     * Verifies enumeration constants across Country, MachineProfile, MatchType, and FieldResolution.
     */
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

    /**
     * Verifies positive invariants for field dimensions and physics constants.
     */
    @Test
    public void testStaticPhysicsConstants() {
        assertTrue(Field.WIDTH > 0);
        assertTrue(Field.HEIGHT > 0);
        assertTrue(Physics.GRAVITY > 0);
        assertTrue(Physics.FPS > 0);
    }

    /**
     * Simulates match creation, frame progression, and pause/resume transitions through DOPOSoccer.
     *
     * @throws DOPOSoccerException if match configuration is invalid
     */
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

    /**
     * Verifies that all machine profiles produce valid action sets during an active match simulation.
     *
     * @throws DOPOSoccerException if match configuration is invalid
     */
    @Test
    public void testDynamicMachineControllersReaction() throws DOPOSoccerException {
        for (MachineProfile profile : MachineProfile.values()) {
            MachineController com = profile.create(Side.RIGHT);
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
                assertNotNull("El controlador " + profile + " debe retornar decisiones validas", actions);
                match.tick();
            }
        }
    }

    /**
     * Simulates gravity, goal celebration delay, kickoff reset, and match completion on target score.
     *
     * @throws DOPOSoccerException if match configuration is invalid
     */
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
        assertTrue("El balon debe descender debido a la gravedad", match.getBall().getY() > startY);

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
