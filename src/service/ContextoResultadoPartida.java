package service;

/**
 * Retrato do fim de uma partida, usado só para avaliar conquistas
 * (sugestão #50). Mantido separado de {@link persistence.RegistroPartida}
 * porque carrega detalhes (bandeira usada, dicas usadas, vidas perdidas)
 * que fazem sentido no momento da avaliação mas não precisam virar
 * histórico permanente.
 */
public final class ContextoResultadoPartida {

    private final boolean vitoria;
    private final int tempoSegundos;
    private final String dificuldade;
    private final boolean bandeiraUsada;
    private final int dicasUsadas;
    private final int vidasPerdidas;

    public ContextoResultadoPartida(boolean vitoria, int tempoSegundos, String dificuldade,
                                     boolean bandeiraUsada, int dicasUsadas, int vidasPerdidas) {
        this.vitoria = vitoria;
        this.tempoSegundos = tempoSegundos;
        this.dificuldade = dificuldade;
        this.bandeiraUsada = bandeiraUsada;
        this.dicasUsadas = dicasUsadas;
        this.vidasPerdidas = vidasPerdidas;
    }

    public boolean isVitoria() {
        return vitoria;
    }

    public int getTempoSegundos() {
        return tempoSegundos;
    }

    public String getDificuldade() {
        return dificuldade;
    }

    public boolean isBandeiraUsada() {
        return bandeiraUsada;
    }

    public int getDicasUsadas() {
        return dicasUsadas;
    }

    public int getVidasPerdidas() {
        return vidasPerdidas;
    }
}
