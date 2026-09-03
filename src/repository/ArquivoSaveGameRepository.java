package repository;

import persistence.ArmazenamentoArquivo;
import persistence.PersistenciaException;
import persistence.SaveGame;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Implementação de {@link SaveGameRepository} que grava a partida como
 * um arquivo binário serializado, um por perfil (sugestão #33) — assim,
 * dois jogadores usando o mesmo programa não sobrescrevem a partida um
 * do outro.
 * <p>
 * Guarda exatamente um "slot" de partida salva por perfil hoje. O
 * desenho já comporta evoluir para múltiplos slots no futuro: bastaria
 * trocar {@link #caminho(String)} para incluir um identificador de slot,
 * sem alterar a interface {@link SaveGameRepository} nem quem a consome.
 */
public class ArquivoSaveGameRepository implements SaveGameRepository {

    @Override
    public void salvar(String perfil, SaveGame jogo) throws PersistenciaException {
        ArmazenamentoArquivo.salvar(jogo, caminho(perfil));
    }

    @Override
    public Optional<SaveGame> carregar(String perfil) throws PersistenciaException {
        return ArmazenamentoArquivo.carregar(caminho(perfil), SaveGame.class);
    }

    @Override
    public boolean existePartidaSalva(String perfil) {
        return ArmazenamentoArquivo.existe(caminho(perfil));
    }

    @Override
    public void excluir(String perfil) {
        ArmazenamentoArquivo.excluir(caminho(perfil));
    }

    private Path caminho(String perfil) {
        String nomeSeguro = (perfil == null || perfil.isBlank()) ? "padrao" : perfil.trim();
        return ArmazenamentoArquivo.resolverArquivo("save-" + nomeSeguro + ".dat");
    }
}
