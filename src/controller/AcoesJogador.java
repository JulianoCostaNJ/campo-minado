package controller;

/**
 * CONTRATO da arquitetura MVC entre View e Controller: toda interação do
 * jogador chega ao Controller através desta interface. A View nunca
 * decide o que um clique "significa" em termos de regra — ela só chama
 * um destes métodos e espera ser chamada de volta para redesenhar.
 * <p>
 * Estende as interfaces menores {@link AcoesPersistencia},
 * {@link AcoesAudio}, {@link AcoesPerfil} e {@link AcoesExtras} para que
 * cada família de ações fique documentada separadamente (Interface
 * Segregation), mas continua sendo uma única interface do ponto de vista
 * de quem liga a View ao Controller — a View guarda uma única referência
 * de "ouvinte", como já acontecia antes de todas essas ações extras
 * existirem.
 */
public interface AcoesJogador extends AcoesPersistencia, AcoesAudio, AcoesPerfil, AcoesExtras {

    /** Compatível com o comportamento original: dificuldade clássica, regras padrão. */
    void aoEscolherDificuldade(int linhas, int colunas, int minas);

    /** Versão estendida: permite regras customizadas, modo de jogadores e um nome de dificuldade. */
    void aoEscolherDificuldade(ConfiguracaoPartida configuracao);

    void aoPedirNovoJogo();

    void aoMarcarCelula(int linha, int coluna);

    void aoRevelarCelula(int linha, int coluna);
}
