package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Grava, em ordem, as jogadas (revelar/marcar) feitas durante uma
 * partida, para permitir reproduzi-las depois, jogada a jogada (modo
 * replay, sugestão #41).
 * <p>
 * Fica isolado do Controller para que ele não precise saber COMO uma
 * gravação é guardada — hoje é só uma lista em memória; se um dia o
 * replay precisar ser salvo em disco entre execuções, essa mudança fica
 * só aqui.
 */
public class GravadorJogadas {

    private final List<Jogada> jogadas = new ArrayList<>();
    private boolean gravando = true;

    public void registrar(TipoJogada tipo, int linha, int coluna) {
        if (gravando) {
            jogadas.add(new Jogada(tipo, linha, coluna));
        }
    }

    /** Usado enquanto um replay está sendo reproduzido, para não gravar as jogadas do replay em si. */
    public void pausarGravacao() {
        gravando = false;
    }

    public void retomarGravacao() {
        gravando = true;
    }

    public void reiniciar() {
        jogadas.clear();
        gravando = true;
    }

    public List<Jogada> getJogadas() {
        return Collections.unmodifiableList(jogadas);
    }

    public boolean possuiJogadas() {
        return !jogadas.isEmpty();
    }
}
