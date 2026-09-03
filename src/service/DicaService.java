package service;

import model.LeituraTabuleiro;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Escolhe uma célula segura para o botão de dica (sugestão #23). Só
 * enxerga o tabuleiro através de {@link LeituraTabuleiro} — quem decide
 * o que fazer com a célula sugerida (chamar {@code tabuleiro.revelar})
 * continua sendo o Controller, mantendo a regra de que só ele altera o
 * Model.
 */
public class DicaService {

    private final Random sorteio = new Random();

    /**
     * Sorteia uma célula segura, não revelada, não minada e sem
     * bandeira. Retorna {@code null} se não houver nenhuma candidata.
     */
    public int[] sugerirCelulaSegura(LeituraTabuleiro tabuleiro) {
        List<int[]> candidatas = new ArrayList<>();
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                if (!tabuleiro.isRevelada(i, j) && !tabuleiro.isMinada(i, j) && !tabuleiro.isMarcada(i, j)) {
                    candidatas.add(new int[]{i, j});
                }
            }
        }
        if (candidatas.isEmpty()) {
            return null;
        }
        return candidatas.get(sorteio.nextInt(candidatas.size()));
    }
}
