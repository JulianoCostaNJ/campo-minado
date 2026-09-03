package repository;

import persistence.PersistenciaException;
import persistence.SaveGame;

import java.util.Optional;

/**
 * Contrato de persistência para "salvar e retomar uma partida em
 * andamento" (sugestão #29, a única de nível difícil implementada).
 * <p>
 * O Controller depende apenas desta interface, nunca de
 * {@code ArquivoSaveGameRepository} diretamente — trocar o formato de
 * salvamento no futuro (JSON, um banco de dados local, sincronização em
 * nuvem) significa escrever uma nova implementação desta interface, sem
 * alterar uma linha do Controller ou da View. Isso é Inversão de
 * Dependência (o "D" do SOLID) aplicada ao pedido do enunciado de manter
 * a persistência fácil de trocar de formato.
 * <p>
 * O {@code perfil} identifica de quem é a partida salva — cada perfil
 * (sugestão #33) tem seu próprio slot, para que dois jogadores usando o
 * mesmo programa não sobrescrevam a partida um do outro.
 */
public interface SaveGameRepository {

    void salvar(String perfil, SaveGame jogo) throws PersistenciaException;

    Optional<SaveGame> carregar(String perfil) throws PersistenciaException;

    boolean existePartidaSalva(String perfil);

    void excluir(String perfil);
}
