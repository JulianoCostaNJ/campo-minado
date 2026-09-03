package main;

import java.util.Scanner;
import model.Tabuleiro;

/**
 * Loop principal do jogo, via console. Esta classe só conversa com
 * {@link Tabuleiro} — nunca acessa {@link Celula} diretamente, respeitando
 * o encapsulamento sugerido na arquitetura do enunciado.
 */
public class JogoCampoMinado {

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);

        System.out.println("=== Campo Minado ===");
        int linhas = lerInteiro(teclado, "Número de linhas: ");
        int colunas = lerInteiro(teclado, "Número de colunas: ");
        int minas = lerInteiro(teclado, "Número de minas: ");

        Tabuleiro tabuleiro = new Tabuleiro(linhas, colunas, minas);

        while (!tabuleiro.isJogoEncerrado()) {
            tabuleiro.imprimir(false);
            System.out.println();
            System.out.println("Comandos: 'r linha coluna' para revelar, 'm linha coluna' para marcar/desmarcar");
            System.out.print("> ");

            String comando = teclado.next();
            int linha = teclado.nextInt();
            int coluna = teclado.nextInt();

            if (comando.equalsIgnoreCase("r")) {
                tabuleiro.revelar(linha, coluna);
            } else if (comando.equalsIgnoreCase("m")) {
                tabuleiro.alternarMarcacao(linha, coluna);
            } else {
                System.out.println("Comando inválido. Use 'r' ou 'm'.");
            }
        }

        tabuleiro.imprimir(true);
        if (tabuleiro.isDerrota()) {
            System.out.println("\nVocê pisou em uma mina. Fim de jogo!");
        } else {
            System.out.println("\nParabéns, você venceu! Todas as células seguras foram reveladas.");
        }

        teclado.close();
    }

    private static int lerInteiro(Scanner teclado, String mensagem) {
        System.out.print(mensagem);
        int valor = teclado.nextInt();
        return valor;
    }
}
