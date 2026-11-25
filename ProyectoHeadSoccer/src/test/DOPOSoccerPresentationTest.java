package test;

import domain.Country;
import domain.DOPOSoccerException;
import domain.MachineProfile;
import domain.MatchConfig;
import domain.MatchType;
import domain.Modality;
import domain.PlayerEffect;
import java.awt.Color;
import javax.swing.JButton;
import org.junit.Test;
import presentation.FieldResolution;
import presentation.ImageAssets;
import presentation.MatchSetup;
import presentation.Screen;
import presentation.Sound;
import presentation.SoundPlayer;
import presentation.Theme;

import static org.junit.Assert.*;

/**
 * Unit tests covering non-visual presentation logic (MatchSetup, Theme, Sound,
 * SoundPlayer, FieldResolution, Screen, and ImageAssets) without opening Swing windows.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class DOPOSoccerPresentationTest {

    /**
     * Verifies that MatchSetup.toConfig builds valid PvP and PvM configurations,
     * applies TIME and GOALS target values, and rejects duplicate player names.
     *
     * @throws DOPOSoccerException if configuration validation fails unexpectedly
     */
    @Test
    public void testMatchSetupToConfigPvPAndPvMAndDuplicateValidation() throws DOPOSoccerException {
        MatchSetup setup = new MatchSetup();
        setup.setLeftPlayerName("  Juan  ");
        setup.setRightPlayerName("  Cristian ");
        setup.setLeftPlayerName("   ");
        assertEquals("Juan", setup.getLeftPlayerName());
        assertEquals("Cristian", setup.getRightPlayerName());

        MatchConfig pvpConfig = setup.toConfig();
        assertEquals(Modality.PVP, pvpConfig.getModality());
        assertEquals("Juan", pvpConfig.getLeftPlayerName());
        assertEquals("Cristian", pvpConfig.getRightPlayerName());
        assertEquals(MatchType.TIME, pvpConfig.getMatchType());
        assertEquals(2, pvpConfig.getTargetValue());

        setup.setModality(Modality.PVC);
        setup.selectOrCycleGoalsMatch();
        MatchConfig pvmConfig = setup.toConfig();
        assertEquals(Modality.PVC, pvmConfig.getModality());
        assertEquals("Juan", pvmConfig.getLeftPlayerName());
        assertEquals("COM " + setup.getMachineProfile().getDisplayName(), pvmConfig.getRightPlayerName());
        assertEquals(MatchType.GOALS, pvmConfig.getMatchType());
        assertEquals(3, pvmConfig.getTargetValue());

        setup.setModality(Modality.PVP);
        setup.setRightPlayerName("Juan");
        try {
            setup.toConfig();
            fail("Expected DOPOSoccerException when both player names are equal");
        } catch (DOPOSoccerException ex) {
            assertEquals(DOPOSoccerException.INVALID_MATCH_CONFIG, ex.getMessage());
        }
    }

    /**
     * Verifies all MatchSetup cycling helpers wrap through every option and return to the start.
     */
    @Test
    public void testMatchSetupCyclesCountriesProfilesModesAndResolution() {
        MatchSetup setup = new MatchSetup();

        Country initialLeft = setup.getLeftCountry();
        for (int i = 0; i < Country.values().length; i++) {
            setup.cycleLeftCountry();
        }
        assertEquals(initialLeft, setup.getLeftCountry());

        Country initialRight = setup.getRightCountry();
        for (int i = 0; i < Country.values().length; i++) {
            setup.cycleRightCountry();
        }
        assertEquals(initialRight, setup.getRightCountry());

        MachineProfile initialProfile = setup.getMachineProfile();
        for (int i = 0; i < MachineProfile.values().length; i++) {
            setup.cycleMachineProfile();
        }
        assertEquals(initialProfile, setup.getMachineProfile());

        setup.selectOrCycleTimeMatch();
        assertEquals(3, setup.getMatchDurationMinutes());
        setup.selectOrCycleTimeMatch();
        assertEquals(1, setup.getMatchDurationMinutes());
        setup.selectOrCycleTimeMatch();
        assertEquals(2, setup.getMatchDurationMinutes());

        setup.selectOrCycleGoalsMatch();
        assertEquals(MatchType.GOALS, setup.getMatchType());
        assertEquals(3, setup.getTargetGoals());
        setup.selectOrCycleGoalsMatch();
        assertEquals(5, setup.getTargetGoals());
        setup.selectOrCycleGoalsMatch();
        assertEquals(7, setup.getTargetGoals());
        setup.selectOrCycleGoalsMatch();
        assertEquals(3, setup.getTargetGoals());

        setup.selectOrCycleTimeMatch();
        assertEquals(MatchType.TIME, setup.getMatchType());

        setup.setResolution(FieldResolution.LARGE);
        assertEquals(FieldResolution.LARGE, setup.getResolution());
    }

    /**
     * Verifies that Theme color lookups return valid colors for every enum value and null,
     * and that Swing styling helpers configure button properties cleanly.
     */
    @Test
    public void testThemeColorsAndButtonHelpers() {
        for (Country c : Country.values()) {
            assertNotNull(Theme.countryColor(c));
            assertNotNull(Theme.powerColor(c));
        }
        assertNotNull(Theme.countryColor(null));
        assertNotNull(Theme.powerColor(null));

        for (PlayerEffect e : PlayerEffect.values()) {
            assertNotNull(Theme.effectColor(e));
        }
        assertEquals(Color.WHITE, Theme.effectColor(null));

        JButton btn = Theme.createStyledButton("PLAY", Theme.ACTION_BLUE, Color.WHITE, 16);
        assertEquals("PLAY", btn.getText());
        assertEquals(Theme.ACTION_BLUE, btn.getBackground());
        assertEquals(Color.WHITE, btn.getForeground());

        Theme.styleToggle(btn, true);
        assertEquals(Theme.ACCENT_GOLD, btn.getBackground());
        assertEquals(Color.BLACK, btn.getForeground());

        Theme.styleToggle(btn, false);
        assertEquals(Theme.TOGGLE_UNSELECTED, btn.getBackground());
        assertEquals(Color.WHITE, btn.getForeground());
    }

    /**
     * Verifies Sound filenames, safe SoundPlayer initialization when WAV files are absent,
     * playback resilience, and mute toggling.
     */
    @Test
    public void testSoundAndSoundPlayerHandleMissingWavsAndMuteToggle() {
        for (Sound s : Sound.values()) {
            assertNotNull(s.getFileName());
            assertTrue(s.getFileName().endsWith(".wav"));
        }

        SoundPlayer player = new SoundPlayer();
        assertFalse(player.isMuted());

        for (Sound s : Sound.values()) {
            player.play(s);
        }
        player.play(null);

        player.toggleMute();
        assertTrue(player.isMuted());
        player.play(Sound.GOAL);

        player.toggleMute();
        assertFalse(player.isMuted());
    }

    /**
     * Verifies FieldResolution presets, Screen enum coverage, and ImageAssets null-safe accessors.
     */
    @Test
    public void testFieldResolutionScreenAndImageAssets() {
        for (FieldResolution res : FieldResolution.values()) {
            assertNotNull(res.getLabel());
            assertTrue(res.getScaleFactor() > 0.0);
            assertTrue(res.getPixelWidth() > 0);
            assertTrue(res.getPixelHeight() > 0);
        }
        assertTrue(Screen.values().length >= 5);

        ImageAssets assets = new ImageAssets();
        assertNull(assets.getFlag(null));
        for (Country c : Country.values()) {
            assets.getFlag(c);
        }
        assets.getBall();
    }
}
