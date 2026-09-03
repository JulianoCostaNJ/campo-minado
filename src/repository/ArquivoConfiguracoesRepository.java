package repository;

import persistence.ArmazenamentoArquivo;
import persistence.Configuracoes;
import persistence.PersistenciaException;

import java.nio.file.Path;

/**
 * Implementação de {@link ConfiguracoesRepository} baseada em um único
 * arquivo serializado.
 */
public class ArquivoConfiguracoesRepository implements ConfiguracoesRepository {

    private static final Path ARQUIVO = ArmazenamentoArquivo.resolverArquivo("config.dat");

    @Override
    public void salvar(Configuracoes configuracoes) throws PersistenciaException {
        ArmazenamentoArquivo.salvar(configuracoes, ARQUIVO);
    }

    @Override
    public Configuracoes carregar() throws PersistenciaException {
        return ArmazenamentoArquivo.carregar(ARQUIVO, Configuracoes.class)
                .orElseGet(Configuracoes::padrao);
    }
}
