package view.componentes;

import controller.ConfiguracaoPartida;
import model.ModoJogadores;
import model.RegrasJogo;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

/**
 * Diálogo de dificuldade personalizada (sugestão #14). Por ser o lugar
 * natural para configurar como a partida vai funcionar, também dá acesso
 * às variantes de regra do Model (sugestões #12 "sem cascata", #17 "3
 * vidas", #18 "toroidal", #16 "relâmpago") e ao modo de jogadores
 * (sugestões #19 "cooperativo", #43 "competitivo"), sem precisar de mais
 * telas na tela inicial.
 */
public class DialogDificuldadeCustom extends JDialog {

    public DialogDificuldadeCustom(Frame dono, double densidadeMinasSugerida,
                                    Consumer<ConfiguracaoPartida> aoConfirmar) {
        super(dono, "Dificuldade personalizada", true);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;

        JSpinner campoLinhas = new JSpinner(new SpinnerNumberModel(9, 5, 40, 1));
        JSpinner campoColunas = new JSpinner(new SpinnerNumberModel(9, 5, 40, 1));
        int minasSugeridas = Math.max(1, (int) Math.round(81 * densidadeMinasSugerida));
        JSpinner campoMinas = new JSpinner(new SpinnerNumberModel(minasSugeridas, 1, 39 * 39, 1));

        JCheckBox semCascata = new JCheckBox("Modo sem cascata (revela só a célula clicada)");
        JCheckBox variasVidas = new JCheckBox("Modo 3 vidas (tolera até 3 minas)");
        JCheckBox toroidal = new JCheckBox("Tabuleiro toroidal (bordas conectadas)");
        JCheckBox relampago = new JCheckBox("Modo relâmpago (revela células seguras no início)");

        JComboBox<String> comboModo = new JComboBox<>(new String[]{
                "Um jogador",
                "Cooperativo (revezar no mesmo tabuleiro)",
                "Competitivo (tempo, tabuleiros iguais)"
        });

        adicionarLinha(c, "Linhas:", campoLinhas);
        adicionarLinha(c, "Colunas:", campoColunas);
        adicionarLinha(c, "Minas:", campoMinas);
        adicionarLinha(c, "Modo de jogadores:", comboModo);

        c.gridx = 0;
        c.gridwidth = 2;
        add(semCascata, c);
        c.gridy++;
        add(variasVidas, c);
        c.gridy++;
        add(toroidal, c);
        c.gridy++;
        add(relampago, c);

        JButton confirmar = new JButton("Começar partida");
        confirmar.addActionListener(e -> {
            int linhas = (Integer) campoLinhas.getValue();
            int colunas = (Integer) campoColunas.getValue();
            int minas = (Integer) campoMinas.getValue();
            if (minas >= linhas * colunas) {
                JOptionPane.showMessageDialog(this,
                        "O número de minas precisa ser menor que o total de células.",
                        "Configuração inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            RegrasJogo.Builder regras = RegrasJogo.construir();
            if (semCascata.isSelected()) {
                regras.semCascata();
            }
            if (variasVidas.isSelected()) {
                regras.comVidas(3);
            }
            if (toroidal.isSelected()) {
                regras.toroidal();
            }
            if (relampago.isSelected()) {
                regras.comRelampago(3);
            }

            ModoJogadores modo;
            switch (comboModo.getSelectedIndex()) {
                case 1:
                    modo = ModoJogadores.COOPERATIVO;
                    break;
                case 2:
                    modo = ModoJogadores.COMPETITIVO;
                    break;
                default:
                    modo = ModoJogadores.INDIVIDUAL;
            }

            ConfiguracaoPartida configuracao =
                    new ConfiguracaoPartida(linhas, colunas, minas, regras.build(), modo, "Personalizado");
            dispose();
            aoConfirmar.accept(configuracao);
        });

        c.gridy++;
        add(confirmar, c);

        pack();
        setLocationRelativeTo(dono);
    }

    private void adicionarLinha(GridBagConstraints c, String rotulo, JComponent campo) {
        c.gridx = 0;
        c.gridwidth = 1;
        add(new JLabel(rotulo), c);
        c.gridx = 1;
        add(campo, c);
        c.gridy++;
    }
}
