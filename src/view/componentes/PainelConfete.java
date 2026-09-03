package view.componentes;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Efeito de confete/partículas para a tela de vitória (sugestão #11).
 * Um componente transparente e temporário, exibido sobre o glass pane do
 * {@link JFrame} por alguns segundos e removido automaticamente — a View
 * só precisa chamar {@link #exibirSobre(JFrame, int)} uma vez.
 */
public class PainelConfete extends JComponent {

    private static final Color[] CORES = {
            new Color(255, 99, 132), new Color(255, 206, 86), new Color(75, 192, 192),
            new Color(153, 102, 255), new Color(255, 159, 64), new Color(100, 220, 120)
    };

    private final List<Particula> particulas = new ArrayList<>();
    private final Random sorteio = new Random();
    private Timer timerAnimacao;

    private static final class Particula {
        float x;
        float y;
        float velocidadeY;
        float velocidadeX;
        float rotacao;
        float velocidadeRotacao;
        Color cor;
        int tamanho;
    }

    public PainelConfete(int largura, int altura) {
        setOpaque(false);
        int larguraSegura = Math.max(1, largura);
        int alturaSegura = Math.max(1, altura);
        for (int i = 0; i < 120; i++) {
            Particula p = new Particula();
            p.x = sorteio.nextInt(larguraSegura);
            p.y = -sorteio.nextInt(alturaSegura);
            p.velocidadeY = 2 + sorteio.nextFloat() * 4;
            p.velocidadeX = (sorteio.nextFloat() - 0.5f) * 2;
            p.rotacao = sorteio.nextFloat() * 360;
            p.velocidadeRotacao = (sorteio.nextFloat() - 0.5f) * 12;
            p.cor = CORES[sorteio.nextInt(CORES.length)];
            p.tamanho = 5 + sorteio.nextInt(6);
            particulas.add(p);
        }
    }

    /** Sobe este painel no glass pane do frame e inicia a animação, removendo-se sozinho ao final. */
    public void exibirSobre(JFrame frame, int duracaoMs) {
        setBounds(0, 0, frame.getWidth(), frame.getHeight());
        frame.setGlassPane(this);
        setVisible(true);

        timerAnimacao = new Timer(30, e -> {
            for (Particula p : particulas) {
                p.y += p.velocidadeY;
                p.x += p.velocidadeX;
                p.rotacao += p.velocidadeRotacao;
            }
            repaint();
        });
        timerAnimacao.start();

        Timer encerrar = new Timer(duracaoMs, e -> {
            timerAnimacao.stop();
            setVisible(false);
        });
        encerrar.setRepeats(false);
        encerrar.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Particula p : particulas) {
            g2.setColor(p.cor);
            g2.translate(p.x, p.y);
            g2.rotate(Math.toRadians(p.rotacao));
            g2.fillRect(-p.tamanho / 2, -p.tamanho / 2, p.tamanho, p.tamanho);
            g2.rotate(-Math.toRadians(p.rotacao));
            g2.translate(-p.x, -p.y);
        }
        g2.dispose();
    }
}
