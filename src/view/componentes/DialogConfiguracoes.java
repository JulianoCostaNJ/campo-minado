package view.componentes;

import persistence.Configuracoes;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.function.Consumer;

/**
 * Painel de configurações (sugestão #46), reunindo tema, skins, áudio,
 * símbolos para daltonismo (sugestão #25) e densidade de minas padrão —
 * tudo que a sugestão #34 pede para ficar persistente entre execuções.
 */
public class DialogConfiguracoes extends JDialog {

    public DialogConfiguracoes(Frame dono, Configuracoes atual, List<String> nomesTemas,
                                List<String> nomesSkinsBandeira, List<String> nomesSkinsCelula,
                                Consumer<Configuracoes> aoSalvar) {
        super(dono, "Configurações", true);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 10, 6, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;

        JComboBox<String> comboTema = new JComboBox<>(nomesTemas.toArray(new String[0]));
        comboTema.setSelectedItem(atual.getNomeTema());

        JComboBox<String> comboSkinBandeira = new JComboBox<>(nomesSkinsBandeira.toArray(new String[0]));
        comboSkinBandeira.setSelectedItem(atual.getNomeSkinBandeira());

        JComboBox<String> comboSkinCelula = new JComboBox<>(nomesSkinsCelula.toArray(new String[0]));
        comboSkinCelula.setSelectedItem(atual.getNomeSkinCelula());

        JSlider sliderVolume = new JSlider(0, 100, Math.round(atual.getVolumeMusica() * 100));
        JSlider sliderDensidade = new JSlider(5, 30, (int) Math.round(atual.getDensidadeMinasPadrao() * 100));
        JSlider sliderZoom = new JSlider(20, 64, atual.getTamanhoCelula());

        JCheckBox checkSom = new JCheckBox("Efeitos sonoros", atual.isSomAtivado());
        JCheckBox checkMusica = new JCheckBox("Música ambiente", atual.isMusicaAtivada());
        JCheckBox checkDaltonico = new JCheckBox("Símbolos extras para daltonismo (não só cor)", atual.isSimbolosDaltonicos());

        adicionarLinha(c, "Tema:", comboTema);
        adicionarLinha(c, "Skin da bandeira:", comboSkinBandeira);
        adicionarLinha(c, "Skin das células:", comboSkinCelula);
        adicionarLinha(c, "Volume da música (%):", sliderVolume);
        adicionarLinha(c, "Densidade de minas padrão (%):", sliderDensidade);
        adicionarLinha(c, "Tamanho da célula (zoom):", sliderZoom);

        c.gridx = 0;
        c.gridwidth = 2;
        add(checkSom, c);
        c.gridy++;
        add(checkMusica, c);
        c.gridy++;
        add(checkDaltonico, c);

        JButton salvar = new JButton("Salvar");
        salvar.addActionListener(e -> {
            atual.setNomeTema((String) comboTema.getSelectedItem());
            atual.setNomeSkinBandeira((String) comboSkinBandeira.getSelectedItem());
            atual.setNomeSkinCelula((String) comboSkinCelula.getSelectedItem());
            atual.setSomAtivado(checkSom.isSelected());
            atual.setMusicaAtivada(checkMusica.isSelected());
            atual.setVolumeMusica(sliderVolume.getValue() / 100f);
            atual.setSimbolosDaltonicos(checkDaltonico.isSelected());
            atual.setDensidadeMinasPadrao(sliderDensidade.getValue() / 100.0);
            atual.setTamanhoCelula(sliderZoom.getValue());
            dispose();
            aoSalvar.accept(atual);
        });
        c.gridy++;
        add(salvar, c);

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
