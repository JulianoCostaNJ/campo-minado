package audio;

/**
 * Uma faixa da playlist de música ambiente (sugestão #36).
 */
public final class Musica {

    private final String titulo;
    private final String caminhoArquivo;

    public Musica(String titulo, String caminhoArquivo) {
        this.titulo = titulo;
        this.caminhoArquivo = caminhoArquivo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }
}
