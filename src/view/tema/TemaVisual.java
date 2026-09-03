package view.tema;

import java.awt.Color;

/**
 * Um esquema de cores completo da interface — um "tema" (sugestão #1:
 * "Sistema de temas de cores plugável (classe TemaVisual trocável em
 * tempo de execução)").
 * <p>
 * Antes, {@link view.CampoMinadoView} tinha o esquema de cores do fundo e
 * o esquema de cores do tabuleiro como dois {@code if/else} fixos dentro
 * de {@code aplicarTemaSelecionado()} — adicionar um tema novo exigia
 * editar aquele método. Agora, um tema é só um objeto imutável, montado
 * com {@link Builder} e guardado em {@link RegistroTemas}; a View lê os
 * campos deste objeto e nunca mais precisa saber quantos temas existem
 * ou quais são.
 */
public final class TemaVisual {

    private final String nome;

    private final Color fundo;
    private final Color fundoClaro;
    private final Color destaque;
    private final Color textoPrincipal;
    private final Color textoSecundario;
    private final Color card;
    private final Color cardHover;
    private final Color borda;

    private final Color celulaOculta;
    private final Color celulaOcultaHover;
    private final Color bordaOculta;
    private final Color celulaRevelada;
    private final Color bordaRevelada;
    private final Color textoSobreRevelada;
    private final Color minaFundo;

    private TemaVisual(Builder b) {
        this.nome = b.nome;
        this.fundo = b.fundo;
        this.fundoClaro = b.fundoClaro;
        this.destaque = b.destaque;
        this.textoPrincipal = b.textoPrincipal;
        this.textoSecundario = b.textoSecundario;
        this.card = b.card;
        this.cardHover = b.cardHover;
        this.borda = b.borda;
        this.celulaOculta = b.celulaOculta;
        this.celulaOcultaHover = b.celulaOcultaHover;
        this.bordaOculta = b.bordaOculta;
        this.celulaRevelada = b.celulaRevelada;
        this.bordaRevelada = b.bordaRevelada;
        this.textoSobreRevelada = b.textoSobreRevelada;
        this.minaFundo = b.minaFundo;
    }

    public static Builder construir(String nome) {
        return new Builder(nome);
    }

    public String getNome() { return nome; }
    public Color getFundo() { return fundo; }
    public Color getFundoClaro() { return fundoClaro; }
    public Color getDestaque() { return destaque; }
    public Color getTextoPrincipal() { return textoPrincipal; }
    public Color getTextoSecundario() { return textoSecundario; }
    public Color getCard() { return card; }
    public Color getCardHover() { return cardHover; }
    public Color getBorda() { return borda; }
    public Color getCelulaOculta() { return celulaOculta; }
    public Color getCelulaOcultaHover() { return celulaOcultaHover; }
    public Color getBordaOculta() { return bordaOculta; }
    public Color getCelulaRevelada() { return celulaRevelada; }
    public Color getBordaRevelada() { return bordaRevelada; }
    public Color getTextoSobreRevelada() { return textoSobreRevelada; }
    public Color getMinaFundo() { return minaFundo; }

    @Override
    public String toString() {
        return nome;
    }

    public static final class Builder {
        private final String nome;
        private Color fundo = Color.DARK_GRAY;
        private Color fundoClaro = Color.GRAY;
        private Color destaque = Color.BLUE;
        private Color textoPrincipal = Color.WHITE;
        private Color textoSecundario = Color.LIGHT_GRAY;
        private Color card = Color.DARK_GRAY;
        private Color cardHover = Color.GRAY;
        private Color borda = Color.GRAY;
        private Color celulaOculta = Color.DARK_GRAY;
        private Color celulaOcultaHover = Color.GRAY;
        private Color bordaOculta = Color.GRAY;
        private Color celulaRevelada = Color.LIGHT_GRAY;
        private Color bordaRevelada = Color.GRAY;
        private Color textoSobreRevelada = Color.BLACK;
        private Color minaFundo = new Color(60, 20, 20);

        private Builder(String nome) {
            this.nome = nome;
        }

        public Builder fundo(Color c) { this.fundo = c; return this; }
        public Builder fundoClaro(Color c) { this.fundoClaro = c; return this; }
        public Builder destaque(Color c) { this.destaque = c; return this; }
        public Builder textoPrincipal(Color c) { this.textoPrincipal = c; return this; }
        public Builder textoSecundario(Color c) { this.textoSecundario = c; return this; }
        public Builder card(Color c) { this.card = c; return this; }
        public Builder cardHover(Color c) { this.cardHover = c; return this; }
        public Builder borda(Color c) { this.borda = c; return this; }
        public Builder celulaOculta(Color c) { this.celulaOculta = c; return this; }
        public Builder celulaOcultaHover(Color c) { this.celulaOcultaHover = c; return this; }
        public Builder bordaOculta(Color c) { this.bordaOculta = c; return this; }
        public Builder celulaRevelada(Color c) { this.celulaRevelada = c; return this; }
        public Builder bordaRevelada(Color c) { this.bordaRevelada = c; return this; }
        public Builder textoSobreRevelada(Color c) { this.textoSobreRevelada = c; return this; }
        public Builder minaFundo(Color c) { this.minaFundo = c; return this; }

        public TemaVisual build() {
            return new TemaVisual(this);
        }
    }
}
