package persistence;

/**
 * Exceção do tipo checked para falhas de leitura/escrita na camada de
 * persistência. Ser checked é proposital: quem chama um repositório
 * precisa decidir conscientemente o que fazer se salvar ou carregar
 * falhar (por exemplo, avisar o jogador), em vez de deixar a falha
 * passar batido como uma RuntimeException ignorável.
 */
public class PersistenciaException extends Exception {

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }
}
