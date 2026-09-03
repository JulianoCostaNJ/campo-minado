package controller;

import model.ModoJogadores;
import model.RegrasJogo;

/**
 * Tudo que é preciso para começar uma partida nova: dimensões, número de
 * minas, as regras variantes (sugestões #12, #16, #17, #18, #49) e o modo
 * de jogadores (sugestões #19, #43).
 * <p>
 * Ter esse objeto único, em vez de {@code aoEscolherDificuldade} crescer
 * um parâmetro a cada sugestão nova, é o que permite que a tela de
 * dificuldade personalizada (sugestão #14) e o desafio diário (sugestão
 * #49) reaproveitem exatamente o mesmo caminho das dificuldades prontas,
 * só que com uma configuração diferente.
 */
public final class ConfiguracaoPartida {

    private final int linhas;
    private final int colunas;
    private final int minas;
    private final RegrasJogo regras;
    private final ModoJogadores modoJogadores;
    private final String nomeDificuldade;

    public ConfiguracaoPartida(int linhas, int colunas, int minas, RegrasJogo regras,
                                ModoJogadores modoJogadores, String nomeDificuldade) {
        this.linhas = linhas;
        this.colunas = colunas;
        this.minas = minas;
        this.regras = regras == null ? RegrasJogo.padrao() : regras;
        this.modoJogadores = modoJogadores == null ? ModoJogadores.INDIVIDUAL : modoJogadores;
        this.nomeDificuldade = nomeDificuldade;
    }

    /** Atalho para uma dificuldade clássica (regras padrão, um jogador). */
    public static ConfiguracaoPartida classica(int linhas, int colunas, int minas, String nomeDificuldade) {
        return new ConfiguracaoPartida(linhas, colunas, minas, RegrasJogo.padrao(), ModoJogadores.INDIVIDUAL, nomeDificuldade);
    }

    public int getLinhas() {
        return linhas;
    }

    public int getColunas() {
        return colunas;
    }

    public int getMinas() {
        return minas;
    }

    public RegrasJogo getRegras() {
        return regras;
    }

    public ModoJogadores getModoJogadores() {
        return modoJogadores;
    }

    public String getNomeDificuldade() {
        return nomeDificuldade;
    }
}
