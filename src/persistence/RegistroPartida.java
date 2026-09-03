package persistence;

import model.ModoJogadores;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Um registro de partida encerrada (vitória ou derrota). Fica no centro
 * de várias sugestões da lista que, no fundo, são só formas diferentes
 * de olhar para a mesma informação — melhor tempo (#30), histórico de
 * partidas (#31), exportação CSV (#32) e ranking (#42) todos leem a
 * mesma lista de {@code RegistroPartida}, guardada por
 * {@link repository.EstatisticasRepository}, em vez de espalhar dados
 * parecidos em classes diferentes.
 */
public final class RegistroPartida implements Serializable, Comparable<RegistroPartida> {

    private static final long serialVersionUID = 1L;

    private final String perfil;
    private final String dificuldade;
    private final boolean vitoria;
    private final int tempoSegundos;
    private final int jogadas;
    private final ModoJogadores modoJogadores;
    private final LocalDateTime dataHora;

    public RegistroPartida(String perfil, String dificuldade, boolean vitoria, int tempoSegundos,
                            int jogadas, ModoJogadores modoJogadores) {
        this.perfil = perfil;
        this.dificuldade = dificuldade;
        this.vitoria = vitoria;
        this.tempoSegundos = tempoSegundos;
        this.jogadas = jogadas;
        this.modoJogadores = modoJogadores;
        this.dataHora = LocalDateTime.now();
    }

    public String getPerfil() {
        return perfil;
    }

    public String getDificuldade() {
        return dificuldade;
    }

    public boolean isVitoria() {
        return vitoria;
    }

    public int getTempoSegundos() {
        return tempoSegundos;
    }

    public int getJogadas() {
        return jogadas;
    }

    public ModoJogadores getModoJogadores() {
        return modoJogadores;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    /** Ordena por tempo — usado pelo ranking (sugestão #42). */
    @Override
    public int compareTo(RegistroPartida outro) {
        return Integer.compare(this.tempoSegundos, outro.tempoSegundos);
    }

    /** Uma linha de CSV (sugestão #32). O cabeçalho correspondente fica no repositório. */
    public String paraLinhaCsv() {
        return String.join(",",
                escapar(perfil),
                escapar(dificuldade),
                String.valueOf(vitoria),
                String.valueOf(tempoSegundos),
                String.valueOf(jogadas),
                modoJogadores.name(),
                dataHora.toString());
    }

    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.contains(",") ? "\"" + valor + "\"" : valor;
    }
}
