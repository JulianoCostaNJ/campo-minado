package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Representa o tabuleiro do Campo Minado: uma matriz bidimensional de
 * {@link Celula}. É a única classe que conhece a grade inteira, que sabe
 * posicionar minas e calcular vizinhança.
 * <p>
 * Parte do MODEL na arquitetura MVC. Implementa {@link LeituraTabuleiro}
 * para que a View possa consultar o estado do jogo sem depender da API
 * completa (mutável) desta classe. Implementa {@link Serializable} para
 * permitir salvar e retomar uma partida em andamento (sugestão #29).
 * <p>
 * As variantes de regra (sem cascata, várias vidas, tabuleiro toroidal,
 * modo relâmpago, semente fixa) ficam centralizadas em {@link RegrasJogo}
 * — os construtores clássicos (sem esse parâmetro) continuam existindo e
 * se comportando exatamente como antes, para não quebrar código já
 * escrito contra esta classe.
 */
public class Tabuleiro implements LeituraTabuleiro, Serializable {

    private static final long serialVersionUID = 1L;

    private final int linhas;
    private final int colunas;
    private final int numMinas;
    private final Celula[][] grade;
    private final RegrasJogo regras;

    private boolean jogoEncerrado;
    private boolean derrota;
    private int vidasRestantes;

    /**
     * Cria um tabuleiro novo com minas posicionadas aleatoriamente e
     * regras clássicas (cascata normal, uma vida, sem bordas conectadas).
     *
     * @param linhas   número de linhas do tabuleiro
     * @param colunas  número de colunas do tabuleiro
     * @param numMinas quantidade de minas a posicionar
     */
    public Tabuleiro(int linhas, int colunas, int numMinas) {
        this(linhas, colunas, numMinas, RegrasJogo.padrao());
    }

    /**
     * Cria um tabuleiro novo com minas posicionadas aleatoriamente,
     * seguindo as {@link RegrasJogo} informadas (sugestões #12, #16, #17,
     * #18, #49).
     */
    public Tabuleiro(int linhas, int colunas, int numMinas, RegrasJogo regras) {
        if (linhas <= 0 || colunas <= 0) {
            throw new IllegalArgumentException("Linhas e colunas devem ser maiores que zero.");
        }
        if (numMinas < 0 || numMinas >= linhas * colunas) {
            throw new IllegalArgumentException("Número de minas inválido para esse tabuleiro.");
        }

        this.linhas = linhas;
        this.colunas = colunas;
        this.numMinas = numMinas;
        this.regras = regras == null ? RegrasJogo.padrao() : regras;
        this.vidasRestantes = this.regras.getVidasIniciais();
        this.grade = new Celula[linhas][colunas];
        inicializarGrade();
        posicionarMinasAleatoriamente();
        calcularMinasVizinhasDeTodasAsCelulas();
        aplicarModoRelampagoSeNecessario();
    }

    /**
     * Construtor auxiliar que recebe as posições das minas explicitamente,
     * em vez de sortear, com regras clássicas. Pensado para ser usado em
     * testes unitários, onde é preciso saber exatamente onde as minas
     * estão para verificar o comportamento da cascata e da contagem de
     * vizinhas.
     *
     * @param linhas         número de linhas do tabuleiro
     * @param colunas        número de colunas do tabuleiro
     * @param posicoesMinas  array de pares {linha, coluna} com as minas
     */
    public Tabuleiro(int linhas, int colunas, int[][] posicoesMinas) {
        this(linhas, colunas, posicoesMinas, RegrasJogo.padrao());
    }

    /**
     * Construtor auxiliar com posições de minas explícitas e regras
     * customizadas. Além dos testes, é usado para reconstruir fielmente
     * um tabuleiro a partir de um layout de minas conhecido — por exemplo
     * ao reproduzir um replay (sugestão #41) ou ao montar os dois
     * tabuleiros idênticos do modo competitivo (sugestão #43). Por isso,
     * ao contrário do construtor aleatório, este nunca aplica o modo
     * relâmpago sozinho: quem reconstrói o tabuleiro decide se e como
     * repete as revelações iniciais.
     */
    public Tabuleiro(int linhas, int colunas, int[][] posicoesMinas, RegrasJogo regras) {
        this.linhas = linhas;
        this.colunas = colunas;
        this.numMinas = posicoesMinas.length;
        this.regras = regras == null ? RegrasJogo.padrao() : regras;
        this.vidasRestantes = this.regras.getVidasIniciais();
        this.grade = new Celula[linhas][colunas];
        inicializarGrade();
        for (int[] posicao : posicoesMinas) {
            grade[posicao[0]][posicao[1]].setMinada(true);
        }
        calcularMinasVizinhasDeTodasAsCelulas();
    }

    private void inicializarGrade() {
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                grade[i][j] = new Celula();
            }
        }
    }

    private void posicionarMinasAleatoriamente() {
        Random sorteio = regras.getSemente() != null ? new Random(regras.getSemente()) : new Random();
        int minasColocadas = 0;
        while (minasColocadas < numMinas) {
            int linha = sorteio.nextInt(linhas);
            int coluna = sorteio.nextInt(colunas);
            if (!grade[linha][coluna].isMinada()) {
                grade[linha][coluna].setMinada(true);
                minasColocadas++;
            }
        }
    }

    private void calcularMinasVizinhasDeTodasAsCelulas() {
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                grade[i][j].setMinasVizinhas(contarMinasVizinhas(i, j));
            }
        }
    }

    private int contarMinasVizinhas(int linha, int coluna) {
        int total = 0;
        for (int[] vizinho : obterVizinhos(linha, coluna)) {
            if (grade[vizinho[0]][vizinho[1]].isMinada()) {
                total++;
            }
        }
        return total;
    }

    /**
     * Revela algumas células seguras aleatórias antes do jogador clicar
     * em qualquer lugar (modo "relâmpago", sugestão #16). Reaproveita o
     * próprio {@link #revelar(int, int)} para que a cascata (ou a falta
     * dela, se {@link RegrasJogo#isCascataAtiva()} for falso) funcione
     * exatamente como em uma revelação normal.
     */
    private void aplicarModoRelampagoSeNecessario() {
        if (!regras.isModoRelampago()) {
            return;
        }
        List<int[]> celulasSeguras = new ArrayList<>();
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                if (!grade[i][j].isMinada()) {
                    celulasSeguras.add(new int[]{i, j});
                }
            }
        }
        Collections.shuffle(celulasSeguras);
        int quantidade = Math.min(regras.getCelulasRelampago(), celulasSeguras.size());
        for (int i = 0; i < quantidade; i++) {
            int[] posicao = celulasSeguras.get(i);
            if (!grade[posicao[0]][posicao[1]].isRevelada()) {
                revelar(posicao[0], posicao[1]);
            }
        }
    }

    private boolean dentroDosLimites(int linha, int coluna) {
        return linha >= 0 && linha < linhas && coluna >= 0 && coluna < colunas;
    }

    /**
     * Lista as posições vizinhas válidas de uma célula. Em tabuleiros
     * comuns, isso é só quem está dentro dos limites da grade; em
     * tabuleiros toroidais (sugestão #18) as bordas se conectam — a
     * célula da coluna 0 é vizinha da última coluna, e assim por diante —
     * usando aritmética modular. Centralizar essa regra aqui evita
     * duplicar a mesma lógica de vizinhança em vários lugares da classe.
     */
    private List<int[]> obterVizinhos(int linha, int coluna) {
        List<int[]> vizinhos = new ArrayList<>();
        for (int deltaLinha = -1; deltaLinha <= 1; deltaLinha++) {
            for (int deltaColuna = -1; deltaColuna <= 1; deltaColuna++) {
                if (deltaLinha == 0 && deltaColuna == 0) {
                    continue;
                }
                int vizinhoLinha = linha + deltaLinha;
                int vizinhoColuna = coluna + deltaColuna;
                if (regras.isToroidal()) {
                    vizinhoLinha = Math.floorMod(vizinhoLinha, linhas);
                    vizinhoColuna = Math.floorMod(vizinhoColuna, colunas);
                    vizinhos.add(new int[]{vizinhoLinha, vizinhoColuna});
                } else if (dentroDosLimites(vizinhoLinha, vizinhoColuna)) {
                    vizinhos.add(new int[]{vizinhoLinha, vizinhoColuna});
                }
            }
        }
        return vizinhos;
    }

    /**
     * Revela a célula indicada. Se a célula não tiver minas vizinhas, o
     * efeito cascata revela automaticamente as células ao redor (e assim
     * sucessivamente), sem nunca revelar uma célula minada por engano —
     * a menos que o modo "sem cascata" (sugestão #12) esteja ativo, caso
     * em que só a célula clicada é revelada.
     * <p>
     * No modo clássico (uma vida), revelar uma mina encerra o jogo
     * imediatamente. No modo "3 vidas" (sugestão #17), a mina é revelada
     * e consumida, mas o jogo só termina quando as vidas acabarem.
     * <p>
     * A cascata é implementada de forma iterativa usando uma
     * {@link ArrayList} como fila de células pendentes de revelação —
     * evita o uso de recursão profunda em tabuleiros grandes.
     *
     * @param linha  linha da célula a revelar
     * @param coluna coluna da célula a revelar
     * @return a lista das células que foram reveladas nesta jogada, na
     *         ordem em que foram reveladas — útil para quem quiser animar
     *         a cascata célula a célula (ex.: a View). Se a jogada não
     *         revelar nada (célula já revelada, marcada, jogo encerrado,
     *         etc.), retorna uma lista vazia.
     */
    public List<int[]> revelar(int linha, int coluna) {
        List<int[]> ordemRevelacao = new ArrayList<>();

        if (jogoEncerrado || !dentroDosLimites(linha, coluna)) {
            return ordemRevelacao;
        }

        Celula celulaInicial = grade[linha][coluna];
        if (celulaInicial.isRevelada() || celulaInicial.isMarcada()) {
            return ordemRevelacao;
        }

        if (celulaInicial.isMinada()) {
            celulaInicial.revelar();
            ordemRevelacao.add(new int[] { linha, coluna });
            vidasRestantes--;
            if (vidasRestantes <= 0) {
                jogoEncerrado = true;
                derrota = true;
            }
            return ordemRevelacao;
        }

        List<int[]> pendentes = new ArrayList<>();
        pendentes.add(new int[] { linha, coluna });

        while (!pendentes.isEmpty()) {
            int[] posicaoAtual = pendentes.remove(pendentes.size() - 1);
            int linhaAtual = posicaoAtual[0];
            int colunaAtual = posicaoAtual[1];
            Celula atual = grade[linhaAtual][colunaAtual];

            if (atual.isRevelada() || atual.isMarcada() || atual.isMinada()) {
                continue;
            }

            atual.revelar();
            ordemRevelacao.add(new int[] { linhaAtual, colunaAtual });

            if (regras.isCascataAtiva() && atual.getMinasVizinhas() == 0) {
                for (int[] vizinho : obterVizinhos(linhaAtual, colunaAtual)) {
                    Celula vizinha = grade[vizinho[0]][vizinho[1]];
                    if (!vizinha.isRevelada() && !vizinha.isMarcada() && !vizinha.isMinada()) {
                        pendentes.add(vizinho);
                    }
                }
            }
        }

        if (verificarVitoria()) {
            jogoEncerrado = true;
        }

        return ordemRevelacao;
    }

    /**
     * Marca ou desmarca uma célula, ciclando entre bandeira e
     * interrogação (sugestão #20), sem revelá-la.
     */
    public void alternarMarcacao(int linha, int coluna) {
        if (jogoEncerrado || !dentroDosLimites(linha, coluna)) {
            return;
        }
        grade[linha][coluna].alternarMarcacao();
    }

    /**
     * O jogo é vencido quando todas as células que não são minas já
     * foram reveladas — mesmo que, no modo "3 vidas", alguma mina também
     * já tenha sido revelada pelo caminho.
     */
    public boolean verificarVitoria() {
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                Celula celula = grade[i][j];
                if (!celula.isMinada() && !celula.isRevelada()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isDerrota() {
        return derrota;
    }

    @Override
    public boolean isJogoEncerrado() {
        return jogoEncerrado;
    }

    /**
     * Encerra a partida como derrota por tempo esgotado (modo de tempo
     * limite, já existente na tela de dificuldade). Fica aqui, e não em
     * um {@code setJogoEncerrado} genérico, para que o Model nunca exponha
     * uma forma de qualquer código externo forçar um estado de jogo
     * arbitrário — só este caso específico e nomeado.
     * <p>
     * Corrige um comportamento observado no Controller original: ao
     * esgotar o tempo, ele criava um {@code Tabuleiro} novo e aleatório
     * só para "ter algo para descartar", sem nunca marcar a partida
     * jogada de fato como encerrada. Isso deixava o tabuleiro exibido em
     * tela (o antigo) sempre com {@code isJogoEncerrado() == false},
     * então cliques adicionais depois do "Você perdeu!" continuavam
     * sendo processados normalmente pelo Model.
     */
    public void encerrarPorTempoEsgotado() {
        if (!jogoEncerrado) {
            this.jogoEncerrado = true;
            this.derrota = true;
        }
    }

    @Override
    public int getLinhas() {
        return linhas;
    }

    @Override
    public int getColunas() {
        return colunas;
    }

    public int getNumMinas() {
        return numMinas;
    }

    @Override
    public int getVidasRestantes() {
        return vidasRestantes;
    }

    @Override
    public int getVidasIniciais() {
        return regras.getVidasIniciais();
    }

    /** Regras ativas neste tabuleiro (cascata, vidas, toroidal, relâmpago, semente). */
    public RegrasJogo getRegras() {
        return regras;
    }

    /**
     * Posições de todas as minas do tabuleiro. Usado para reconstruir um
     * tabuleiro idêntico (replay — sugestão #41; modo competitivo —
     * sugestão #43) e pelo modo de depuração "minas visíveis"
     * (sugestão #13, já coberto por {@link #isMinada}).
     */
    public List<int[]> obterPosicoesMinas() {
        List<int[]> posicoes = new ArrayList<>();
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                if (grade[i][j].isMinada()) {
                    posicoes.add(new int[]{i, j});
                }
            }
        }
        return posicoes;
    }

    // ----- implementação de LeituraTabuleiro (usada pela View) -----

    @Override
    public boolean isRevelada(int linha, int coluna) {
        return grade[linha][coluna].isRevelada();
    }

    @Override
    public boolean isMarcada(int linha, int coluna) {
        return grade[linha][coluna].isMarcada();
    }

    @Override
    public boolean isInterrogada(int linha, int coluna) {
        return grade[linha][coluna].isInterrogada();
    }

    @Override
    public boolean isMinada(int linha, int coluna) {
        return grade[linha][coluna].isMinada();
    }

    @Override
    public int getMinasVizinhas(int linha, int coluna) {
        return grade[linha][coluna].getMinasVizinhas();
    }

    /**
     * Retorna a célula em uma posição específica. Mantido para uso interno
     * do próprio Model e para os testes unitários — a View nunca deve
     * chamar este método diretamente; ela usa {@link LeituraTabuleiro}.
     */
    public Celula getCelula(int linha, int coluna) {
        return grade[linha][coluna];
    }

    /**
     * Imprime o tabuleiro no console. Quando revelarTudo é true (por
     * exemplo, ao final de uma derrota), mostra também as minas.
     */
    public void imprimir(boolean revelarTudo) {
        StringBuilder cabecalho = new StringBuilder("   ");
        for (int j = 0; j < colunas; j++) {
            cabecalho.append(String.format("%2d", j));
        }
        System.out.println(cabecalho);

        for (int i = 0; i < linhas; i++) {
            StringBuilder linhaTexto = new StringBuilder(String.format("%2d ", i));
            for (int j = 0; j < colunas; j++) {
                Celula celula = grade[i][j];
                if (revelarTudo && celula.isMinada()) {
                    linhaTexto.append(" *");
                } else {
                    linhaTexto.append(" ").append(celula);
                }
            }
            System.out.println(linhaTexto);
        }
    }
}
