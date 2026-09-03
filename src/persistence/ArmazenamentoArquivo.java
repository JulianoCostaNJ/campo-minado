package persistence;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pequeno utilitário de E/S compartilhado por todos os repositórios
 * baseados em arquivo. Sem ele, {@code ArquivoSaveGameRepository},
 * {@code ArquivoEstatisticasRepository}, {@code ArquivoConfiguracoesRepository}
 * e {@code ArquivoPerfilRepository} repetiriam o mesmo
 * try/catch de {@link ObjectOutputStream}/{@link ObjectInputStream}
 * quatro vezes — o que violaria DRY e tornaria uma futura troca de
 * formato (JSON, XML, banco de dados) quatro vezes mais trabalhosa.
 * <p>
 * Cada repositório continua livre para trocar de estratégia de
 * persistência sem depender desta classe — ela só existe para não
 * duplicar a estratégia "serialização Java em arquivo", que é a usada
 * hoje por todos eles.
 */
public final class ArmazenamentoArquivo {

    private static final Path DIRETORIO_DADOS =
            Paths.get(System.getProperty("user.home", "."), ".campominado");

    private ArmazenamentoArquivo() {
    }

    /** Diretório onde os dados do jogo (saves, estatísticas, configurações) são guardados. */
    public static Path diretorioDados() {
        return DIRETORIO_DADOS;
    }

    public static Path resolverArquivo(String nomeArquivo) {
        return DIRETORIO_DADOS.resolve(nomeArquivo);
    }

    private static void garantirDiretorio() throws IOException {
        if (!Files.exists(DIRETORIO_DADOS)) {
            Files.createDirectories(DIRETORIO_DADOS);
        }
    }

    public static void salvar(Serializable objeto, Path caminho) throws PersistenciaException {
        try {
            garantirDiretorio();
            try (ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(caminho.toFile()))) {
                saida.writeObject(objeto);
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível salvar em " + caminho, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> carregar(Path caminho, Class<T> tipo) throws PersistenciaException {
        File arquivo = caminho.toFile();
        if (!arquivo.exists()) {
            return Optional.empty();
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(arquivo))) {
            Object lido = entrada.readObject();
            return Optional.of(tipo.cast(lido));
        } catch (EOFException e) {
            return Optional.empty();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new PersistenciaException("Não foi possível carregar " + caminho, e);
        }
    }

    /**
     * Acrescenta um objeto a uma lista persistida em arquivo (usado pelo
     * histórico de partidas, que cresce a cada jogo). Lê a lista inteira,
     * adiciona o item e regrava — simples e suficiente para o volume de
     * dados de um jogo local; uma implementação futura baseada em banco
     * de dados poderia trocar isso por um INSERT sem tocar em quem chama.
     */
    public static <T extends Serializable> void adicionarALista(T item, Path caminho, Class<T> tipo)
            throws PersistenciaException {
        List<T> lista = carregarLista(caminho, tipo);
        lista.add(item);
        salvar(new ArrayList<>(lista), caminho);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> List<T> carregarLista(Path caminho, Class<T> tipo)
            throws PersistenciaException {
        Optional<List> lida = carregar(caminho, List.class);
        if (!lida.isPresent()) {
            return new ArrayList<>();
        }
        return new ArrayList<>((List<T>) lida.get());
    }

    public static void excluir(Path caminho) {
        try {
            Files.deleteIfExists(caminho);
        } catch (IOException ignorada) {
            // Falha silenciosa: não existir mais o arquivo já é o resultado desejado.
        }
    }

    public static boolean existe(Path caminho) {
        return Files.exists(caminho);
    }
}
