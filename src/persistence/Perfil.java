package persistence;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Um perfil de jogador (sugestão #33 — "vários usuários usando o mesmo
 * programa, cada um com seu histórico"). O nome do perfil é a chave usada
 * por {@link repository.EstatisticasRepository} para filtrar o histórico
 * de cada jogador e por {@link SaveGame} para saber de quem é uma partida
 * salva.
 * <p>
 * As conquistas desbloqueadas (sugestão #50) também ficam aqui, por
 * pertencerem naturalmente ao perfil — não faria sentido um "venceu sem
 * usar bandeira" pertencer a ninguém.
 */
public final class Perfil implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nome;
    private final LocalDateTime criadoEm;
    private final Set<String> conquistasDesbloqueadas;

    public Perfil(String nome) {
        this.nome = nome;
        this.criadoEm = LocalDateTime.now();
        this.conquistasDesbloqueadas = new LinkedHashSet<>();
    }

    public String getNome() {
        return nome;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public Set<String> getConquistasDesbloqueadas() {
        return conquistasDesbloqueadas;
    }

    public boolean desbloquear(String idConquista) {
        return conquistasDesbloqueadas.add(idConquista);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Perfil)) return false;
        return nome.equals(((Perfil) o).nome);
    }

    @Override
    public int hashCode() {
        return nome.hashCode();
    }
}
