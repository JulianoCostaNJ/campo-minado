package view.tema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Tenta detectar se o sistema operacional está no modo escuro ou claro
 * (sugestão #7), para pré-selecionar um tema coerente na primeira vez
 * que o jogo abre.
 * <p>
 * Não existe uma API do Java puro para isso, então cada sistema exige um
 * comando diferente (registro do Windows, {@code defaults} do macOS,
 * {@code gsettings} do GNOME/Linux). Qualquer falha — comando ausente,
 * SO não reconhecido, timeout — cai silenciosamente no padrão escuro;
 * detectar o tema do sistema é só uma conveniência, nunca algo que pode
 * travar ou atrapalhar a abertura do jogo.
 */
public final class DetectorTemaSistema {

    private DetectorTemaSistema() {
    }

    public static boolean sistemaPreferemodoEscuro() {
        String so = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        try {
            if (so.contains("win")) {
                return detectarWindows();
            } else if (so.contains("mac")) {
                return detectarMacOS();
            } else if (so.contains("nux") || so.contains("nix")) {
                return detectarLinux();
            }
        } catch (Exception ignorada) {
            // Detecção é só uma conveniência — qualquer problema aqui não deve impedir o jogo de abrir.
        }
        return true; // padrão: escuro, mesmo esquema que o projeto já usava antes desta sugestão existir.
    }

    private static boolean detectarWindows() throws IOException, InterruptedException {
        String saida = executar(10,
                "reg", "query",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "/v", "AppsUseLightTheme");
        if (saida == null) {
            return true;
        }
        // A saída contém "0x0" (modo escuro) ou "0x1" (modo claro).
        return saida.contains("0x0");
    }

    private static boolean detectarMacOS() throws IOException, InterruptedException {
        String saida = executar(10, "defaults", "read", "-g", "AppleInterfaceStyle");
        // Comando só imprime "Dark" quando o modo escuro está ativo; em modo
        // claro ele falha (chave inexistente) — tratado como claro.
        return saida != null && saida.toLowerCase(Locale.ROOT).contains("dark");
    }

    private static boolean detectarLinux() throws IOException, InterruptedException {
        String saida = executar(10, "gsettings", "get", "org.gnome.desktop.interface", "color-scheme");
        if (saida != null) {
            return saida.toLowerCase(Locale.ROOT).contains("dark");
        }
        return true;
    }

    private static String executar(int timeoutSegundos, String... comando) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(comando);
        builder.redirectErrorStream(true);
        Process processo = builder.start();
        StringBuilder saida = new StringBuilder();
        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(processo.getInputStream(), StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                saida.append(linha).append('\n');
            }
        }
        boolean terminou = processo.waitFor(timeoutSegundos, TimeUnit.SECONDS);
        if (!terminou) {
            processo.destroyForcibly();
            return null;
        }
        return processo.exitValue() == 0 ? saida.toString() : null;
    }
}
