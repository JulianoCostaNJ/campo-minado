package model;

/**
 * Exceção customizada para violações de regras do jogo 
 */
public class RegraJogoException extends RuntimeException {

    public RegraJogoException(String mensagem) {
        super(mensagem);
    }
}