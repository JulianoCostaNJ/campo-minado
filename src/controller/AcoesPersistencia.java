package controller;

import persistence.Configuracoes;

import java.io.File;

/**
 * Ações relacionadas a salvar/carregar dados (sugestões #29, #32, #34).
 * Separada de {@link AcoesJogador} para não misturar "jogar" com
 * "persistir" na mesma interface (Interface Segregation Principle).
 */
public interface AcoesPersistencia {

    /** Salva a partida em andamento (sugestão #29). */
    void aoSalvarPartida();

    /** Carrega a última partida salva do perfil ativo, se existir (sugestão #29). */
    void aoContinuarPartidaSalva();

    /** Exporta o histórico de partidas do perfil ativo para um CSV (sugestão #32). */
    void aoExportarEstatisticasCsv(File destino);

    /** Aplica e persiste um novo conjunto de configurações (sugestão #34). */
    void aoAtualizarConfiguracoes(Configuracoes novaConfiguracao);
}
