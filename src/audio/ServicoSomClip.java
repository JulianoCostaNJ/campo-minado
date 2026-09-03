package audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/**
 * Implementação de {@link ServicoSom} usando {@code javax.sound.sampled},
 * como sugerido na lista (#35).
 * <p>
 * Os clipes são carregados uma vez, de {@code recursos/sons/<arquivo>},
 * relativo ao diretório de execução. Este projeto não veio com arquivos
 * de áudio reais — colocar um .wav com o nome esperado (veja
 * {@link EfeitoSonoro}) em {@code recursos/sons/} liga o efeito
 * automaticamente, sem precisar recompilar nada. Se o arquivo não
 * existir, o efeito simplesmente não toca (falha silenciosa, com um
 * aviso único no console) — o jogo nunca trava por falta de áudio.
 */
public class ServicoSomClip implements ServicoSom {

    private final File pasta;
    private final Map<EfeitoSonoro, Clip> clipesCarregados = new EnumMap<>(EfeitoSonoro.class);
    private boolean ativado = true;

    public ServicoSomClip() {
        this(new File("recursos/sons"));
    }

    public ServicoSomClip(File pasta) {
        this.pasta = pasta;
        carregarClipes();
    }

    private void carregarClipes() {
        for (EfeitoSonoro efeito : EfeitoSonoro.values()) {
            File arquivo = new File(pasta, efeito.getNomeArquivo());
            if (!arquivo.isFile()) {
                continue;
            }
            try (AudioInputStream fluxo = AudioSystem.getAudioInputStream(arquivo)) {
                Clip clip = AudioSystem.getClip();
                clip.open(fluxo);
                clipesCarregados.put(efeito, clip);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("Não foi possível carregar o som de " + efeito + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void tocar(EfeitoSonoro efeito) {
        if (!ativado) {
            return;
        }
        Clip clip = clipesCarregados.get(efeito);
        if (clip == null) {
            return;
        }
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    @Override
    public void setAtivado(boolean ativado) {
        this.ativado = ativado;
    }

    @Override
    public boolean isAtivado() {
        return ativado;
    }
}
