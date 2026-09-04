package model;

import java.io.Serializable;

/**
 * Conjunto de regras opcionais que alteram o comportamento do
 * {@link Tabuleiro}, mantendo o comportamento clássico como padrão.
 * <p>
 * Antes dessas variações existirem, {@link Tabuleiro} só sabia jogar do
 * jeito clássico (cascata normal, uma vida, sem bordas conectadas). Em
 * vez de multiplicar construtores ou adicionar vários booleans soltos ao
 * {@code Tabuleiro}, todas as variantes de regra ficam agrupadas aqui,
 * como um único objeto imutável — assim, uma nova variante no futuro
 * (ex.: "minas que se movem") normalmente significa só adicionar um campo
 * aqui e um construtor auxiliar em {@code Builder}, sem tocar na
 * assinatura pública de {@code Tabuleiro}.
 * <p>
 * Construído com {@link Builder} (padrão Builder), pensado para deixar
 * explícito quais regras estão ativas mesmo quando várias se combinam:
 * <pre>{@code
 * RegrasJogo regras = RegrasJogo.construir()
 *         .comVidas(3)
 *         .toroidal()
 *         .comSemente(1234L)
 *         .build();
 * }</pre>
 */
public final class RegrasJogo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean cascataAtiva;
    private final int vidasIniciais;
    private final boolean toroidal;
    private final boolean modoRelampago;
    private final int celulasRelampago;
    private final Long semente;

    private RegrasJogo(Builder builder) {
        this.cascataAtiva = builder.cascataAtiva;
        this.vidasIniciais = builder.vidasIniciais;
        this.toroidal = builder.toroidal;
        this.modoRelampago = builder.modoRelampago;
        this.celulasRelampago = builder.celulasRelampago;
        this.semente = builder.semente;
    }

    /** Regras clássicas: cascata normal, uma vida, tabuleiro comum, sem semente fixa. */
    public static RegrasJogo padrao() {
        return new Builder().build();
    }

    public static Builder construir() {
        return new Builder();
    }

    /**
     * Devolve um {@link Builder} pré-preenchido com os valores deste
     * objeto, para derivar uma cópia modificada sem repetir os campos que
     * não mudam. Usado, por exemplo, para pegar as regras escolhidas pelo
     * jogador e só acrescentar uma semente fixa (modo competitivo,
     * sugestão #43), sem perder as outras variantes que ele já tinha
     * marcado.
     */
    public Builder paraBuilder() {
        Builder builder = new Builder();
        builder.cascataAtiva = this.cascataAtiva;
        builder.vidasIniciais = this.vidasIniciais;
        builder.toroidal = this.toroidal;
        builder.modoRelampago = this.modoRelampago;
        builder.celulasRelampago = this.celulasRelampago;
        builder.semente = this.semente;
        return builder;
    }

    public boolean isCascataAtiva() {
        return cascataAtiva;
    }

    public int getVidasIniciais() {
        return vidasIniciais;
    }

    public boolean isToroidal() {
        return toroidal;
    }

    public boolean isModoRelampago() {
        return modoRelampago;
    }

    public int getCelulasRelampago() {
        return celulasRelampago;
    }

    /** Semente fixa para o sorteio de minas, ou {@code null} para aleatório de verdade. */
    public Long getSemente() {
        return semente;
    }

    public static final class Builder {
        private boolean cascataAtiva = true;
        private int vidasIniciais = 1;
        private boolean toroidal = false;
        private boolean modoRelampago = false;
        private int celulasRelampago = 3;
        private Long semente = null;

        /** Modo "sem cascata": revela só a célula clicada, mesmo com zero minas vizinhas. */
        public Builder semCascata() {
            this.cascataAtiva = false;
            return this;
        }

        /** Modo "3 vidas" (ou qualquer número > 1): tolera múltiplos cliques em mina. */
        public Builder comVidas(int vidas) {
            if (vidas < 1) {
                throw new RegraJogoException("O jogo precisa de pelo menos 1 vida.");
            }
            this.vidasIniciais = vidas;
            return this;
        }

        /** Tabuleiro toroidal: as bordas se conectam (topo-base, esquerda-direita). */
        public Builder toroidal() {
            this.toroidal = true;
            return this;
        }

        /** Modo "relâmpago": revela algumas células seguras aleatórias antes de começar. */
        public Builder comRelampago(int quantidadeCelulas) {
            this.modoRelampago = true;
            this.celulasRelampago = Math.max(1, quantidadeCelulas);
            return this;
        }

        /** Semente fixa (ex.: desafio diário, ou dois tabuleiros idênticos no modo competitivo). */
        public Builder comSemente(long semente) {
            this.semente = semente;
            return this;
        }

        public RegrasJogo build() {
            return new RegrasJogo(this);
        }
    }
}
