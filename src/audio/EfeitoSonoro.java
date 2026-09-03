package audio;

/**
 * Catálogo de efeitos sonoros do jogo (sugestão #35). Adicionar um efeito
 * novo no futuro é só acrescentar mais uma constante aqui — nenhuma outra
 * classe precisa mudar, já que {@link ServicoSom} só sabe tocar
 * "um {@code EfeitoSonoro}", não uma lista de casos fixos.
 */
public enum EfeitoSonoro {

    CLIQUE("clique.wav"),
    BANDEIRA("bandeira.wav"),
    EXPLOSAO("explosao.wav"),
    VITORIA("vitoria.wav");

    private final String nomeArquivo;

    EfeitoSonoro(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }
}
