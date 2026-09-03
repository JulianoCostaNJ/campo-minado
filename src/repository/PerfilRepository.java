package repository;

import persistence.PersistenciaException;
import persistence.Perfil;

import java.util.List;

/**
 * Contrato de persistência dos perfis de jogador (sugestão #33).
 */
public interface PerfilRepository {

    List<Perfil> listarPerfis() throws PersistenciaException;

    /** Devolve o perfil existente com esse nome, ou cria (e já persiste) um novo. */
    Perfil obterOuCriar(String nome) throws PersistenciaException;

    void salvar(Perfil perfil) throws PersistenciaException;
}
