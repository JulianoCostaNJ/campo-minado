package view.componentes;

import controller.AcoesAudio;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Mini player de música ambiente (sugestão #36): faixa atual,
 * anterior/próxima, ligar/desligar e volume. Nunca fala diretamente com
 * {@link audio.ServicoMusica} — só chama {@link AcoesAudio}, exatamente
 * como qualquer outro botão da View, e é o Controller quem devolve o
 * estado atualizado via {@link #atualizarEstado}.
 */
public class PainelMusica extends JPanel {

    private final JLabel rotuloFaixa = new JLabel("Música desativada");
    private final JButton botaoAnterior = new JButton("\u23EE");
    private final JButton botaoAlternar = new JButton("\u25B6");
    private final JButton botaoProxima = new JButton("\u23ED");
    private final JSlider controleVolume = new JSlider(0, 100, 60);

    private AcoesAudio ouvinte;
    private boolean atualizandoProgramaticamente;

    public PainelMusica() {
        super(new FlowLayout(FlowLayout.LEFT, 6, 2));
        setOpaque(false);

        controleVolume.setOpaque(false);
        controleVolume.setPreferredSize(new Dimension(80, 20));

        botaoAnterior.addActionListener(e -> { if (ouvinte != null) ouvinte.aoMusicaAnterior(); });
        botaoProxima.addActionListener(e -> { if (ouvinte != null) ouvinte.aoProximaMusica(); });
        botaoAlternar.addActionListener(e -> { if (ouvinte != null) ouvinte.aoAlternarMusica(); });
        controleVolume.addChangeListener(e -> {
            if (ouvinte != null && !atualizandoProgramaticamente && !controleVolume.getValueIsAdjusting()) {
                ouvinte.aoAjustarVolumeMusica(controleVolume.getValue() / 100f);
            }
        });

        add(new JLabel("\uD83C\uDFB5"));
        add(rotuloFaixa);
        add(botaoAnterior);
        add(botaoAlternar);
        add(botaoProxima);
        add(controleVolume);
    }

    public void setOuvinte(AcoesAudio ouvinte) {
        this.ouvinte = ouvinte;
    }

    public void atualizarEstado(String tituloFaixa, boolean tocando, boolean ativado, float volume) {
        if (!ativado) {
            rotuloFaixa.setText("Música desativada");
        } else {
            rotuloFaixa.setText(tituloFaixa != null ? tituloFaixa : "Sem faixas na pasta recursos/musicas");
        }
        botaoAlternar.setText(ativado && tocando ? "\u23F8" : "\u25B6");

        atualizandoProgramaticamente = true;
        controleVolume.setValue(Math.round(volume * 100));
        atualizandoProgramaticamente = false;
    }

    public void aplicarCorTexto(Color cor) {
        rotuloFaixa.setForeground(cor);
    }
}
