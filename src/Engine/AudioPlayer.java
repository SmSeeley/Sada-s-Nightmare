package Engine;

import javax.sound.sampled.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AudioPlayer {
    // ---- Music (looping) ----
    private static Clip musicClip;

    // ---- SFX cache & pools (for overlap) ----
    // Each path maps to a small pool of Clips so the same sound can overlap (e.g., footsteps)
    private static final Map<String, List<Clip>> sfxPools = new ConcurrentHashMap<>();
    private static final int DEFAULT_POOL_SIZE = 4; // small and fast; bump if you need more overlap

    // ===== MUSIC =====

    /** Play/loop background music. Calling again switches tracks cleanly. */
    public static void playLoop(String path, float volumeDb) {
        stopLoop(); // ensure no overlap
        try {
            Clip clip = loadClip(path);
            if (clip == null) return;
            musicClip = clip;
            setVolume(musicClip, volumeDb);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
            System.out.println("[AudioPlayer] Looping: " + path);
        } catch (Exception e) {
            System.out.println("[AudioPlayer] Failed to loop " + path + ": " + e);
            stopLoop();
        }
    }

    /** Stop only the looping music. */
    public static void stopLoop() {
        if (musicClip != null) {
            try {
                musicClip.stop();
                musicClip.flush();
                musicClip.close();
            } catch (Exception ignored) {}
            musicClip = null;
        }
    }

    /** Stop everything (music + all SFX). */
    public static void stopAll() {
        stopLoop();
        for (List<Clip> pool : sfxPools.values()) {
            for (Clip c : pool) {
                try { c.stop(); c.flush(); c.close(); } catch (Exception ignored) {}
            }
        }
        sfxPools.clear();
    }

    // ===== SFX =====

    /** Play a one-shot sound effect, with small pool for overlaps and zero lag after first play. */
    public static void playSound(String path, float volumeDb) {
        try {
            Clip clip = obtainClipFromPool(path);
            if (clip == null) return;

            // If clip is currently running (rare due to pool), restart another one
            if (clip.isRunning()) {
                // Try another available clip in pool
                clip = obtainClipFromPool(path, /*forceNewIfBusy*/ true);
                if (clip == null) return;
            }

            setVolume(clip, volumeDb);
            clip.stop(); // ensure reset
            clip.setFramePosition(0); // rewind to start (fast)
            clip.start();
        } catch (Exception e) {
            System.out.println("[AudioPlayer] Failed to play SFX " + path + ": " + e);
        }
    }

    // Obtain a clip from pool; create pool lazily on first use
    private static Clip obtainClipFromPool(String path) {
        return obtainClipFromPool(path, false);
    }

    private static Clip obtainClipFromPool(String path, boolean forceNewIfBusy) {
        List<Clip> pool = sfxPools.computeIfAbsent(path, p -> createPool(p, DEFAULT_POOL_SIZE));
        if (pool == null || pool.isEmpty()) return null;

        // Find a free clip
        for (Clip c : pool) {
            if (!c.isRunning()) return c;
        }

        if (forceNewIfBusy) {
            // Add one more to pool if all are busy (prevents missed sounds on spikes)
            try {
                Clip extra = loadClip(path);
                if (extra != null) {
                    pool.add(extra);
                    return extra;
                }
            } catch (Exception ignored) {}
        }

        // fallback: reuse first (will cut off if still running, but better than silence)
        return pool.get(0);
    }

    private static List<Clip> createPool(String path, int count) {
        List<Clip> pool = new ArrayList<>(Math.max(1, count));
        for (int i = 0; i < count; i++) {
            Clip c = loadClip(path);
            if (c != null) pool.add(c);
        }
        if (pool.isEmpty()) {
            System.out.println("[AudioPlayer] Could not create SFX pool for " + path);
        } else {
            System.out.println("[AudioPlayer] SFX pool ready (" + pool.size() + "): " + path);
        }
        return pool;
    }

    // ===== Low-level helpers =====

    private static Clip loadClip(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("[AudioPlayer] File not found: " + path);
                return null;
            }
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            // Auto-close on STOP for music? No; we manage lifecycle manually.
            return clip;
        } catch (Exception e) {
            System.out.println("[AudioPlayer] loadClip failed for " + path + ": " + e);
            return null;
        }
    }

    private static void setVolume(Clip clip, float gainDb) {
        if (clip == null) return;
        try {
            FloatControl ctrl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float clamped = Math.max(ctrl.getMinimum(), Math.min(ctrl.getMaximum(), gainDb));
            ctrl.setValue(clamped);
        } catch (IllegalArgumentException ignored) {
            // Some mixers/devices don’t expose MASTER_GAIN — just skip.
        }
    }
}
