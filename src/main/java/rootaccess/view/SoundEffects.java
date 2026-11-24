// Módulo: rootaccess.view.SoundEffects
// Propósito: sintetiza sons curtos da interface sem depender de ficheiros externos.
// Aluno: C - Cleiton Muangala (30016337)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.view;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SoundEffects {

    private static final Logger LOGGER = Logger.getLogger(SoundEffects.class.getName());
    private static final AudioFormat FORMAT = new AudioFormat(44100, 16, 1, true, false);
    private static final AtomicBoolean WARNING_REPORTED = new AtomicBoolean();
    private static final ExecutorService AUDIO_QUEUE = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "root-access-audio");
        thread.setDaemon(true);
        return thread;
    });

    private static volatile boolean enabled = true;

    private SoundEffects() { }

    public static void setEnabled(boolean enabled) {
        SoundEffects.enabled = enabled;
    }

    public static void playClick() {
        playTone(880, 45);
    }

    public static void playStep() {
        playTone(520, 35);
    }

    public static void playEvent() {
        AUDIO_QUEUE.execute(() -> {
            playToneNow(1040, 90);
            playToneNow(660, 120);
        });
    }

    // Confirma a face final do dado com dois toques curtos e ascendentes.
    public static void playDiceReveal() {
        AUDIO_QUEUE.execute(() -> {
            // Primeiro toque: anuncia o resultado.
            playToneNow(760, 55);
            // Segundo toque: destaca o número que ficou visível.
            playToneNow(1140, 90);
        });
    }

    // Fanfarra curta usada quando o avatar termina a animação de chegada.
    public static void playArrival() {
        AUDIO_QUEUE.execute(() -> {
            // Sequência de três notas para dar sensação de conquista.
            playToneNow(660, 60);
            playToneNow(880, 60);
            playToneNow(1320, 120);
        });
    }

    // Confirma que a firewall deixou passar o jogador.
    public static void playFirewallPass() {
        AUDIO_QUEUE.execute(() -> {
            // Duas notas ascendentes, distintas da chegada normal.
            playToneNow(990, 70);
            playToneNow(1480, 110);
        });
    }

    // Aviso grave quando a resposta à firewall está errada.
    public static void playDenied() {
        AUDIO_QUEUE.execute(() -> {
            // Descida curta para comunicar recusa sem ser agressiva.
            playToneNow(330, 100);
            playToneNow(220, 150);
        });
    }

    private static void playTone(int frequency, int durationMs) {
        if (!enabled) {
            return;
        }
        AUDIO_QUEUE.execute(() -> playToneNow(frequency, durationMs));
    }

    private static void playToneNow(int frequency, int durationMs) {
        if (!enabled) {
            return;
        }
        try (SourceDataLine line = AudioSystem.getSourceDataLine(FORMAT)) {
            line.open(FORMAT);
            line.start();
            byte[] samples = createSamples(frequency, durationMs);
            line.write(samples, 0, samples.length);
            line.drain();
        } catch (LineUnavailableException | IllegalArgumentException ex) {
            if (WARNING_REPORTED.compareAndSet(false, true)) {
                LOGGER.log(Level.WARNING, "Não foi possível reproduzir os sons do jogo.", ex);
            }
        }
    }

    private static byte[] createSamples(int frequency, int durationMs) {
        int frameCount = 44100 * durationMs / 1000;
        byte[] samples = new byte[frameCount * FORMAT.getFrameSize()];
        for (int i = 0; i < frameCount; i++) {
            double envelope = Math.min(1.0, Math.min(i / 180.0, (frameCount - i) / 700.0));
            short sample = (short) (Math.sin(2.0 * Math.PI * frequency * i / 44100.0)
                    * envelope * 9000);
            samples[i * 2] = (byte) sample;
            samples[i * 2 + 1] = (byte) (sample >> 8);
        }
        return samples;
    }
}
