import model.RegrasJogo;
import model.RegraJogoException;
import model.Tabuleiro;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários cobrindo contagem de minas vizinhas, o efeito cascata,
 * a condição de vitória e as novas variantes de regra do Model
 * (sugestões #12, #16, #17, #18, #20, #29 do documento de sugestões).
 * <p>
 * Todos os testes usam o construtor de {@link Tabuleiro} que recebe as
 * posições das minas explicitamente, para que o resultado seja
 * determinístico (sem depender do sorteio aleatório).
 * <p>
 * Nota de manutenção: este arquivo, como recebido originalmente, não
 * tinha nenhuma declaração de pacote nem import de {@code Tabuleiro} —
 * embora {@code Tabuleiro} esteja no pacote {@code model}. Isso fazia
 * essa classe não compilar de fato contra o restante do projeto (o
 * próprio comando de compilação do README nunca incluía {@code src/test}
 * junto). Corrigido aqui apenas adicionando os imports que faltavam;
 * nenhuma asserção dos testes originais foi alterada.
 */
public class CampoMinadoTest {

    @Test
    void testContagemDeMinasVizinhas() {
        // Tabuleiro 3x3 com uma única mina no centro (1,1).
        // Todas as 8 células ao redor devem contar exatamente 1 mina vizinha.
        int[][] minas = { { 1, 1 } };
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas);

        assertEquals(1, tabuleiro.getCelula(0, 0).getMinasVizinhas());
        assertEquals(1, tabuleiro.getCelula(0, 1).getMinasVizinhas());
        assertEquals(1, tabuleiro.getCelula(2, 2).getMinasVizinhas());
        // A própria célula minada não conta a si mesma.
        assertEquals(0, tabuleiro.getCelula(1, 1).getMinasVizinhas());
    }
    @Test
    void testTabuleiroRejeitaQuantidadeDeMinasInvalida() {
        // 9 minas num tabuleiro 3x3 (9 células) é uma regra de jogo
        // violada: não sobraria nenhuma célula segura.
        assertThrows(RegraJogoException.class, () -> new Tabuleiro(3, 3, 9));
    }
    
    @Test
    void testContagemDeMinasVizinhasComDuasMinasAdjacentes() {
        // Duas minas lado a lado: a célula (0,2) tem ambas como vizinhas.
        int[][] minas = { { 0, 0 }, { 0, 1 } };
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas);

        assertEquals(2, tabuleiro.getCelula(1, 0).getMinasVizinhas());
        assertEquals(2, tabuleiro.getCelula(0, 2).getMinasVizinhas());
        assertEquals(1, tabuleiro.getCelula(2, 2).getMinasVizinhas());
    }

    @Test
    void testCascataRevelaTodasAsCelulasSemMinasProximas() {
        // Tabuleiro 4x4 sem nenhuma mina: revelar qualquer célula deve
        // revelar o tabuleiro inteiro via cascata.
        int[][] semMinas = {};
        Tabuleiro tabuleiro = new Tabuleiro(4, 4, semMinas);

        tabuleiro.revelar(0, 0);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertTrue(tabuleiro.getCelula(i, j).isRevelada(),
                        "Célula (" + i + "," + j + ") deveria ter sido revelada pela cascata");
            }
        }
    }

    @Test
    void testCascataNaoRevelaMinaNemPassaDelaAdiante() {
        // Mina isolada em (2,2) de um tabuleiro 5x5. Revelar (0,0), que
        // está longe da mina, deve espalhar a cascata mas nunca revelar
        // a célula minada.
        int[][] minas = { { 2, 2 } };
        Tabuleiro tabuleiro = new Tabuleiro(5, 5, minas);

        tabuleiro.revelar(0, 0);

        assertFalse(tabuleiro.getCelula(2, 2).isRevelada(),
                "A célula minada nunca deve ser revelada pela cascata");
        // As células vizinhas à mina, por terem minasVizinhas > 0, devem
        // ter sido reveladas (mostram o número), mas a cascata para nelas
        // e não avança para dentro da mina.
        assertTrue(tabuleiro.getCelula(1, 2).isRevelada());
    }

    @Test
    void testRevelarCelulaMinadaEncerraOJogoComDerrota() {
        int[][] minas = { { 1, 1 } };
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas);

        tabuleiro.revelar(1, 1);

        assertTrue(tabuleiro.isJogoEncerrado());
        assertTrue(tabuleiro.isDerrota());
        assertTrue(tabuleiro.getCelula(1, 1).isRevelada());
    }

    @Test
    void testMarcarEDesmarcarCelulaComBandeira() {
        int[][] minas = { { 0, 0 } };
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas);

        assertFalse(tabuleiro.getCelula(1, 1).isMarcada());

        tabuleiro.alternarMarcacao(1, 1);
        assertTrue(tabuleiro.getCelula(1, 1).isMarcada());

        tabuleiro.alternarMarcacao(1, 1);
        assertFalse(tabuleiro.getCelula(1, 1).isMarcada());
    }

    @Test
    void testCelulaMarcadaNaoPodeSerRevelada() {
        int[][] semMinas = {};
        Tabuleiro tabuleiro = new Tabuleiro(2, 2, semMinas);

        tabuleiro.alternarMarcacao(0, 0);
        tabuleiro.revelar(0, 0);

        assertFalse(tabuleiro.getCelula(0, 0).isRevelada(),
                "Uma célula marcada com bandeira não deve ser revelada");
    }

    @Test
    void testVerificarVitoriaQuandoTodasAsCelulasSeguraForamReveladas() {
        // Tabuleiro 2x2 com uma mina: vitória ocorre quando as outras
        // 3 células (sem mina) forem reveladas.
        int[][] minas = { { 0, 0 } };
        Tabuleiro tabuleiro = new Tabuleiro(2, 2, minas);

        assertFalse(tabuleiro.verificarVitoria());

        tabuleiro.revelar(0, 1);
        tabuleiro.revelar(1, 0);
        tabuleiro.revelar(1, 1);

        assertTrue(tabuleiro.verificarVitoria());
        assertTrue(tabuleiro.isJogoEncerrado());
        assertFalse(tabuleiro.isDerrota());
    }

    @Test
    void testVerificarVitoriaEhFalsaEnquantoHouverCelulaSeguraNaoRevelada() {
        int[][] minas = { { 0, 0 } };
        Tabuleiro tabuleiro = new Tabuleiro(2, 2, minas);

        tabuleiro.revelar(0, 1);

        assertFalse(tabuleiro.verificarVitoria());
    }

    // ================================================================
    // Novos testes: marcação de três estados (sugestão #20)
    // ================================================================

    @Test
    void testMarcacaoCiclaEntreBandeiraInterrogacaoENenhuma() {
        int[][] semMinas = {};
        Tabuleiro tabuleiro = new Tabuleiro(2, 2, semMinas);

        assertFalse(tabuleiro.getCelula(0, 0).isMarcada());
        assertFalse(tabuleiro.getCelula(0, 0).isInterrogada());

        tabuleiro.alternarMarcacao(0, 0); // nenhuma -> bandeira
        assertTrue(tabuleiro.getCelula(0, 0).isMarcada());
        assertFalse(tabuleiro.getCelula(0, 0).isInterrogada());

        tabuleiro.alternarMarcacao(0, 0); // bandeira -> interrogação
        assertFalse(tabuleiro.getCelula(0, 0).isMarcada());
        assertTrue(tabuleiro.getCelula(0, 0).isInterrogada());

        tabuleiro.alternarMarcacao(0, 0); // interrogação -> nenhuma
        assertFalse(tabuleiro.getCelula(0, 0).isMarcada());
        assertFalse(tabuleiro.getCelula(0, 0).isInterrogada());
    }

    @Test
    void testCelulaInterrogadaPodeSerRevelada() {
        // Ao contrário da bandeira, a interrogação é só um lembrete
        // visual — não deve impedir a revelação.
        int[][] semMinas = {};
        Tabuleiro tabuleiro = new Tabuleiro(2, 2, semMinas);

        tabuleiro.alternarMarcacao(0, 0);
        tabuleiro.alternarMarcacao(0, 0); // agora está em interrogação

        tabuleiro.revelar(0, 0);

        assertTrue(tabuleiro.getCelula(0, 0).isRevelada());
    }

    // ================================================================
    // Novos testes: modo "sem cascata" (sugestão #12)
    // ================================================================

    @Test
    void testModoSemCascataRevelaApenasACelulaClicada() {
        int[][] semMinas = {};
        RegrasJogo regras = RegrasJogo.construir().semCascata().build();
        Tabuleiro tabuleiro = new Tabuleiro(4, 4, semMinas, regras);

        tabuleiro.revelar(0, 0);

        assertTrue(tabuleiro.getCelula(0, 0).isRevelada());
        assertFalse(tabuleiro.getCelula(0, 1).isRevelada());
        assertFalse(tabuleiro.getCelula(1, 1).isRevelada());
    }

    // ================================================================
    // Novos testes: modo "3 vidas" (sugestão #17)
    // ================================================================

    @Test
    void testModoVariasVidasNaoEncerraJogoAntesDeEsgotarAsVidas() {
        int[][] minas = { { 0, 0 }, { 0, 1 } };
        RegrasJogo regras = RegrasJogo.construir().comVidas(3).build();
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas, regras);

        tabuleiro.revelar(0, 0); // 1ª mina
        assertFalse(tabuleiro.isJogoEncerrado());
        assertEquals(2, tabuleiro.getVidasRestantes());

        tabuleiro.revelar(0, 1); // 2ª mina
        assertFalse(tabuleiro.isJogoEncerrado());
        assertEquals(1, tabuleiro.getVidasRestantes());
    }

    @Test
    void testModoVariasVidasEncerraJogoQuandoAsVidasAcabam() {
        int[][] minas = { { 0, 0 }, { 0, 1 }, { 0, 2 } };
        RegrasJogo regras = RegrasJogo.construir().comVidas(3).build();
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas, regras);

        tabuleiro.revelar(0, 0);
        tabuleiro.revelar(0, 1);
        assertFalse(tabuleiro.isJogoEncerrado());

        tabuleiro.revelar(0, 2); // 3ª mina, esgota as vidas
        assertTrue(tabuleiro.isJogoEncerrado());
        assertTrue(tabuleiro.isDerrota());
        assertEquals(0, tabuleiro.getVidasRestantes());
    }

    // ================================================================
    // Novos testes: tabuleiro toroidal (sugestão #18)
    // ================================================================

    @Test
    void testTabuleiroToroidalConectaAsBordas() {
        // Mina no canto (0,0) de um 3x3 toroidal: como as bordas se
        // conectam, TODAS as outras 8 células passam a ser vizinhas
        // dela — inclusive (2,2), que num tabuleiro comum estaria a
        // distância 2 e não contaria a mina.
        int[][] minas = { { 0, 0 } };
        RegrasJogo regras = RegrasJogo.construir().toroidal().build();
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas, regras);

        assertEquals(1, tabuleiro.getCelula(2, 2).getMinasVizinhas());
        assertEquals(1, tabuleiro.getCelula(2, 0).getMinasVizinhas());
        assertEquals(1, tabuleiro.getCelula(0, 2).getMinasVizinhas());
    }

    @Test
    void testTabuleiroNaoToroidalNaoConectaAsBordas() {
        // Mesmo cenário, mas sem a regra toroidal: (2,2) fica longe
        // demais da mina em (0,0) para contá-la como vizinha.
        int[][] minas = { { 0, 0 } };
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas);

        assertEquals(0, tabuleiro.getCelula(2, 2).getMinasVizinhas());
    }

    // ================================================================
    // Novos testes: modo relâmpago (sugestão #16) e semente (sugestão #49)
    // ================================================================

    @Test
    void testModoRelampagoRevelaCelulasSegurasAntesDeComecar() {
        int[][] minas = { { 0, 0 }, { 0, 1 } };
        RegrasJogo regras = RegrasJogo.construir().semCascata().comRelampago(3).build();
        Tabuleiro tabuleiro = new Tabuleiro(4, 4, minas, regras);

        int reveladas = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (tabuleiro.getCelula(i, j).isRevelada()) {
                    reveladas++;
                    assertFalse(tabuleiro.getCelula(i, j).isMinada(),
                            "O modo relâmpago nunca deve revelar uma mina");
                }
            }
        }
        assertEquals(3, reveladas);
    }

    @Test
    void testSementeFixaProduzOMesmoTabuleiro() {
        RegrasJogo regras = RegrasJogo.construir().comSemente(42L).build();
        Tabuleiro tabuleiroA = new Tabuleiro(9, 9, 10, regras);
        Tabuleiro tabuleiroB = new Tabuleiro(9, 9, 10, regras);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                assertEquals(tabuleiroA.isMinada(i, j), tabuleiroB.isMinada(i, j),
                        "Dois tabuleiros com a mesma semente devem ter minas nas mesmas posições");
            }
        }
    }

    @Test
    void testObterPosicoesMinasRetornaTodasAsMinas() {
        int[][] minas = { { 1, 1 }, { 2, 2 } };
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas);

        assertEquals(2, tabuleiro.obterPosicoesMinas().size());
    }

    // ================================================================
    // Novos testes: RegrasJogo (Builder)
    // ================================================================

    @Test
    void testRegrasPadraoMantemComportamentoClassico() {
        RegrasJogo regras = RegrasJogo.padrao();

        assertTrue(regras.isCascataAtiva());
        assertEquals(1, regras.getVidasIniciais());
        assertFalse(regras.isToroidal());
        assertFalse(regras.isModoRelampago());
        assertNull(regras.getSemente());
    }

    @Test
    void testBuilderNaoAceitaMenosDeUmaVida() {
        assertThrows(RegraJogoException.class, () -> RegrasJogo.construir().comVidas(0));
    }
}
