package persistence;

import model.ModoJogadores;
import model.Tabuleiro;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Fotografia completa de uma partida em andamento, pronta para ser
 * serializada e depois restaurada exatamente do ponto onde parou
 * (sugestão #29 — "Salvar e retomar uma partida em andamento").
 * <p>
 * O núcleo é o próprio {@link Tabuleiro}, serializado tal como está
 * (minas, células reveladas/marcadas, vidas restantes, regras da
 * partida). Os campos extras aqui são só o que o {@code Tabuleiro}
 * sozinho não sabe: há quanto tempo a partida está rodando, quantas
 * jogadas já foram feitas, o limite de tempo escolhido e o modo de
 * jogadores — tudo que o Controller precisa para religar o cronômetro e
 * a tela exatamente como estavam.
 */
public final class SaveGame implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Tabuleiro tabuleiro;
    private final String nomeDificuldade;
    private final int tempoDecorridoSegundos;
    private final int limiteSegundos;
    private final int jogadas;
    private final ModoJogadores modoJogadores;
    private final LocalDateTime salvoEm;

    public SaveGame(Tabuleiro tabuleiro, String nomeDificuldade, int tempoDecorridoSegundos,
                     int limiteSegundos, int jogadas, ModoJogadores modoJogadores) {
        this.tabuleiro = tabuleiro;
        this.nomeDificuldade = nomeDificuldade;
        this.tempoDecorridoSegundos = tempoDecorridoSegundos;
        this.limiteSegundos = limiteSegundos;
        this.jogadas = jogadas;
        this.modoJogadores = modoJogadores;
        this.salvoEm = LocalDateTime.now();
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    public String getNomeDificuldade() {
        return nomeDificuldade;
    }

    public int getTempoDecorridoSegundos() {
        return tempoDecorridoSegundos;
    }

    public int getLimiteSegundos() {
        return limiteSegundos;
    }

    public int getJogadas() {
        return jogadas;
    }

    public ModoJogadores getModoJogadores() {
        return modoJogadores;
    }

    public LocalDateTime getSalvoEm() {
        return salvoEm;
    }
}
