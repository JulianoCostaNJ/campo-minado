package controller;

/**
 * Ações que não fazem parte do "núcleo" de revelar/marcar, mas também
 * não são persistência nem áudio: dica (#23), depuração (#13),
 * compartilhar resultado (#44) e replay (#41).
 */
public interface AcoesExtras {

    void aoPedirDica();

    void aoAlternarModoDebugMinasVisiveis();

    void aoCompartilharResultado();

    void aoIniciarReplay();

    /** Reúne histórico, ranking e conquistas do perfil ativo e pede à View para exibi-los (sugestões #31, #42, #50). */
    void aoAbrirEstatisticas();
}
