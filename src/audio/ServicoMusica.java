package audio;

import java.util.List;

/**
 * Serviço de música ambiente (sugestão #36), pensado desde o início para
 * crescer — exatamente como pedido no enunciado ("hoje existe uma opção
 * de música... quero algo preparado para crescer"):
 * <ul>
 *     <li>lista de músicas disponíveis ({@link #getPlaylist()});</li>
 *     <li>seleção dinâmica ({@link #proxima()}, {@link #anterior()});</li>
 *     <li>fácil adicionar novas músicas sem alterar outras classes
 *         ({@link #adicionarFaixa(Musica)} — ou simplesmente soltando um
 *         novo .wav na pasta de músicas, veja {@link ServicoMusicaClip});</li>
 *     <li>controle de volume ({@link #setVolume(float)});</li>
 *     <li>espaço para playlist/aleatório/repetição entrarem depois sem
 *         quebrar quem já usa esta interface.</li>
 * </ul>
 */
public interface ServicoMusica {

    List<Musica> getPlaylist();

    void adicionarFaixa(Musica musica);

    void tocar();

    void pausar();

    void parar();

    void proxima();

    void anterior();

    void setVolume(float volume);

    float getVolume();

    void setAtivado(boolean ativado);

    boolean isAtivado();

    Musica getFaixaAtual();

    boolean isTocando();
}
