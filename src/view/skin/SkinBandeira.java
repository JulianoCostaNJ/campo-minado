package view.skin;

/**
 * Skins de bandeira (sugestão #8): troca só o símbolo desenhado sobre a
 * célula marcada — a regra de jogo (o que "marcada" significa) continua
 * inteiramente no Model, esta classe só sabe desenhar.
 */
public enum SkinBandeira {

    PADRAO("Padrão", "\uD83D\uDEA9"),      // 🚩
    ESTRELA("Estrela", "\u2B50"),          // ⭐
    CORACAO("Coração", "\u2764\uFE0F"),    // ❤️
    ALFINETE("Alfinete", "\uD83D\uDCCC");  // 📌

    private final String nomeExibicao;
    private final String emoji;

    SkinBandeira(String nomeExibicao, String emoji) {
        this.nomeExibicao = nomeExibicao;
        this.emoji = emoji;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public String getEmoji() {
        return emoji;
    }

    public static SkinBandeira porNomeExibicao(String nome) {
        for (SkinBandeira skin : values()) {
            if (skin.nomeExibicao.equals(nome)) {
                return skin;
            }
        }
        return PADRAO;
    }
}
