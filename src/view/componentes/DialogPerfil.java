package view.componentes;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.List;
import java.util.function.Consumer;

/**
 * Diálogo de escolha/criação de perfil de jogador (sugestão #33).
 */
public class DialogPerfil extends JDialog {

    public DialogPerfil(Frame dono, List<String> perfisExistentes, String perfilAtual, Consumer<String> aoEscolher) {
        super(dono, "Escolher perfil", true);
        setLayout(new BorderLayout(8, 8));

        DefaultListModel<String> modelo = new DefaultListModel<>();
        perfisExistentes.forEach(modelo::addElement);
        JList<String> lista = new JList<>(modelo);
        lista.setSelectedValue(perfilAtual, true);
        add(new JScrollPane(lista), BorderLayout.CENTER);

        JButton usarSelecionado = new JButton("Usar perfil selecionado");
        usarSelecionado.addActionListener(e -> {
            String selecionado = lista.getSelectedValue();
            if (selecionado != null) {
                dispose();
                aoEscolher.accept(selecionado);
            }
        });
        add(usarSelecionado, BorderLayout.NORTH);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JTextField campoNovo = new JTextField(12);
        JButton criar = new JButton("Criar/usar este nome");
        criar.addActionListener(e -> {
            String nome = campoNovo.getText().trim();
            if (!nome.isEmpty()) {
                dispose();
                aoEscolher.accept(nome);
            }
        });
        rodape.add(new JLabel("Novo perfil:"));
        rodape.add(campoNovo);
        rodape.add(criar);
        add(rodape, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(320, 360));
        pack();
        setLocationRelativeTo(dono);
    }
}
