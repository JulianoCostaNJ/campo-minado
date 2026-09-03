package repository;

import persistence.Configuracoes;
import persistence.PersistenciaException;

/**
 * Contrato de persistência das configurações do jogador (sugestão #34).
 */
public interface ConfiguracoesRepository {

    void salvar(Configuracoes configuracoes) throws PersistenciaException;

    /** Nunca retorna vazio: se não houver nada salvo ainda, devolve {@link Configuracoes#padrao()}. */
    Configuracoes carregar() throws PersistenciaException;
}
