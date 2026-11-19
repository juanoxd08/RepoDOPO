package test;

import domain.*;
import org.junit.Test;
import persistence.SerializedMatchRepository;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * Persistence unit tests verifying match serialization, state restoration,
 * listener reattachment, and domain exception handling on invalid files.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class DOPOSoccerPersistenceTest {

    /**
     * Verifies that saving and reopening a match restores scoreboard, remaining time,
     * player health, power bar, entity positions, and listener notifications.
     *
     * @throws DOPOSoccerException if match creation or persistence fails
     * @throws IOException if creating the temporary test file fails
     */
    @Test
    public void testSaveAndOpenRestoresFullMatchState() throws DOPOSoccerException, IOException {
        DOPOSoccer originalGame = new DOPOSoccer(new SerializedMatchRepository());
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.CAMEROON)
                .matchType(MatchType.TIME)
                .targetValue(2)
                .build();
        originalGame.newMatch(config);

        for (int i = 0; i < 65; i++) {
            originalGame.setIntention(Side.LEFT, EnumSet.of(PlayerAction.RIGHT));
            originalGame.tick();
        }
        originalGame.pause();

        MatchView before = originalGame.getMatch();
        File tempFile = File.createTempFile("doposoccer_test_", ".dat");
        tempFile.deleteOnExit();

        originalGame.save(tempFile);

        DOPOSoccer loadedGame = new DOPOSoccer(new SerializedMatchRepository());
        AtomicBoolean goalNotified = new AtomicBoolean(false);
        loadedGame.addListener(new MatchListener() {
            @Override
            public void onGoal(Side scorer) {
                goalNotified.set(true);
            }
        });
        loadedGame.open(tempFile);

        MatchView after = loadedGame.getMatch();
        assertNotNull(after);
        assertEquals(GameStatus.PAUSED, after.getStatus());
        assertEquals(before.getGoals(Side.LEFT), after.getGoals(Side.LEFT));
        assertEquals(before.getGoals(Side.RIGHT), after.getGoals(Side.RIGHT));
        assertEquals(before.getRemainingSeconds(), after.getRemainingSeconds());
        assertEquals(before.getBall().getX(), after.getBall().getX(), 0.001);
        assertEquals(before.getBall().getY(), after.getBall().getY(), 0.001);
        assertEquals(before.getPlayer(Side.LEFT).getLife(), after.getPlayer(Side.LEFT).getLife(), 0.001);
        assertEquals(before.getPlayer(Side.LEFT).getPowerProgress(), after.getPlayer(Side.LEFT).getPowerProgress(), 0.001);
        assertEquals(before.getPlayer(Side.LEFT).getHeadShape().getX(), after.getPlayer(Side.LEFT).getHeadShape().getX(), 0.001);

        loadedGame.resume();
        assertEquals(GameStatus.PLAYING, loadedGame.getMatch().getStatus());
        assertFalse(goalNotified.get());
    }

    /**
     * Verifies that opening a non-existent file throws a DOPOSoccerException.
     *
     * @throws DOPOSoccerException expected when file does not exist
     */
    @Test(expected = DOPOSoccerException.class)
    public void testOpenMissingFileThrowsDOPOSoccerException() throws DOPOSoccerException {
        DOPOSoccer game = new DOPOSoccer(new SerializedMatchRepository());
        game.open(new File("non_existent_match_12345.dat"));
    }

    /**
     * Verifies that saving when no match is active throws a DOPOSoccerException.
     *
     * @throws DOPOSoccerException expected when no match has been started
     */
    @Test(expected = DOPOSoccerException.class)
    public void testSaveWithoutActiveMatchThrowsDOPOSoccerException() throws DOPOSoccerException {
        DOPOSoccer game = new DOPOSoccer(new SerializedMatchRepository());
        game.save(new File("unused_match.dat"));
    }
}
