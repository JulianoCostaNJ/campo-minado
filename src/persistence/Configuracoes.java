package persistence;

import java.io.Serializable;

/**
 * Configurações persistentes entre execuções (sugestão #34): tema, skins,
 * som/música, densidade de minas padrão para o modo customizado (parte da
 * sugestão #46) e o perfil ativo (sugestão #33). Um único objeto simples
 * — sem comportamento — guardado inteiro a cada alteração, para que
 * adicionar uma configuração nova no futuro seja só adicionar um campo e
 * um getter/wither aqui.
 */
public final class Configuracoes implements Serializable {

    private static final long serialVersionUID = 1L;

    private String perfilAtivo = "Jogador";
    private String nomeTema = "Escuro";
    private String nomeSkinBandeira = "Padrão";
    private String nomeSkinCelula = "Números";
    private boolean somAtivado = true;
    private boolean musicaAtivada = false;
    private float volumeMusica = 0.6f;
    private boolean simbolosDaltonicos = false;
    private double densidadeMinasPadrao = 0.15625; // ~igual ao Iniciante (10/81)
    private int tamanhoCelula = 36;

    public static Configuracoes padrao() {
        return new Configuracoes();
    }

    public String getPerfilAtivo() {
        return perfilAtivo;
    }

    public void setPerfilAtivo(String perfilAtivo) {
        this.perfilAtivo = perfilAtivo;
    }

    public String getNomeTema() {
        return nomeTema;
    }

    public void setNomeTema(String nomeTema) {
        this.nomeTema = nomeTema;
    }

    public String getNomeSkinBandeira() {
        return nomeSkinBandeira;
    }

    public void setNomeSkinBandeira(String nomeSkinBandeira) {
        this.nomeSkinBandeira = nomeSkinBandeira;
    }

    public String getNomeSkinCelula() {
        return nomeSkinCelula;
    }

    public void setNomeSkinCelula(String nomeSkinCelula) {
        this.nomeSkinCelula = nomeSkinCelula;
    }

    public boolean isSomAtivado() {
        return somAtivado;
    }

    public void setSomAtivado(boolean somAtivado) {
        this.somAtivado = somAtivado;
    }

    public boolean isMusicaAtivada() {
        return musicaAtivada;
    }

    public void setMusicaAtivada(boolean musicaAtivada) {
        this.musicaAtivada = musicaAtivada;
    }

    public float getVolumeMusica() {
        return volumeMusica;
    }

    public void setVolumeMusica(float volumeMusica) {
        this.volumeMusica = Math.max(0f, Math.min(1f, volumeMusica));
    }

    public boolean isSimbolosDaltonicos() {
        return simbolosDaltonicos;
    }

    public void setSimbolosDaltonicos(boolean simbolosDaltonicos) {
        this.simbolosDaltonicos = simbolosDaltonicos;
    }

    public double getDensidadeMinasPadrao() {
        return densidadeMinasPadrao;
    }

    public void setDensidadeMinasPadrao(double densidadeMinasPadrao) {
        this.densidadeMinasPadrao = densidadeMinasPadrao;
    }

    public int getTamanhoCelula() {
        return tamanhoCelula;
    }

    public void setTamanhoCelula(int tamanhoCelula) {
        this.tamanhoCelula = Math.max(20, Math.min(64, tamanhoCelula));
    }
}
