package audio;

/**
 * Serviço de efeitos sonoros (sugestão #35). O Controller depende só
 * desta interface — a implementação por trás dela (hoje,
 * {@code javax.sound.sampled}) pode ser trocada sem que o resto do
 * projeto perceba.
 */
public interface ServicoSom {

    void tocar(EfeitoSonoro efeito);

    void setAtivado(boolean ativado);

    boolean isAtivado();
}
