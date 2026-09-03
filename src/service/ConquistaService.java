package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Avalia, ao final de cada partida, quais conquistas (sugestão #50) o
 * jogador desbloqueou pela primeira vez.
 */
public class ConquistaService {

    /** Devolve só as conquistas novas — as que já estavam em {@code jaDesbloqueadas} são ignoradas. */
    public List<Conquista> avaliarNovasConquistas(ContextoResultadoPartida contexto, Set<String> jaDesbloqueadas) {
        List<Conquista> novas = new ArrayList<>();
        for (Conquista conquista : Conquista.values()) {
            if (!jaDesbloqueadas.contains(conquista.name()) && conquista.foiConquistada(contexto)) {
                novas.add(conquista);
            }
        }
        return novas;
    }
}
