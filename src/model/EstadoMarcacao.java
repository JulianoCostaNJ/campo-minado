package model;

import java.io.Serializable;

/**
 * Estado de marcação de uma {@link Celula}, independente de ela estar
 * revelada ou não.
 * <p>
 * Substitui o antigo campo {@code boolean marcada} por um ciclo de três
 * estados (nenhuma → bandeira → interrogação → nenhuma), pedido como
 * evolução da marcação clássica de bandeira (sugestão #20). Ficar em um
 * enum, em vez de dois booleans soltos, evita estados inválidos como
 * "bandeira e interrogação ao mesmo tempo".
 */
public enum EstadoMarcacao implements Serializable {

    NENHUMA,
    BANDEIRA,
    INTERROGACAO;

    /**
     * Próximo estado no ciclo nenhuma → bandeira → interrogação → nenhuma.
     * Mantido aqui (e não em {@link Celula}) para que qualquer regra de
     * jogo futura que precise alterar a ordem do ciclo mexa em um único
     * lugar.
     */
    public EstadoMarcacao proximo() {
        switch (this) {
            case NENHUMA:
                return BANDEIRA;
            case BANDEIRA:
                return INTERROGACAO;
            default:
                return NENHUMA;
        }
    }
}
