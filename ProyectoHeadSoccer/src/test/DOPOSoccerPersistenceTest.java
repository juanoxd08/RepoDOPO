package test;

import domain.*;
import org.junit.Test;
import persistence.SerializedMatchRepository;
import persistence.SerializedProfileRepository;

import java.io.File;
import java.io.FileWriter;
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
            if (i < 15) {
                originalGame.setIntention(Side.LEFT, EnumSet.of(PlayerAction.RIGHT));
            } else {
                originalGame.setIntention(Side.LEFT, EnumSet.noneOf(PlayerAction.class));
            }
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
     * Verifies that opening a non-existent file throws FILE_NOT_FOUND with an underlying cause.
     */
    @Test
    public void testOpenMissingFileThrowsDOPOSoccerException() {
        DOPOSoccer game = new DOPOSoccer(new SerializedMatchRepository());
        try {
            game.open(new File("non_existent_match_12345.dat"));
            fail("Expected DOPOSoccerException for missing file");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.FILE_NOT_FOUND, e.getMessage());
            assertNotNull(e.getCause());
        }
    }

    /**
     * Verifies that opening a corrupted file throws INVALID_FILE with an underlying cause.
     *
     * @throws IOException if creating the temporary corrupt file fails
     */
    @Test
    public void testOpenCorruptFileThrowsInvalidFileException() throws IOException {
        File corruptFile = File.createTempFile("doposoccer_corrupt_", ".dat");
        corruptFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(corruptFile)) {
            writer.write("not a serialized match");
        }

        DOPOSoccer game = new DOPOSoccer(new SerializedMatchRepository());
        try {
            game.open(corruptFile);
            fail("Expected DOPOSoccerException for corrupt file");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_FILE, e.getMessage());
            assertNotNull(e.getCause());
        }
    }

    /**
     * Verifies that saving when no match is active throws NO_ACTIVE_MATCH.
     */
    @Test
    public void testSaveWithoutActiveMatchThrowsDOPOSoccerException() {
        DOPOSoccer game = new DOPOSoccer(new SerializedMatchRepository());
        try {
            game.save(new File("unused_match.dat"));
            fail("Expected DOPOSoccerException when no match is active");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.NO_ACTIVE_MATCH, e.getMessage());
        }
    }

    /**
     * Verifies that saving and reloading a player profile preserves accumulated points and stats.
     *
     * @throws DOPOSoccerException if profile persistence fails
     * @throws IOException if creating the temporary storage file fails
     */
    @Test
    public void testSaveAndLoadProfilePreservesPointsAndStats() throws DOPOSoccerException, IOException {
        File profileFile = File.createTempFile("doposoccer_profiles_", ".dat");
        profileFile.delete();
        profileFile.deleteOnExit();

        SerializedProfileRepository repo = new SerializedProfileRepository(profileFile);
        PlayerProfile profile = new PlayerProfile("Ana", new PlayerStats(3, 4, 2, 5));
        profile.addPoints(250);
        repo.save(profile);

        SerializedProfileRepository reloadedRepo = new SerializedProfileRepository(profileFile);
        PlayerProfile loaded = reloadedRepo.load("Ana");

        assertEquals("Ana", loaded.getName());
        assertEquals(250, loaded.getPoints());
        assertEquals(3, loaded.getStat(StatType.SPEED));
        assertEquals(4, loaded.getStat(StatType.JUMP));
        assertEquals(2, loaded.getStat(StatType.KICK));
        assertEquals(5, loaded.getStat(StatType.SURVIVAL));
    }

    /**
     * Verifies that loading an unknown player name returns a fresh level-1 profile with zero points.
     *
     * @throws DOPOSoccerException if profile loading fails
     * @throws IOException if creating the temporary storage file fails
     */
    @Test
    public void testLoadUnknownProfileReturnsDefaultLevelOneAndZeroPoints() throws DOPOSoccerException, IOException {
        File profileFile = File.createTempFile("doposoccer_empty_profiles_", ".dat");
        profileFile.delete();
        profileFile.deleteOnExit();

        SerializedProfileRepository repo = new SerializedProfileRepository(profileFile);
        PlayerProfile loaded = repo.load("NuevoJugador");

        assertEquals("NuevoJugador", loaded.getName());
        assertEquals(0, loaded.getPoints());
        assertEquals(1, loaded.getStat(StatType.SPEED));
        assertEquals(1, loaded.getStat(StatType.JUMP));
        assertEquals(1, loaded.getStat(StatType.KICK));
        assertEquals(1, loaded.getStat(StatType.SURVIVAL));
    }

    /**
     * Verifies that finishing a match persists earned points so the next match loads them automatically.
     *
     * @throws DOPOSoccerException if match or profile operations fail
     * @throws IOException if creating the temporary storage file fails
     */
    @Test
    public void testFinishedMatchPersistsPointsForNextMatch() throws DOPOSoccerException, IOException {
        File profileFile = File.createTempFile("doposoccer_integration_profiles_", ".dat");
        profileFile.delete();
        profileFile.deleteOnExit();

        ProfileRepository profileRepo = new SerializedProfileRepository(profileFile);
        DOPOSoccer game = new DOPOSoccer(new SerializedMatchRepository(), profileRepo);

        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftPlayerName("Ana")
                .rightPlayerName("Luis")
                .matchType(MatchType.TIME)
                .targetValue(1)
                .build();

        game.newMatch(config);
        game.finish();

        DOPOSoccer nextSession = new DOPOSoccer(new SerializedMatchRepository(), new SerializedProfileRepository(profileFile));
        nextSession.newMatch(config);

        assertEquals(25, nextSession.getMatch().getPlayer(Side.LEFT).getPoints());
        assertEquals(25, nextSession.getMatch().getPlayer(Side.RIGHT).getPoints());
    }

    /**
     * Verifies that saving and reopening a PVC match preserves modality and keeps the machine controller working.
     *
     * @throws DOPOSoccerException if match creation or persistence fails
     * @throws IOException if creating the temporary match file fails
     */
    @Test
    public void testSaveAndOpenPvcMatchKeepsMachineWorking() throws DOPOSoccerException, IOException {
        DOPOSoccer originalGame = new DOPOSoccer(new SerializedMatchRepository());
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVC)
                .machineProfile(MachineProfile.DEFENSIVA)
                .leftCountry(Country.SOUTH_KOREA)
                .rightCountry(Country.CAMEROON)
                .matchType(MatchType.TIME)
                .targetValue(2)
                .build();
        originalGame.newMatch(config);

        for (int i = 0; i < 30; i++) {
            originalGame.tick();
        }

        File matchFile = File.createTempFile("doposoccer_pvc_", ".dat");
        matchFile.deleteOnExit();
        originalGame.save(matchFile);

        DOPOSoccer loadedGame = new DOPOSoccer(new SerializedMatchRepository());
        loadedGame.open(matchFile);
        assertEquals(Modality.PVC, loadedGame.getMatch().getModality());
        assertEquals(GameStatus.PAUSED, loadedGame.getMatch().getStatus());

        loadedGame.resume();
        for (int i = 0; i < 30; i++) {
            loadedGame.tick();
        }
        assertEquals(GameStatus.PLAYING, loadedGame.getMatch().getStatus());
    }

    /**
     * Verifies that upgrading a player stat through the facade persists the upgraded stat and remaining points.
     *
     * @throws DOPOSoccerException if profile persistence or upgrade fails
     * @throws IOException if creating the temporary storage file fails
     */
    @Test
    public void testUpgradeViaFacadePersistsStatAndDeductsPoints() throws DOPOSoccerException, IOException {
        File profileFile = File.createTempFile("doposoccer_upgrade_profiles_", ".dat");
        profileFile.delete();
        profileFile.deleteOnExit();

        SerializedProfileRepository repo = new SerializedProfileRepository(profileFile);
        PlayerProfile initial = new PlayerProfile("Carlos");
        initial.addPoints(600);
        repo.save(initial);

        DOPOSoccer game = new DOPOSoccer(new SerializedMatchRepository(), repo);
        game.upgrade("Carlos", StatType.JUMP);

        DOPOSoccer reloadedGame = new DOPOSoccer(new SerializedMatchRepository(), new SerializedProfileRepository(profileFile));
        ProfileView view = reloadedGame.getProfile("Carlos");

        assertEquals(2, view.getStat(StatType.JUMP));
        assertEquals(100, view.getPoints());
        assertEquals(1000, view.upgradeCost(StatType.JUMP));
    }

    /**
     * Verifies that MatchConfig rejects identical names on both sides regardless of case.
     */
    @Test
    public void testMatchConfigRejectsSamePlayerNames() {
        try {
            MatchConfig.builder()
                    .modality(Modality.PVP)
                    .leftPlayerName("Juan")
                    .rightPlayerName("juan")
                    .build();
            fail("Expected DOPOSoccerException when both players have the same name");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_MATCH_CONFIG, e.getMessage());
        }
    }

    /**
     * Verifies that finishing an older saved match adds its earned points on top of newer stored progress
     * without overwriting points earned in other matches.
     *
     * @throws DOPOSoccerException if match or profile operations fail
     * @throws IOException if creating temporary test files fails
     */
    @Test
    public void testOpeningOldMatchDoesNotOverwriteNewerProfilePoints() throws DOPOSoccerException, IOException {
        File profileFile = File.createTempFile("doposoccer_edge_profiles_", ".dat");
        profileFile.delete();
        profileFile.deleteOnExit();
        File matchFile = File.createTempFile("doposoccer_edge_match_", ".dat");
        matchFile.deleteOnExit();

        ProfileRepository profileRepo = new SerializedProfileRepository(profileFile);
        DOPOSoccer game = new DOPOSoccer(new SerializedMatchRepository(), profileRepo);
        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftPlayerName("Ana")
                .rightPlayerName("Luis")
                .matchType(MatchType.TIME)
                .targetValue(1)
                .build();

        game.newMatch(config);
        game.save(matchFile);

        PlayerProfile anaLater = profileRepo.load("Ana");
        anaLater.addPoints(400);
        profileRepo.save(anaLater);

        DOPOSoccer reopened = new DOPOSoccer(new SerializedMatchRepository(), profileRepo);
        reopened.open(matchFile);
        reopened.finish();

        ProfileView finalAna = reopened.getProfile("Ana");
        assertEquals(425, finalAna.getPoints());
    }

    /**
     * Verifies that a profile save failure on match completion notifies listeners via onProfileSaveFailed
     * without crashing the match tick loop.
     *
     * @throws DOPOSoccerException if match setup fails
     */
    @Test
    public void testProfileSaveFailureNotifiesListenerInsteadOfThrowing() throws DOPOSoccerException {
        ProfileRepository failingRepo = new ProfileRepository() {
            @Override
            public PlayerProfile load(String name) {
                return new PlayerProfile(name);
            }

            @Override
            public void save(PlayerProfile profile) throws DOPOSoccerException {
                throw new DOPOSoccerException(DOPOSoccerException.SAVE_ERROR);
            }
        };

        DOPOSoccer game = new DOPOSoccer(new SerializedMatchRepository(), failingRepo);
        AtomicBoolean notified = new AtomicBoolean(false);
        game.addListener(new MatchListener() {
            @Override
            public void onProfileSaveFailed(String message) {
                if (DOPOSoccerException.SAVE_ERROR.equals(message)) {
                    notified.set(true);
                }
            }
        });

        MatchConfig config = MatchConfig.builder()
                .modality(Modality.PVP)
                .leftPlayerName("Ana")
                .rightPlayerName("Luis")
                .build();
        game.newMatch(config);
        game.finish();

        assertTrue(notified.get());
    }
}
