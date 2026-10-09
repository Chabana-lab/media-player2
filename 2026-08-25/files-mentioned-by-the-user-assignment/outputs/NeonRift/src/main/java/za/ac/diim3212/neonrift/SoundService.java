package za.ac.diim3212.neonrift;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Lightweight generated audio: no external files or libraries are required. */
public final class SoundService {
    private static final SoundService INSTANCE = new SoundService();
    private final ScheduledExecutorService musicLoop = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "neon-rift-music"); thread.setDaemon(true); return thread;
    });
    private volatile boolean soundEnabled = true;
    private volatile boolean musicEnabled;
    private volatile boolean musicStarted;

    private SoundService() { }
    public static SoundService get() { return INSTANCE; }
    public void setSoundEnabled(boolean enabled) { soundEnabled = enabled; }
    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (enabled && !musicStarted) {
            musicStarted = true;
            musicLoop.scheduleAtFixedRate(this::ambientPhrase, 0, 2300, TimeUnit.MILLISECONDS);
        }
    }
    public void click() { tone(620, 45, .13); }
    public void collect() { tone(880, 85, .20); }
    public void hit() { tone(150, 180, .22); }
    public void complete() { tone(523, 120, .18); tone(659, 140, .18); tone(784, 240, .20); }

    private void ambientPhrase() {
        if (!musicEnabled) return;
        toneRaw(165, 360, .05); toneRaw(220, 320, .045); toneRaw(247, 420, .04);
    }
    private void tone(int frequency, int durationMs, double volume) {
        if (soundEnabled) toneRaw(frequency, durationMs, volume);
    }
    private void toneRaw(int frequency, int durationMs, double volume) {
        Thread audio = new Thread(() -> {
            AudioFormat format = new AudioFormat(44_100, 8, 1, true, false);
            SourceDataLine line = null;
            try {
                line = AudioSystem.getSourceDataLine(format);
                line.open(format); line.start();
                int samples = 44_100 * durationMs / 1000;
                byte[] data = new byte[samples];
                for (int i = 0; i < samples; i++) {
                    double envelope = Math.min(1, Math.min(i / 180.0, (samples - i) / 450.0));
                    data[i] = (byte) (Math.sin(2 * Math.PI * frequency * i / 44_100.0) * 127 * volume * envelope);
                }
                line.write(data, 0, data.length); line.drain();
            } catch (LineUnavailableException ignored) {
                // The GUI remains usable on systems without an audio output device.
            } finally {
                if (line != null) line.close();
            }
        }, "neon-rift-sfx");
        audio.setDaemon(true); audio.start();
    }
}
