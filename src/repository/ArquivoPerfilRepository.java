package repository;

import persistence.ArmazenamentoArquivo;
import persistence.PersistenciaException;
import persistence.Perfil;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Implementação de {@link PerfilRepository} que guarda todos os perfis
 * (normalmente poucos, um jogo local não tem milhares de jogadores) em
 * um único arquivo serializado.
 */
public class ArquivoPerfilRepository implements PerfilRepository {

    private static final Path ARQUIVO = ArmazenamentoArquivo.resolverArquivo("perfis.dat");

    @Override
    public List<Perfil> listarPerfis() throws PersistenciaException {
        return ArmazenamentoArquivo.carregarLista(ARQUIVO, Perfil.class);
    }

    @Override
    public Perfil obterOuCriar(String nome) throws PersistenciaException {
        List<Perfil> perfis = listarPerfis();
        Optional<Perfil> existente = perfis.stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome))
                .findFirst();
        if (existente.isPresent()) {
            return existente.get();
        }
        Perfil novo = new Perfil(nome);
        ArmazenamentoArquivo.adicionarALista(novo, ARQUIVO, Perfil.class);
        return novo;
    }

    @Override
    public void salvar(Perfil perfil) throws PersistenciaException {
        List<Perfil> perfis = listarPerfis();
        perfis.removeIf(p -> p.getNome().equalsIgnoreCase(perfil.getNome()));
        perfis.add(perfil);
        ArmazenamentoArquivo.salvar(new java.util.ArrayList<>(perfis), ARQUIVO);
    }
}
