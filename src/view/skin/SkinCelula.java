package view.skin;

/**
 * Skins do número de minas vizinhas de uma célula revelada (sugestão
 * #9): "Números" é o comportamento clássico; "Frutas" e "Animais" trocam
 * o dígito por um emoji temático correspondente à contagem (1 a 8).
 */
public enum SkinCelula {

    NUMEROS("Números", null),
    FRUTAS("Frutas", new String[] {
            "\uD83C\uDF4E", "\uD83C\uDF4C", "\uD83C\uDF47", "\uD83C\uDF4A",
            "\uD83C\uDF53", "\uD83C\uDF4D", "\uD83C\uDF51", "\uD83C\uDF49"
    }),
    ANIMAIS("Animais", new String[] {
            "\uD83D\uDC31", "\uD83D\uDC36", "\uD83D\uDC2D", "\uD83D\uDC39",
            "\uD83D\uDC30", "\uD83E\uDD8A", "\uD83D\uDC3B", "\uD83D\uDC3C"
    });

    private final String nomeExibicao;
    private final String[] simbolosPorContagem;

    SkinCelula(String nomeExibicao, String[] simbolosPorContagem) {
        this.nomeExibicao = nomeExibicao;
        this.simbolosPorContagem = simbolosPorContagem;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    /** Símbolo a desenhar para uma célula com {@code minasVizinhas} minas ao redor (1 a 8). */
    public String obterSimbolo(int minasVizinhas) {
        if (simbolosPorContagem == null || minasVizinhas < 1 || minasVizinhas > simbolosPorContagem.length) {
            return String.valueOf(minasVizinhas);
        }
        return simbolosPorContagem[minasVizinhas - 1];
    }

    public static SkinCelula porNomeExibicao(String nome) {
        for (SkinCelula skin : values()) {
            if (skin.nomeExibicao.equals(nome)) {
                return skin;
            }
        }
        return NUMEROS;
    }
}
