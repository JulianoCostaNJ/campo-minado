package view.tema;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Catálogo dos temas visuais disponíveis. Novos temas podem ser
 * registrados em tempo de execução com {@link #registrar(TemaVisual)} —
 * a View e o {@code JComboBox} de temas simplesmente leem
 * {@link #listarNomes()}, então um tema novo aparece na tela sem
 * qualquer alteração na View (Aberto/Fechado: aberto para extensão,
 * fechado para modificação).
 * <p>
 * Os quatro primeiros temas reproduzem exatamente as cores que o
 * projeto original já usava (Escuro = tema padrão de fundo + tabuleiro
 * "Clássico", Claro, Noite e Campo), para que o visual de quem já
 * jogava não mude. Os cinco temas seguintes são as sugestões #2 a #6.
 */
public final class RegistroTemas {

    private final Map<String, TemaVisual> temas = new LinkedHashMap<>();

    public RegistroTemas() {
        registrarTemasPadrao();
    }

    public void registrar(TemaVisual tema) {
        temas.put(tema.getNome(), tema);
    }

    public TemaVisual obter(String nome) {
        TemaVisual tema = temas.get(nome);
        return tema != null ? tema : temas.get("Escuro");
    }

    public List<String> listarNomes() {
        return new ArrayList<>(temas.keySet());
    }

    private void registrarTemasPadrao() {
        registrar(TemaVisual.construir("Escuro")
                .fundo(new Color(30, 30, 35))
                .fundoClaro(new Color(45, 45, 52))
                .destaque(new Color(70, 130, 180))
                .textoPrincipal(new Color(230, 230, 235))
                .textoSecundario(new Color(150, 150, 160))
                .card(new Color(50, 50, 58))
                .cardHover(new Color(65, 65, 78))
                .borda(new Color(80, 80, 90))
                .celulaOculta(new Color(72, 78, 96))
                .celulaOcultaHover(new Color(90, 97, 118))
                .bordaOculta(new Color(100, 107, 128))
                .celulaRevelada(new Color(228, 228, 233))
                .bordaRevelada(new Color(195, 195, 202))
                .textoSobreRevelada(new Color(40, 40, 45))
                .minaFundo(new Color(60, 20, 20))
                .build());

        registrar(TemaVisual.construir("Claro")
                .fundo(new Color(245, 245, 250))
                .fundoClaro(new Color(230, 230, 235))
                .destaque(new Color(35, 100, 190))
                .textoPrincipal(new Color(25, 25, 30))
                .textoSecundario(new Color(95, 95, 110))
                .card(new Color(245, 245, 250))
                .cardHover(new Color(225, 225, 235))
                .borda(new Color(180, 180, 190))
                .celulaOculta(new Color(72, 78, 96))
                .celulaOcultaHover(new Color(90, 97, 118))
                .bordaOculta(new Color(100, 107, 128))
                .celulaRevelada(new Color(228, 228, 233))
                .bordaRevelada(new Color(195, 195, 202))
                .textoSobreRevelada(new Color(40, 40, 45))
                .minaFundo(new Color(60, 20, 20))
                .build());

        registrar(TemaVisual.construir("Noite")
                .fundo(new Color(30, 30, 35))
                .fundoClaro(new Color(45, 45, 52))
                .destaque(new Color(70, 130, 180))
                .textoPrincipal(new Color(230, 230, 235))
                .textoSecundario(new Color(150, 150, 160))
                .card(new Color(50, 50, 58))
                .cardHover(new Color(65, 65, 78))
                .borda(new Color(80, 80, 90))
                .celulaOculta(new Color(20, 30, 45))
                .celulaOcultaHover(new Color(35, 50, 75))
                .bordaOculta(new Color(70, 90, 120))
                .celulaRevelada(new Color(55, 65, 80))
                .bordaRevelada(new Color(80, 95, 115))
                .textoSobreRevelada(new Color(230, 230, 240))
                .minaFundo(new Color(180, 40, 40))
                .build());

        registrar(TemaVisual.construir("Campo")
                .fundo(new Color(25, 35, 25))
                .fundoClaro(new Color(45, 65, 45))
                .destaque(new Color(140, 200, 120))
                .textoPrincipal(new Color(220, 230, 200))
                .textoSecundario(new Color(170, 190, 150))
                .card(new Color(35, 55, 35))
                .cardHover(new Color(55, 75, 55))
                .borda(new Color(60, 80, 60))
                .celulaOculta(new Color(40, 70, 45))
                .celulaOcultaHover(new Color(60, 95, 65))
                .bordaOculta(new Color(70, 105, 80))
                .celulaRevelada(new Color(220, 235, 210))
                .bordaRevelada(new Color(155, 175, 145))
                .textoSobreRevelada(new Color(25, 45, 25))
                .minaFundo(new Color(170, 40, 40))
                .build());

        // Sugestão #2 — terminal retrô (verde fósforo sobre preto).
        registrar(TemaVisual.construir("Terminal Retrô")
                .fundo(new Color(8, 12, 8))
                .fundoClaro(new Color(16, 24, 16))
                .destaque(new Color(80, 250, 80))
                .textoPrincipal(new Color(120, 255, 120))
                .textoSecundario(new Color(60, 160, 60))
                .card(new Color(12, 18, 12))
                .cardHover(new Color(20, 32, 20))
                .borda(new Color(40, 90, 40))
                .celulaOculta(new Color(14, 26, 14))
                .celulaOcultaHover(new Color(22, 42, 22))
                .bordaOculta(new Color(50, 130, 50))
                .celulaRevelada(new Color(6, 12, 6))
                .bordaRevelada(new Color(60, 150, 60))
                .textoSobreRevelada(new Color(130, 255, 130))
                .minaFundo(new Color(40, 10, 10))
                .build());

        // Sugestão #3 — cyberpunk neon (roxo escuro, rosa e ciano).
        registrar(TemaVisual.construir("Cyberpunk Neon")
                .fundo(new Color(16, 8, 26))
                .fundoClaro(new Color(30, 15, 46))
                .destaque(new Color(255, 20, 190))
                .textoPrincipal(new Color(225, 205, 255))
                .textoSecundario(new Color(150, 110, 190))
                .card(new Color(25, 12, 40))
                .cardHover(new Color(40, 20, 62))
                .borda(new Color(100, 45, 130))
                .celulaOculta(new Color(35, 15, 55))
                .celulaOcultaHover(new Color(55, 25, 82))
                .bordaOculta(new Color(150, 60, 190))
                .celulaRevelada(new Color(230, 220, 245))
                .bordaRevelada(new Color(0, 220, 220))
                .textoSobreRevelada(new Color(40, 20, 55))
                .minaFundo(new Color(65, 10, 45))
                .build());

        // Sugestão #4 — Halloween (laranja, roxo, marrom).
        registrar(TemaVisual.construir("Halloween")
                .fundo(new Color(20, 12, 8))
                .fundoClaro(new Color(35, 20, 12))
                .destaque(new Color(255, 140, 20))
                .textoPrincipal(new Color(240, 210, 180))
                .textoSecundario(new Color(180, 140, 100))
                .card(new Color(30, 18, 12))
                .cardHover(new Color(45, 28, 18))
                .borda(new Color(95, 58, 30))
                .celulaOculta(new Color(48, 22, 58))
                .celulaOcultaHover(new Color(68, 34, 78))
                .bordaOculta(new Color(130, 65, 140))
                .celulaRevelada(new Color(235, 215, 190))
                .bordaRevelada(new Color(180, 110, 40))
                .textoSobreRevelada(new Color(50, 25, 15))
                .minaFundo(new Color(60, 15, 10))
                .build());

        // Sugestão #5 — oceano/tropical (azul profundo, coral, turquesa).
        registrar(TemaVisual.construir("Oceano/Tropical")
                .fundo(new Color(6, 30, 45))
                .fundoClaro(new Color(10, 45, 65))
                .destaque(new Color(255, 140, 110))
                .textoPrincipal(new Color(220, 245, 250))
                .textoSecundario(new Color(140, 200, 210))
                .card(new Color(10, 45, 65))
                .cardHover(new Color(15, 60, 85))
                .borda(new Color(30, 95, 115))
                .celulaOculta(new Color(10, 60, 90))
                .celulaOcultaHover(new Color(15, 80, 115))
                .bordaOculta(new Color(40, 135, 155))
                .celulaRevelada(new Color(210, 245, 240))
                .bordaRevelada(new Color(90, 190, 180))
                .textoSobreRevelada(new Color(10, 60, 60))
                .minaFundo(new Color(90, 25, 25))
                .build());

        // Sugestão #6 — alto contraste, pensado para acessibilidade.
        registrar(TemaVisual.construir("Alto Contraste")
                .fundo(Color.BLACK)
                .fundoClaro(new Color(25, 25, 25))
                .destaque(new Color(255, 221, 0))
                .textoPrincipal(Color.WHITE)
                .textoSecundario(new Color(255, 221, 0))
                .card(Color.BLACK)
                .cardHover(new Color(45, 45, 45))
                .borda(Color.WHITE)
                .celulaOculta(Color.BLACK)
                .celulaOcultaHover(new Color(45, 45, 45))
                .bordaOculta(Color.WHITE)
                .celulaRevelada(Color.WHITE)
                .bordaRevelada(Color.BLACK)
                .textoSobreRevelada(Color.BLACK)
                .minaFundo(new Color(190, 0, 0))
                .build());
    }
}
