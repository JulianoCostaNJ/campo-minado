package repository;

import persistence.ArmazenamentoArquivo;
import persistence.PersistenciaException;
import persistence.RegistroPartida;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementação de {@link EstatisticasRepository} que guarda todo o
 * histórico de partidas (de todos os perfis) em um único arquivo
 * serializado.
 */
public class ArquivoEstatisticasRepository implements EstatisticasRepository {

    private static final Path ARQUIVO = ArmazenamentoArquivo.resolverArquivo("historico.dat");
    private static final String CABECALHO_CSV = "perfil,dificuldade,vitoria,tempoSegundos,jogadas,modo,dataHora";

    @Override
    public void registrarPartida(RegistroPartida registro) throws PersistenciaException {
        ArmazenamentoArquivo.adicionarALista(registro, ARQUIVO, RegistroPartida.class);
    }

    @Override
    public List<RegistroPartida> listarHistorico(String perfil) throws PersistenciaException {
        return todos().stream()
                .filter(r -> r.getPerfil().equals(perfil))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RegistroPartida> melhorTempo(String perfil, String dificuldade) throws PersistenciaException {
        List<RegistroPartida> candidatos = todos().stream()
                .filter(r -> r.getPerfil().equals(perfil))
                .filter(r -> r.getDificuldade().equals(dificuldade))
                .filter(RegistroPartida::isVitoria)
                .collect(Collectors.toList());

        if (candidatos.isEmpty()) {
            return Optional.empty();
        }
        ordenarPorTempo(candidatos);
        return Optional.of(candidatos.get(0));
    }

    @Override
    public List<RegistroPartida> ranking(String dificuldade, int limite) throws PersistenciaException {
        List<RegistroPartida> vitoriasDaDificuldade = todos().stream()
                .filter(r -> r.getDificuldade().equals(dificuldade))
                .filter(RegistroPartida::isVitoria)
                .collect(Collectors.toList());

        ordenarPorTempo(vitoriasDaDificuldade);

        return vitoriasDaDificuldade.size() > limite
                ? new ArrayList<>(vitoriasDaDificuldade.subList(0, limite))
                : vitoriasDaDificuldade;
    }

    @Override
    public void exportarCsv(String perfil, File destino) throws PersistenciaException {
        List<RegistroPartida> registros = listarHistorico(perfil);
        try (Writer escritor = new FileWriter(destino)) {
            escritor.write(CABECALHO_CSV);
            escritor.write(System.lineSeparator());
            for (RegistroPartida registro : registros) {
                escritor.write(registro.paraLinhaCsv());
                escritor.write(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível exportar o CSV em " + destino, e);
        }
    }

    private List<RegistroPartida> todos() throws PersistenciaException {
        return ArmazenamentoArquivo.carregarLista(ARQUIVO, RegistroPartida.class);
    }

    /**
     * Ordenação por inserção, implementada manualmente em vez de usar
     * {@code Collections.sort} ou {@code List.sort} — pedido explícito da
     * sugestão #42 ("implementando manualmente o algoritmo de ordenação,
     * ótimo gancho para Estrutura de Dados"). O(n²), mas a lista de
     * partidas de um jogo local é pequena o bastante para isso nunca
     * pesar.
     */
    private void ordenarPorTempo(List<RegistroPartida> registros) {
        for (int i = 1; i < registros.size(); i++) {
            RegistroPartida atual = registros.get(i);
            int j = i - 1;
            while (j >= 0 && registros.get(j).getTempoSegundos() > atual.getTempoSegundos()) {
                registros.set(j + 1, registros.get(j));
                j--;
            }
            registros.set(j + 1, atual);
        }
    }
}
