package audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementação de {@link ServicoMusica} usando {@code javax.sound.sampled}.
 * <p>
 * A playlist é descoberta automaticamente: todo arquivo {@code .wav}
 * dentro de {@code recursos/musicas/} vira uma faixa, na ordem em que o
 * sistema de arquivos os lista. Isso é o que torna "adicionar uma música
 * nova" tão simples quanto copiar um arquivo para essa pasta — nenhuma
 * classe Java precisa ser tocada. Faixas também podem ser adicionadas em
 * tempo de execução via {@link #adicionarFaixa(Musica)}, para uma futura
 * tela de "importar música", por exemplo.
 * <p>
 * Este projeto não veio com arquivos de música reais; a pasta é criada
 * vazia. Sem faixas, o serviço simplesmente não toca nada — silenciosa e
 * graciosamente — e avisa uma única vez no console.
 */
public class ServicoMusicaClip implements ServicoMusica {

    private static final File PASTA_PADRAO = new File("recursos/musicas");

    private final File pasta;
    private final List<Musica> playlist = new ArrayList<>();
    private int indiceAtual = -1;
    private Clip clipAtual;
    private float volume = 0.6f;
    private boolean ativado = false;
    private boolean avisoAusenciaJaExibido = false;

    public ServicoMusicaClip() {
        this(PASTA_PADRAO);
    }

    public ServicoMusicaClip(File pasta) {
        this.pasta = pasta;
        carregarPastaDeMusicas();
    }

    private void carregarPastaDeMusicas() {
        File[] arquivos = pasta.isDirectory()
                ? pasta.listFiles((dir, nome) -> nome.toLowerCase().endsWith(".wav"))
                : null;
        if (arquivos == null) {
            return;
        }
        for (File arquivo : arquivos) {
            String titulo = arquivo.getName().replaceFirst("(?i)\\.wav$", "");
            playlist.add(new Musica(titulo, arquivo.getPath()));
        }
    }

    @Override
    public List<Musica> getPlaylist() {
        return Collections.unmodifiableList(playlist);
    }

    @Override
    public void adicionarFaixa(Musica musica) {
        playlist.add(musica);
    }

    @Override
    public void tocar() {
        if (!ativado || playlist.isEmpty()) {
            avisarAusenciaDeFaixasSeNecessario();
            return;
        }
        if (indiceAtual == -1) {
            indiceAtual = 0;
        }
        abrirEIniciar(indiceAtual);
    }

    @Override
    public void pausar() {
        if (clipAtual != null && clipAtual.isRunning()) {
            clipAtual.stop();
        }
    }

    @Override
    public void parar() {
        if (clipAtual != null) {
            clipAtual.stop();
            clipAtual.close();
            clipAtual = null;
        }
    }

    @Override
    public void proxima() {
        if (playlist.isEmpty()) {
            return;
        }
        indiceAtual = (indiceAtual + 1) % playlist.size();
        if (ativado) {
            abrirEIniciar(indiceAtual);
        }
    }

    @Override
    public void anterior() {
        if (playlist.isEmpty()) {
            return;
        }
        indiceAtual = (indiceAtual - 1 + playlist.size()) % playlist.size();
        if (ativado) {
            abrirEIniciar(indiceAtual);
        }
    }

    @Override
    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
        aplicarVolume();
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public void setAtivado(boolean ativado) {
        this.ativado = ativado;
        if (!ativado) {
            parar();
        } else {
            tocar();
        }
    }

    @Override
    public boolean isAtivado() {
        return ativado;
    }

    @Override
    public Musica getFaixaAtual() {
        return (indiceAtual >= 0 && indiceAtual < playlist.size()) ? playlist.get(indiceAtual) : null;
    }

    @Override
    public boolean isTocando() {
        return clipAtual != null && clipAtual.isRunning();
    }

    private void abrirEIniciar(int indice) {
        parar();
        try {
            Musica musica = playlist.get(indice);
            AudioInputStream fluxo = AudioSystem.getAudioInputStream(new File(musica.getCaminhoArquivo()));
            clipAtual = AudioSystem.getClip();
            clipAtual.open(fluxo);
            aplicarVolume();
            clipAtual.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Não foi possível tocar '" + playlist.get(indice).getTitulo() + "': " + e.getMessage());
        }
    }

    private void aplicarVolume() {
        if (clipAtual == null || !clipAtual.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl controle = (FloatControl) clipAtual.getControl(FloatControl.Type.MASTER_GAIN);
        float minimo = controle.getMinimum();
        float maximo = controle.getMaximum();
        float ganho = minimo + (maximo - minimo) * volume;
        controle.setValue(ganho);
    }

    private void avisarAusenciaDeFaixasSeNecessario() {
        if (!avisoAusenciaJaExibido) {
            System.out.println("Nenhuma música encontrada em '" + pasta.getPath()
                    + "'. Adicione arquivos .wav nessa pasta para ativar a trilha sonora.");
            avisoAusenciaJaExibido = true;
        }
    }
}
