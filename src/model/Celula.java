package model;

import java.io.Serializable;

/**
 * Representa uma única célula do tabuleiro de Campo Minado.
 * <p>
 * Parte do MODEL (na arquitetura MVC): não conhece Swing, não conhece a
 * View nem o Controller. Todo o estado (minada, revelada, marcação,
 * minasVizinhas) é privado; nenhuma classe externa altera esses valores
 * diretamente, apenas através dos métodos públicos abaixo.
 * <p>
 * Implementa {@link Serializable} para permitir salvar e retomar uma
 * partida em andamento (sugestão #29): o estado inteiro do
 * {@link Tabuleiro}, incluindo cada célula, é serializado tal como está.
 */
public class Celula implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean minada;
    private boolean revelada;
    private EstadoMarcacao estadoMarcacao;
    private int minasVizinhas;

    public Celula() {
        this.minada = false;
        this.revelada = false;
        this.estadoMarcacao = EstadoMarcacao.NENHUMA;
        this.minasVizinhas = 0;
    }

    // ----- minada -----

    public boolean isMinada() {
        return minada;
    }

    public void setMinada(boolean minada) {
        this.minada = minada;
    }

    // ----- revelada -----

    public boolean isRevelada() {
        return revelada;
    }

    /**
     * Revela a célula. Uma célula marcada com bandeira não pode ser
     * revelada por engano; é preciso desmarcá-la primeiro. Uma célula
     * marcada apenas com interrogação pode ser revelada normalmente —
     * a interrogação é só um lembrete visual do jogador, não uma
     * proteção como a bandeira.
     */
    public void revelar() {
        if (estadoMarcacao != EstadoMarcacao.BANDEIRA) {
            this.revelada = true;
        }
    }

    // ----- marcação (bandeira / interrogação) -----

    public EstadoMarcacao getEstadoMarcacao() {
        return estadoMarcacao;
    }

    /** Compatibilidade com o comportamento clássico: true somente quando marcada com bandeira. */
    public boolean isMarcada() {
        return estadoMarcacao == EstadoMarcacao.BANDEIRA;
    }

    public boolean isInterrogada() {
        return estadoMarcacao == EstadoMarcacao.INTERROGACAO;
    }

    /**
     * Avança o estado de marcação no ciclo nenhuma → bandeira →
     * interrogação → nenhuma (sugestão #20). Só é possível marcar uma
     * célula que ainda não foi revelada. Cada instância de Celula guarda
     * o próprio estado — não há nada compartilhado entre células.
     */
    public void alternarMarcacao() {
        if (!revelada) {
            this.estadoMarcacao = this.estadoMarcacao.proximo();
        }
    }

    /** Remove qualquer marcação, sem passar pelo ciclo. Usado por modos especiais (ex.: dica). */
    public void limparMarcacao() {
        this.estadoMarcacao = EstadoMarcacao.NENHUMA;
    }

    // ----- minasVizinhas -----

    public int getMinasVizinhas() {
        return minasVizinhas;
    }

    public void setMinasVizinhas(int minasVizinhas) {
        this.minasVizinhas = minasVizinhas;
    }

    /**
     * Representação usada para exibir a célula no console.
     */
    @Override
    public String toString() {
        if (estadoMarcacao == EstadoMarcacao.BANDEIRA) {
            return "F";
        }
        if (estadoMarcacao == EstadoMarcacao.INTERROGACAO) {
            return "?";
        }
        if (!revelada) {
            return ".";
        }
        if (minada) {
            return "*";
        }
        if (minasVizinhas == 0) {
            return " ";
        }
        return String.valueOf(minasVizinhas);
    }
}
