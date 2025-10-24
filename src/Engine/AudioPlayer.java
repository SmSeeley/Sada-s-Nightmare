package Engine;

import javax.sound.sampled.*;
import java.io.File;

public class AudioPlayer {
    private static Clip clip;

    // Play and loop a WAV file indefinitely
    public static void playLoop(String path, float volumeDb) {
        stop();
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("[AudioPlayer] File not found: " + path);
                return;
            }

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(stream);
            setVolume(volumeDb);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            System.out.println("[AudioPlayer] Playing loop: " + path);
        } catch (Exception e) {
            System.out.println("[AudioPlayer] Failed to play " + path + ": " + e);
            stop();
        }
    }

    //stop playing music
    public static void stop() {
        if (clip != null) {
            try {
                clip.stop();
                clip.close();
            } catch (Exception ignored) {}
            clip = null;
        }
    }

    // adjust volume
    public static void setVolume(float gainDb) {
        if (clip == null) return;
        try {
            FloatControl ctrl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainDb = Math.max(ctrl.getMinimum(), Math.min(ctrl.getMaximum(), gainDb));
            ctrl.setValue(gainDb);
        } catch (IllegalArgumentException ignored) {}
    }

    // Play a short, one-time sound (like item pickup, attack, etc.)
    public static void playSound(String path, float volumeDb) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("[AudioPlayer] File not found: " + path);
                return;
            }

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            Clip sfx = AudioSystem.getClip();
            sfx.open(stream);

            // set volume
            try {
                FloatControl ctrl = (FloatControl) sfx.getControl(FloatControl.Type.MASTER_GAIN);
                volumeDb = Math.max(ctrl.getMinimum(), Math.min(ctrl.getMaximum(), volumeDb));
                ctrl.setValue(volumeDb);
            } catch (IllegalArgumentException ignored) {}

            sfx.start();

            // automatically close clip when done playing
            sfx.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    sfx.close();
                }
            });

            System.out.println("[AudioPlayer] Played sound: " + path);
        } catch (Exception e) {
            System.out.println("[AudioPlayer] Failed to play sound " + path + ": " + e);
        }
}
}
