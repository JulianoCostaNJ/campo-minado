package repository;

import persistence.PersistenciaException;
import persistence.RegistroPartida;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência e consulta do histórico de partidas. Reúne,
 * sobre a mesma lista de {@link RegistroPartida}, quatro sugestões da
 * lista que são, no fundo, a mesma informação vista de ângulos
 * diferentes:
 * <ul>
 *     <li>#30 — melhor tempo por dificuldade;</li>
 *     <li>#31 — histórico de partidas;</li>
 *     <li>#32 — exportação para CSV;</li>
 *     <li>#42 — ranking dos melhores tempos.</li>
 * </ul>
 */
public interface EstatisticasRepository {

    void registrarPartida(RegistroPartida registro) throws PersistenciaException;

    List<RegistroPartida> listarHistorico(String perfil) throws PersistenciaException;

    Optional<RegistroPartida> melhorTempo(String perfil, String dificuldade) throws PersistenciaException;

    /** Ranking dos melhores tempos de uma dificuldade, entre todos os perfis, do menor para o maior. */
    List<RegistroPartida> ranking(String dificuldade, int limite) throws PersistenciaException;

    void exportarCsv(String perfil, File destino) throws PersistenciaException;
}
