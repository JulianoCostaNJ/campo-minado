package controller;

/** Ações de troca de perfil de jogador (sugestão #33). */
public interface AcoesPerfil {

    void aoSelecionarPerfil(String nome);

    /** Reúne a lista de perfis existentes e pede à View para exibi-los para escolha/criação. */
    void aoAbrirSelecaoDePerfil();
}
