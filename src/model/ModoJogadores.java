package model;

/**
 * Quantos jogadores participam da partida e como (sugestões #19 e #43).
 * Fica no Model por ser um conceito de regra de jogo, não de tela — tanto
 * o Controller quanto a camada de persistência precisam conhecê-lo, e
 * colocá-lo aqui evita que a persistência dependa do pacote controller.
 */
public enum ModoJogadores {

    /** Um jogador, um tabuleiro — o modo clássico. */
    INDIVIDUAL,

    /** Dois jogadores revezam jogadas no mesmo tabuleiro (sugestão #19). */
    COOPERATIVO,

    /** Dois jogadores competem em tabuleiros idênticos, por tempo (sugestão #43). */
    COMPETITIVO
}
