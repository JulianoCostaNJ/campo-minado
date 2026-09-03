package service;

/**
 * Catálogo de conquistas/badges (sugestão #50). Cada constante carrega a
 * própria regra de desbloqueio — um "Strategy por constante de enum" —
 * então adicionar uma conquista nova no futuro é só acrescentar mais uma
 * constante com sua própria implementação de {@link #foiConquistada};
 * nenhuma outra classe (nem {@link ConquistaService}) precisa mudar.
 */
public enum Conquista {

    PRIMEIRA_VITORIA("Primeira vitória", "Venceu uma partida pela primeira vez.") {
        @Override
        public boolean foiConquistada(ContextoResultadoPartida contexto) {
            return contexto.isVitoria();
        }
    },

    SEM_BANDEIRA("Sem bandeira", "Venceu sem usar nenhuma bandeira.") {
        @Override
        public boolean foiConquistada(ContextoResultadoPartida contexto) {
            return contexto.isVitoria() && !contexto.isBandeiraUsada();
        }
    },

    RELAMPAGO_HUMANO("Relâmpago humano", "Venceu em menos de 30 segundos.") {
        @Override
        public boolean foiConquistada(ContextoResultadoPartida contexto) {
            return contexto.isVitoria() && contexto.getTempoSegundos() < 30;
        }
    },

    POR_CONTA_PROPRIA("Por conta própria", "Venceu sem usar nenhuma dica.") {
        @Override
        public boolean foiConquistada(ContextoResultadoPartida contexto) {
            return contexto.isVitoria() && contexto.getDicasUsadas() == 0;
        }
    },

    MESTRE_DO_AVANCADO("Mestre do Avançado", "Venceu uma partida no modo Avançado.") {
        @Override
        public boolean foiConquistada(ContextoResultadoPartida contexto) {
            return contexto.isVitoria() && "Avançado".equals(contexto.getDificuldade());
        }
    },

    SOBREVIVENTE("Sobrevivente", "Venceu depois de pisar em pelo menos uma mina, no modo várias vidas.") {
        @Override
        public boolean foiConquistada(ContextoResultadoPartida contexto) {
            return contexto.isVitoria() && contexto.getVidasPerdidas() > 0;
        }
    };

    private final String titulo;
    private final String descricao;

    Conquista(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public abstract boolean foiConquistada(ContextoResultadoPartida contexto);

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }
}
