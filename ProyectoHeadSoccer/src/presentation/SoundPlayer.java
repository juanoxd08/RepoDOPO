package presentation;

import persistence.Log;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Loads and plays WAV sound effects for match events, failing silently when audio is unavailable.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class SoundPlayer {
    private static final String SOUNDS_DIR = "/presentation/resources/sounds/";

    private final Map<Sound, Clip> audios = new EnumMap<>(Sound.class);
    private boolean muted = false;

    /**
     * Preloads all configured sound effects into memory.
     */
    public SoundPlayer() {
        for (Sound sound : Sound.values()) {
            loadClip(sound);
        }
    }

    /**
     * Plays the given sound effect from the beginning unless muted or unavailable.
     *
     * @param sound Sound effect to play
     */
    public void play(Sound sound) {
        if (muted || sound == null) return;
        Clip clip = audios.get(sound);
        if (clip == null) return;
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    /**
     * Toggles global sound playback mute state.
     */
    public void toggleMute() {
        muted = !muted;
    }

    /**
     * Indicates whether sound playback is currently muted.
     *
     * @return True if muted
     */
    public boolean isMuted() {
        return muted;
    }

    /*
     * Loads a single WAV resource into a reusable Clip, logging any missing or unsupported audio.
     */
    private void loadClip(Sound sound) {
        String path = SOUNDS_DIR + sound.getFileName();
        InputStream raw = getClass().getResourceAsStream(path);
        if (raw == null) {
            Log.record(new IOException("Sonido no encontrado: " + path));
            return;
        }
        try (InputStream stream = raw;
             BufferedInputStream buffered = new BufferedInputStream(stream);
             AudioInputStream audio = AudioSystem.getAudioInputStream(buffered)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            audios.put(sound, clip);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            Log.record(ex);
        }
    }
}