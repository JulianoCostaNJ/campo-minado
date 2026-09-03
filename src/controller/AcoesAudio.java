package controller;

/**
 * Ações do sistema de som e música (sugestões #35, #36). Separada de
 * {@link AcoesJogador} para que a View de áudio não precise conhecer o
 * restante das ações de jogo.
 */
public interface AcoesAudio {

    void aoAlternarSom();

    void aoAlternarMusica();

    void aoAjustarVolumeMusica(float volume);

    void aoProximaMusica();

    void aoMusicaAnterior();
}
