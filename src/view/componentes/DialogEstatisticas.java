package view.componentes;

import persistence.RegistroPartida;
import service.Conquista;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Janela de estatísticas: histórico de partidas (sugestão #31), ranking
 * dos melhores tempos (sugestão #42), conquistas desbloqueadas (sugestão
 * #50) e exportação para CSV (sugestão #32). Recebe os dados já prontos
 * do Controller — não conhece nenhum repositório, só exibe o que
 * recebeu.
 */
public class DialogEstatisticas extends JDialog {

    public DialogEstatisticas(Frame dono, List<RegistroPartida> historico, List<RegistroPartida> ranking,
                               Set<String> conquistasDesbloqueadas, Consumer<File> aoExportarCsv) {
        super(dono, "Estatísticas", true);
        setLayout(new BorderLayout(8, 8));
        setPreferredSize(new Dimension(560, 460));

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Histórico", criarTabelaHistorico(historico));
        abas.addTab("Ranking", criarTabelaRanking(ranking));
        abas.addTab("Conquistas", criarListaConquistas(conquistasDesbloqueadas));
        add(abas, BorderLayout.CENTER);

        JButton exportar = new JButton("Exportar histórico em CSV...");
        exportar.addActionListener(e -> {
            JFileChooser seletor = new JFileChooser();
            seletor.setSelectedFile(new File("estatisticas-campo-minado.csv"));
            if (seletor.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                aoExportarCsv.accept(seletor.getSelectedFile());
            }
        });
        add(exportar, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(dono);
    }

    private JScrollPane criarTabelaHistorico(List<RegistroPartida> historico) {
        String[] colunas = {"Dificuldade", "Resultado", "Tempo", "Jogadas", "Modo", "Data"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0);
        for (RegistroPartida r : historico) {
            modelo.addRow(new Object[]{
                    r.getDificuldade(), r.isVitoria() ? "Vitória" : "Derrota",
                    formatarTempo(r.getTempoSegundos()), r.getJogadas(), r.getModoJogadores(), r.getDataHora()
            });
        }
        return new JScrollPane(new JTable(modelo));
    }

    private JScrollPane criarTabelaRanking(List<RegistroPartida> ranking) {
        String[] colunas = {"#", "Jogador", "Tempo", "Data"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0);
        int posicao = 1;
        for (RegistroPartida r : ranking) {
            modelo.addRow(new Object[]{posicao++, r.getPerfil(), formatarTempo(r.getTempoSegundos()), r.getDataHora()});
        }
        return new JScrollPane(new JTable(modelo));
    }

    private JComponent criarListaConquistas(Set<String> desbloqueadas) {
        DefaultListModel<String> modelo = new DefaultListModel<>();
        for (Conquista conquista : Conquista.values()) {
            boolean tem = desbloqueadas.contains(conquista.name());
            String marcador = tem ? "\u2705 " : "\uD83D\uDD12 ";
            modelo.addElement(marcador + conquista.getTitulo() + " — " + conquista.getDescricao());
        }
        return new JScrollPane(new JList<>(modelo));
    }

    private String formatarTempo(int segundos) {
        return String.format("%02d:%02d", segundos / 60, segundos % 60);
    }
}
