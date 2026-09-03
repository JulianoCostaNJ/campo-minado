package controller;

import audio.EfeitoSonoro;
import audio.Musica;
import audio.ServicoMusica;
import audio.ServicoMusicaClip;
import audio.ServicoSom;
import audio.ServicoSomClip;
import model.ModoJogadores;
import model.RegrasJogo;
import model.Tabuleiro;
import persistence.Configuracoes;
import persistence.PersistenciaException;
import persistence.Perfil;
import persistence.RegistroPartida;
import persistence.SaveGame;
import repository.ArquivoConfiguracoesRepository;
import repository.ArquivoEstatisticasRepository;
import repository.ArquivoPerfilRepository;
import repository.ArquivoSaveGameRepository;
import repository.ConfiguracoesRepository;
import repository.EstatisticasRepository;
import repository.PerfilRepository;
import repository.SaveGameRepository;
import service.Conquista;
import service.ConquistaService;
import service.ContextoResultadoPartida;
import service.DicaService;
import service.GravadorJogadas;
import service.Jogada;
import service.TipoJogada;
import view.CampoMinadoView;

import javax.swing.Timer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CONTROLLER da arquitetura MVC: é o único ponto que conhece tanto o
 * {@link Tabuleiro} (Model) quanto a {@link CampoMinadoView} (View).
 * Recebe notificações de clique da View através de {@link AcoesJogador},
 * aplica a jogada no Model e manda a View se redesenhar. A View nunca
 * toca no Model diretamente, e o Model nunca conhece a View.
 * <p>
 * Além do núcleo de jogo original, o Controller agora também orquestra
 * (sem implementar a lógica de cada uma, que fica delegada a serviços e
 * repositórios dedicados): som e música (sugestões #35, #36), dicas
 * (#23), modo de depuração (#13), conquistas (#50), replay (#41), modo
 * cooperativo e competitivo (#19, #43), perfis (#33) e persistência de
 * partida/estatísticas/configurações (#29, #30, #31, #32, #34, #42).
 */
public class CampoMinadoController implements AcoesJogador {

    private static final int LIMITE_DICAS = 3;
    private static final int LIMITE_RANKING = 10;
    private static final int ATRASO_REPLAY_MS = 300;

    private final CampoMinadoView view;

    private final SaveGameRepository saveGameRepository;
    private final EstatisticasRepository estatisticasRepository;
    private final ConfiguracoesRepository configuracoesRepository;
    private final PerfilRepository perfilRepository;
    private final ServicoSom servicoSom;
    private final ServicoMusica servicoMusica;
    private final DicaService dicaService;
    private final ConquistaService conquistaService;
    private final GravadorJogadas gravadorJogadas;

    private Tabuleiro tabuleiro;
    private int totalMinas;
    private int totalCelulas;
    private int celulasReveladas;
    private int jogadas;
    private boolean jogoIniciado;
    private long tempoInicio;
    private int limiteSegundos;
    private Timer timerJogo;

    private int linhasPartidaAtual;
    private int colunasPartidaAtual;
    private String nomeDificuldadeAtual = "Personalizado";
    private ModoJogadores modoJogadores = ModoJogadores.INDIVIDUAL;
    private int jogadorDaVezCooperativo = 1;

    private ConfiguracaoPartida configuracaoCompetitivaAtual;
    private int turnoCompetitivo = 1;
    private int tempoJogador1;
    private boolean vitoriaJogador1;

    private int dicasRestantes;
    private int dicasUsadasNaPartida;
    private boolean bandeiraUsadaNaPartida;
    private boolean modoDebugMinasVisiveis;

    private boolean emReplay;
    private List<int[]> posicoesMinasCapturadas;
    private RegrasJogo regrasPartidaAtual;

    private Perfil perfilAtivo;
    private Configuracoes configuracoesAtuais;

    public CampoMinadoController(CampoMinadoView view) {
        this(view, new ArquivoSaveGameRepository(), new ArquivoEstatisticasRepository(),
                new ArquivoConfiguracoesRepository(), new ArquivoPerfilRepository(),
                new ServicoSomClip(), new ServicoMusicaClip(), new DicaService(), new ConquistaService());
    }

    /**
     * Construtor completo, útil para trocar qualquer peça (por exemplo,
     * repositórios em memória) sem depender das implementações concretas
     * — todas as dependências chegam de fora, já como interface.
     */
    public CampoMinadoController(CampoMinadoView view, SaveGameRepository saveGameRepository,
                                  EstatisticasRepository estatisticasRepository,
                                  ConfiguracoesRepository configuracoesRepository,
                                  PerfilRepository perfilRepository, ServicoSom servicoSom,
                                  ServicoMusica servicoMusica, DicaService dicaService,
                                  ConquistaService conquistaService) {
        this.view = view;
        this.saveGameRepository = saveGameRepository;
        this.estatisticasRepository = estatisticasRepository;
        this.configuracoesRepository = configuracoesRepository;
        this.perfilRepository = perfilRepository;
        this.servicoSom = servicoSom;
        this.servicoMusica = servicoMusica;
        this.dicaService = dicaService;
        this.conquistaService = conquistaService;
        this.gravadorJogadas = new GravadorJogadas();
        this.view.setOuvinte(this);
    }

    public void iniciar() {
        carregarConfiguracoesEPerfil();
        aplicarConfiguracoesNaView();
        mostrarTelaInicialAtualizada();
        view.setVisible(true);
    }

    private void carregarConfiguracoesEPerfil() {
        Configuracoes configuracoesCarregadas;
        try {
            configuracoesCarregadas = configuracoesRepository.carregar();
        } catch (PersistenciaException e) {
            configuracoesCarregadas = Configuracoes.padrao();
        }
        this.configuracoesAtuais = configuracoesCarregadas;

        try {
            this.perfilAtivo = perfilRepository.obterOuCriar(configuracoesAtuais.getPerfilAtivo());
        } catch (PersistenciaException e) {
            this.perfilAtivo = new Perfil(configuracoesAtuais.getPerfilAtivo());
        }

        servicoSom.setAtivado(configuracoesAtuais.isSomAtivado());
        servicoMusica.setVolume(configuracoesAtuais.getVolumeMusica());
        servicoMusica.setAtivado(configuracoesAtuais.isMusicaAtivada());
    }

    private void aplicarConfiguracoesNaView() {
        view.aplicarConfiguracoes(configuracoesAtuais);
        atualizarPainelMusicaNaView();
    }

    private void atualizarPainelMusicaNaView() {
        Musica faixa = servicoMusica.getFaixaAtual();
        view.atualizarPainelMusica(faixa != null ? faixa.getTitulo() : null,
                servicoMusica.isTocando(), servicoMusica.isAtivado(), servicoMusica.getVolume());
    }

    private void mostrarTelaInicialAtualizada() {
        boolean partidaSalvaDisponivel = perfilAtivo != null && saveGameRepository.existePartidaSalva(perfilAtivo.getNome());
        view.mostrarTelaInicial(partidaSalvaDisponivel);
    }

    private String nomeDificuldadeParaDimensoes(int linhas, int colunas, int minas) {
        if (linhas == 9 && colunas == 9 && minas == 10) {
            return "Iniciante";
        }
        if (linhas == 16 && colunas == 16 && minas == 40) {
            return "Intermediário";
        }
        if (linhas == 16 && colunas == 30 && minas == 99) {
            return "Avançado";
        }
        return "Personalizado";
    }

    // ================================================================
    // AcoesJogador — chamado pela View
    // ================================================================

    @Override
    public void aoEscolherDificuldade(int linhas, int colunas, int minas) {
        aoEscolherDificuldade(ConfiguracaoPartida.classica(linhas, colunas, minas,
                nomeDificuldadeParaDimensoes(linhas, colunas, minas)));
    }

    @Override
    public void aoEscolherDificuldade(ConfiguracaoPartida configuracao) {
        ConfiguracaoPartida efetiva = configuracao;
        if (configuracao.getModoJogadores() == ModoJogadores.COMPETITIVO && configuracao.getRegras().getSemente() == null) {
            RegrasJogo regrasComSemente = configuracao.getRegras().paraBuilder().comSemente(System.nanoTime()).build();
            efetiva = new ConfiguracaoPartida(configuracao.getLinhas(), configuracao.getColunas(),
                    configuracao.getMinas(), regrasComSemente, configuracao.getModoJogadores(), configuracao.getNomeDificuldade());
        }

        this.configuracaoCompetitivaAtual = efetiva;
        this.turnoCompetitivo = 1;
        iniciarPartida(efetiva);
    }

    private void iniciarPartida(ConfiguracaoPartida configuracao) {
        this.tabuleiro = new Tabuleiro(configuracao.getLinhas(), configuracao.getColunas(),
                configuracao.getMinas(), configuracao.getRegras());
        this.linhasPartidaAtual = configuracao.getLinhas();
        this.colunasPartidaAtual = configuracao.getColunas();
        this.totalMinas = configuracao.getMinas();
        this.totalCelulas = configuracao.getLinhas() * configuracao.getColunas() - totalMinas;
        this.celulasReveladas = contarCelulasReveladas();
        this.jogadas = 0;
        this.jogoIniciado = false;
        this.nomeDificuldadeAtual = configuracao.getNomeDificuldade();
        this.modoJogadores = configuracao.getModoJogadores();
        this.jogadorDaVezCooperativo = 1;
        this.dicasRestantes = LIMITE_DICAS;
        this.dicasUsadasNaPartida = 0;
        this.bandeiraUsadaNaPartida = false;
        this.modoDebugMinasVisiveis = false;
        this.emReplay = false;
        this.gravadorJogadas.reiniciar();
        this.posicoesMinasCapturadas = tabuleiro.obterPosicoesMinas();
        this.regrasPartidaAtual = tabuleiro.getRegras();

        pararTimer();
        view.aplicarTemaSelecionado();

        this.limiteSegundos = view.getTempoLimiteSegundosSelecionado();
        view.iniciarTelaDeJogo(configuracao.getLinhas(), configuracao.getColunas(), totalMinas, totalCelulas, limiteSegundos);
        redesenharTabuleiroCompleto();
        view.atualizarEstatisticas(totalMinas, celulasReveladas, totalCelulas, 0);
        view.atualizarDicasRestantes(dicasRestantes);
        view.atualizarEstadoSom(servicoSom.isAtivado());
        if (modoJogadores == ModoJogadores.COOPERATIVO) {
            view.atualizarJogadorDaVez(jogadorDaVezCooperativo);
        } else {
            view.atualizarJogadorDaVez(0);
        }
    }

    private void redesenharTabuleiroCompleto() {
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                view.atualizarCelula(i, j, tabuleiro);
            }
        }
        if (modoDebugMinasVisiveis) {
            aplicarIndicadoresDebug();
        }
    }

    @Override
    public void aoPedirNovoJogo() {
        pararTimer();
        mostrarTelaInicialAtualizada();
    }

    @Override
    public void aoMarcarCelula(int linha, int coluna) {
        if (tabuleiro.isJogoEncerrado()) {
            return;
        }
        tabuleiro.alternarMarcacao(linha, coluna);
        if (tabuleiro.isMarcada(linha, coluna)) {
            bandeiraUsadaNaPartida = true;
            servicoSom.tocar(EfeitoSonoro.BANDEIRA);
        }
        gravadorJogadas.registrar(TipoJogada.MARCAR, linha, coluna);
        view.atualizarCelula(linha, coluna, tabuleiro);
        atualizarEstatisticasNaView();
        avancarTurnoCooperativoSeNecessario();
    }

    @Override
    public void aoRevelarCelula(int linha, int coluna) {
        if (tabuleiro.isJogoEncerrado()) {
            return;
        }

        if (!jogoIniciado) {
            jogoIniciado = true;
            tempoInicio = System.currentTimeMillis();
            iniciarTimer();
        }

        if (limiteSegundos > 0 && obterSegundosPassados() >= limiteSegundos) {
            encerrarPorTempo();
            return;
        }

        jogadas++;
        gravadorJogadas.registrar(TipoJogada.REVELAR, linha, coluna);
        servicoSom.tocar(EfeitoSonoro.CLIQUE);
        List<int[]> reveladas = tabuleiro.revelar(linha, coluna);
        celulasReveladas = contarCelulasReveladas();
        avancarTurnoCooperativoSeNecessario();

        int atraso = reveladas.size() > 80 ? 3 : (reveladas.size() > 25 ? 8 : 18);
        animarRevelacao(reveladas, 0, atraso);
    }

    private void avancarTurnoCooperativoSeNecessario() {
        if (modoJogadores == ModoJogadores.COOPERATIVO && tabuleiro != null && !tabuleiro.isJogoEncerrado()) {
            jogadorDaVezCooperativo = jogadorDaVezCooperativo == 1 ? 2 : 1;
            view.atualizarJogadorDaVez(jogadorDaVezCooperativo);
        }
    }

    // ================================================================
    // Contagens e sincronização com a View
    // ================================================================

    private int contarCelulasReveladas() {
        int count = 0;
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                if (tabuleiro.isRevelada(i, j) && !tabuleiro.isMinada(i, j)) {
                    count++;
                }
            }
        }
        return count;
    }

    private int contarMarcadas() {
        int count = 0;
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                if (tabuleiro.isMarcada(i, j)) {
                    count++;
                }
            }
        }
        return count;
    }

    private void atualizarEstatisticasNaView() {
        int marcadas = contarMarcadas();
        int restantes = totalMinas - marcadas;
        view.atualizarEstatisticas(restantes, celulasReveladas, totalCelulas, jogadas);
        view.indicarExcessoDeBandeiras(marcadas > totalMinas);
    }

    // ================================================================
    // Timer do cronômetro
    // ================================================================

    private void iniciarTimer() {
        timerJogo = new Timer(1000, e -> atualizarTempo());
        timerJogo.start();
    }

    private void pararTimer() {
        if (timerJogo != null) {
            timerJogo.stop();
        }
    }

    private long obterSegundosPassados() {
        return (System.currentTimeMillis() - tempoInicio) / 1000;
    }

    private String formatarTempo(long segundos) {
        return String.format("%02d:%02d", segundos / 60, segundos % 60);
    }

    private void atualizarTempo() {
        long segundosPassados = obterSegundosPassados();
        if (limiteSegundos > 0) {
            long restantes = Math.max(0, limiteSegundos - segundosPassados);
            view.atualizarTempo("-" + formatarTempo(restantes));
            if (restantes <= 0) {
                encerrarPorTempo();
            }
        } else {
            view.atualizarTempo(formatarTempo(segundosPassados));
        }
    }

    private void encerrarPorTempo() {
        pararTimer();
        if (tabuleiro != null && !tabuleiro.isJogoEncerrado()) {
            tabuleiro.encerrarPorTempoEsgotado();
        }
        finalizarJogada();
    }

    // ================================================================
    // Animações (o Controller decide o ritmo; a View só desenha um passo)
    // ================================================================

    private void animarRevelacao(List<int[]> celulas, int indice, int atraso) {
        if (indice >= celulas.size()) {
            finalizarJogada();
            return;
        }
        int[] posicao = celulas.get(indice);
        view.atualizarCelula(posicao[0], posicao[1], tabuleiro);

        Timer timer = new Timer(atraso, e -> animarRevelacao(celulas, indice + 1, atraso));
        timer.setRepeats(false);
        timer.start();
    }

    private void finalizarJogada() {
        atualizarEstatisticasNaView();

        if (!tabuleiro.isJogoEncerrado()) {
            return;
        }

        pararTimer();
        boolean vitoria = !tabuleiro.isDerrota();

        if (emReplay) {
            exibirResultadoVisual(vitoria);
            return;
        }

        registrarResultadoDaPartida(vitoria);

        if (modoJogadores == ModoJogadores.COMPETITIVO) {
            finalizarTurnoCompetitivo(vitoria);
            return;
        }

        exibirResultadoVisual(vitoria);
    }

    private void exibirResultadoVisual(boolean vitoria) {
        if (!vitoria) {
            servicoSom.tocar(EfeitoSonoro.EXPLOSAO);
            view.mostrarDerrota();
            view.sacudirJanela();
            animarExplosao();
        } else {
            servicoSom.tocar(EfeitoSonoro.VITORIA);
            view.mostrarVitoria();
            animarVitoria();
            view.mostrarConfete();
        }
    }

    private void registrarResultadoDaPartida(boolean vitoria) {
        saveGameRepository.excluir(perfilAtivo.getNome());

        int tempoSegundos = (int) obterSegundosPassados();
        RegistroPartida registro = new RegistroPartida(perfilAtivo.getNome(), nomeDificuldadeAtual, vitoria,
                tempoSegundos, jogadas, modoJogadores);
        try {
            estatisticasRepository.registrarPartida(registro);
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Não foi possível salvar as estatísticas desta partida: " + e.getMessage());
        }

        int vidasPerdidas = tabuleiro.getVidasIniciais() - tabuleiro.getVidasRestantes();
        ContextoResultadoPartida contexto = new ContextoResultadoPartida(vitoria, tempoSegundos, nomeDificuldadeAtual,
                bandeiraUsadaNaPartida, dicasUsadasNaPartida, vidasPerdidas);
        List<Conquista> novasConquistas =
                conquistaService.avaliarNovasConquistas(contexto, perfilAtivo.getConquistasDesbloqueadas());
        if (!novasConquistas.isEmpty()) {
            for (Conquista conquista : novasConquistas) {
                perfilAtivo.desbloquear(conquista.name());
            }
            try {
                perfilRepository.salvar(perfilAtivo);
            } catch (PersistenciaException ignorada) {
                // A conquista já foi comemorada na tela; falha ao persistir não deve travar o jogo.
            }
            view.mostrarConquistasDesbloqueadas(novasConquistas);
        }
    }

    private void finalizarTurnoCompetitivo(boolean vitoriaJogadorAtual) {
        int tempoFinal = (int) obterSegundosPassados();
        if (turnoCompetitivo == 1) {
            this.tempoJogador1 = tempoFinal;
            this.vitoriaJogador1 = vitoriaJogadorAtual;
            this.turnoCompetitivo = 2;
            view.mostrarTransicaoCompetitiva(2, this::iniciarProximoTabuleiroCompetitivo);
        } else {
            view.mostrarResultadoCompetitivo(tempoJogador1, vitoriaJogador1, tempoFinal, vitoriaJogadorAtual);
        }
    }

    private void iniciarProximoTabuleiroCompetitivo() {
        iniciarPartida(configuracaoCompetitivaAtual);
    }

    private void animarExplosao() {
        Timer piscar = new Timer(100, null);
        int[] contador = {0};
        piscar.addActionListener(e -> {
            contador[0]++;
            view.piscarFundoDeExplosao(contador[0] % 2 == 1);
            if (contador[0] >= 6) {
                piscar.stop();
                view.piscarFundoDeExplosao(false);
                revelarMinasComAnimacao();
            }
        });
        piscar.start();
    }

    private void revelarMinasComAnimacao() {
        List<int[]> minasNaoReveladas = new ArrayList<>();
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                if (tabuleiro.isMinada(i, j) && !tabuleiro.isRevelada(i, j)) {
                    minasNaoReveladas.add(new int[]{i, j});
                }
            }
        }
        revelarMinasPasso(minasNaoReveladas, 0);
    }

    private void revelarMinasPasso(List<int[]> minas, int indice) {
        if (indice >= minas.size()) {
            return;
        }
        int[] posicao = minas.get(indice);
        view.marcarMinaExplodida(posicao[0], posicao[1]);

        Timer timer = new Timer(80, e -> revelarMinasPasso(minas, indice + 1));
        timer.setRepeats(false);
        timer.start();
    }

    private void animarVitoria() {
        List<int[]> celulasSeguras = new ArrayList<>();
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                if (tabuleiro.isRevelada(i, j) && !tabuleiro.isMinada(i, j)) {
                    celulasSeguras.add(new int[]{i, j});
                }
            }
        }
        vitoriaPasso(celulasSeguras, 0);
    }

    private void vitoriaPasso(List<int[]> celulas, int indice) {
        if (indice >= celulas.size()) {
            return;
        }
        int[] atual = celulas.get(indice);
        view.destacarCelulaVencedora(atual[0], atual[1]);

        Timer timer = new Timer(8, e -> vitoriaPasso(celulas, indice + 1));
        timer.setRepeats(false);
        timer.start();
    }

    // ================================================================
    // AcoesExtras — dica, depuração, compartilhar, replay, estatísticas
    // ================================================================

    @Override
    public void aoPedirDica() {
        if (tabuleiro == null || tabuleiro.isJogoEncerrado()) {
            return;
        }
        if (dicasRestantes <= 0) {
            view.mostrarMensagem("Você já usou todas as dicas desta partida.");
            return;
        }
        int[] sugestao = dicaService.sugerirCelulaSegura(tabuleiro);
        if (sugestao == null) {
            view.mostrarMensagem("Não há nenhuma célula segura sobrando para sugerir.");
            return;
        }
        dicasRestantes--;
        dicasUsadasNaPartida++;
        view.atualizarDicasRestantes(dicasRestantes);
        aoRevelarCelula(sugestao[0], sugestao[1]);
    }

    @Override
    public void aoAlternarModoDebugMinasVisiveis() {
        if (tabuleiro == null || tabuleiro.isJogoEncerrado()) {
            return;
        }
        modoDebugMinasVisiveis = !modoDebugMinasVisiveis;
        if (modoDebugMinasVisiveis) {
            aplicarIndicadoresDebug();
        } else {
            limparIndicadoresDebug();
        }
        view.indicarModoDebug(modoDebugMinasVisiveis);
    }

    private void aplicarIndicadoresDebug() {
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                if (tabuleiro.isMinada(i, j) && !tabuleiro.isRevelada(i, j)) {
                    view.indicarMinaDebug(i, j, true);
                }
            }
        }
    }

    private void limparIndicadoresDebug() {
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                if (tabuleiro.isMinada(i, j) && !tabuleiro.isRevelada(i, j)) {
                    view.indicarMinaDebug(i, j, false);
                }
            }
        }
    }

    @Override
    public void aoCompartilharResultado() {
        if (tabuleiro == null || !tabuleiro.isJogoEncerrado()) {
            view.mostrarMensagem("Termine uma partida antes de compartilhar o resultado.");
            return;
        }
        String verbo = tabuleiro.isDerrota() ? "Não consegui terminar" : "Terminei";
        String texto = String.format("%s o Campo Minado em %s no modo %s! \uD83D\uDCA3",
                verbo, formatarTempo(obterSegundosPassados()), nomeDificuldadeAtual);
        view.copiarParaAreaDeTransferencia(texto);
        view.mostrarMensagem("Resultado copiado para a área de transferência!");
    }

    @Override
    public void aoIniciarReplay() {
        if (posicoesMinasCapturadas == null || !gravadorJogadas.possuiJogadas()) {
            view.mostrarMensagem("Não há uma partida recente para reproduzir.");
            return;
        }
        if (modoJogadores != ModoJogadores.INDIVIDUAL) {
            view.mostrarMensagem("O replay está disponível apenas para partidas individuais.");
            return;
        }

        pararTimer();
        emReplay = true;
        gravadorJogadas.pausarGravacao();

        int[][] posicoes = posicoesMinasCapturadas.toArray(new int[0][]);
        this.tabuleiro = new Tabuleiro(linhasPartidaAtual, colunasPartidaAtual, posicoes, regrasPartidaAtual);
        this.celulasReveladas = contarCelulasReveladas();
        this.jogadas = 0;

        view.iniciarTelaDeJogo(linhasPartidaAtual, colunasPartidaAtual, totalMinas, totalCelulas, 0);
        redesenharTabuleiroCompleto();
        view.atualizarEstatisticas(totalMinas, celulasReveladas, totalCelulas, 0);

        reproduzirJogadaReplay(gravadorJogadas.getJogadas(), 0);
    }

    private void reproduzirJogadaReplay(List<Jogada> jogadasGravadas, int indice) {
        if (indice >= jogadasGravadas.size() || tabuleiro.isJogoEncerrado()) {
            emReplay = false;
            gravadorJogadas.retomarGravacao();
            return;
        }

        Jogada jogada = jogadasGravadas.get(indice);
        if (jogada.getTipo() == TipoJogada.REVELAR) {
            List<int[]> reveladas = tabuleiro.revelar(jogada.getLinha(), jogada.getColuna());
            for (int[] posicao : reveladas) {
                view.atualizarCelula(posicao[0], posicao[1], tabuleiro);
            }
            celulasReveladas = contarCelulasReveladas();
            jogadas++;
            atualizarEstatisticasNaView();
            if (tabuleiro.isJogoEncerrado()) {
                finalizarJogada();
            }
        } else {
            tabuleiro.alternarMarcacao(jogada.getLinha(), jogada.getColuna());
            view.atualizarCelula(jogada.getLinha(), jogada.getColuna(), tabuleiro);
            atualizarEstatisticasNaView();
        }

        Timer timer = new Timer(ATRASO_REPLAY_MS, e -> reproduzirJogadaReplay(jogadasGravadas, indice + 1));
        timer.setRepeats(false);
        timer.start();
    }

    @Override
    public void aoAbrirEstatisticas() {
        try {
            List<RegistroPartida> historico = estatisticasRepository.listarHistorico(perfilAtivo.getNome());
            String dificuldadeParaRanking = nomeDificuldadeAtual != null ? nomeDificuldadeAtual : "Iniciante";
            List<RegistroPartida> ranking = estatisticasRepository.ranking(dificuldadeParaRanking, LIMITE_RANKING);
            view.mostrarEstatisticas(historico, ranking, perfilAtivo.getConquistasDesbloqueadas());
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Não foi possível carregar as estatísticas: " + e.getMessage());
        }
    }

    // ================================================================
    // AcoesPersistencia — salvar/carregar partida, exportar CSV, configurações
    // ================================================================

    @Override
    public void aoSalvarPartida() {
        if (tabuleiro == null || tabuleiro.isJogoEncerrado()) {
            view.mostrarMensagem("Não há uma partida em andamento para salvar.");
            return;
        }
        SaveGame saveGame = new SaveGame(tabuleiro, nomeDificuldadeAtual, (int) obterSegundosPassados(),
                limiteSegundos, jogadas, modoJogadores);
        try {
            saveGameRepository.salvar(perfilAtivo.getNome(), saveGame);
            view.mostrarMensagem("Partida salva! Você pode continuar de onde parou na próxima vez.");
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Não foi possível salvar a partida: " + e.getMessage());
        }
    }

    @Override
    public void aoContinuarPartidaSalva() {
        try {
            Optional<SaveGame> carregado = saveGameRepository.carregar(perfilAtivo.getNome());
            if (!carregado.isPresent()) {
                view.mostrarMensagem("Não há nenhuma partida salva.");
                return;
            }
            restaurarPartidaSalva(carregado.get());
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Não foi possível carregar a partida salva: " + e.getMessage());
        }
    }

    private void restaurarPartidaSalva(SaveGame saveGame) {
        this.tabuleiro = saveGame.getTabuleiro();
        this.nomeDificuldadeAtual = saveGame.getNomeDificuldade();
        this.limiteSegundos = saveGame.getLimiteSegundos();
        this.jogadas = saveGame.getJogadas();
        this.modoJogadores = saveGame.getModoJogadores();
        this.linhasPartidaAtual = tabuleiro.getLinhas();
        this.colunasPartidaAtual = tabuleiro.getColunas();
        this.totalMinas = tabuleiro.getNumMinas();
        this.totalCelulas = linhasPartidaAtual * colunasPartidaAtual - totalMinas;
        this.celulasReveladas = contarCelulasReveladas();
        this.jogoIniciado = true;
        this.tempoInicio = System.currentTimeMillis() - saveGame.getTempoDecorridoSegundos() * 1000L;
        this.dicasRestantes = LIMITE_DICAS;
        this.dicasUsadasNaPartida = 0;
        this.bandeiraUsadaNaPartida = false;
        this.modoDebugMinasVisiveis = false;
        this.emReplay = false;
        this.gravadorJogadas.reiniciar();
        this.gravadorJogadas.pausarGravacao();
        this.posicoesMinasCapturadas = tabuleiro.obterPosicoesMinas();
        this.regrasPartidaAtual = tabuleiro.getRegras();

        view.iniciarTelaDeJogo(linhasPartidaAtual, colunasPartidaAtual, totalMinas, totalCelulas, limiteSegundos);
        redesenharTabuleiroCompleto();
        view.atualizarEstatisticas(totalMinas - contarMarcadas(), celulasReveladas, totalCelulas, jogadas);
        view.atualizarDicasRestantes(dicasRestantes);
        iniciarTimer();
    }

    @Override
    public void aoExportarEstatisticasCsv(File destino) {
        try {
            estatisticasRepository.exportarCsv(perfilAtivo.getNome(), destino);
            view.mostrarMensagem("Estatísticas exportadas para " + destino.getName() + ".");
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Não foi possível exportar o CSV: " + e.getMessage());
        }
    }

    @Override
    public void aoAtualizarConfiguracoes(Configuracoes novaConfiguracao) {
        this.configuracoesAtuais = novaConfiguracao;
        try {
            configuracoesRepository.salvar(configuracoesAtuais);
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Não foi possível salvar as configurações: " + e.getMessage());
        }
        servicoSom.setAtivado(configuracoesAtuais.isSomAtivado());
        servicoMusica.setVolume(configuracoesAtuais.getVolumeMusica());
        servicoMusica.setAtivado(configuracoesAtuais.isMusicaAtivada());
        aplicarConfiguracoesNaView();
        view.atualizarEstadoSom(servicoSom.isAtivado());
    }

    // ================================================================
    // AcoesPerfil
    // ================================================================

    @Override
    public void aoSelecionarPerfil(String nome) {
        try {
            this.perfilAtivo = perfilRepository.obterOuCriar(nome);
            this.configuracoesAtuais.setPerfilAtivo(nome);
            configuracoesRepository.salvar(configuracoesAtuais);
            mostrarTelaInicialAtualizada();
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Não foi possível trocar de perfil: " + e.getMessage());
        }
    }

    @Override
    public void aoAbrirSelecaoDePerfil() {
        try {
            List<String> nomes = new ArrayList<>();
            for (Perfil perfil : perfilRepository.listarPerfis()) {
                nomes.add(perfil.getNome());
            }
            if (perfilAtivo != null && !nomes.contains(perfilAtivo.getNome())) {
                nomes.add(perfilAtivo.getNome());
            }
            view.mostrarSelecaoDePerfil(nomes, perfilAtivo != null ? perfilAtivo.getNome() : null);
        } catch (PersistenciaException e) {
            view.mostrarMensagem("Não foi possível carregar os perfis: " + e.getMessage());
        }
    }

    // ================================================================
    // AcoesAudio
    // ================================================================

    @Override
    public void aoAlternarSom() {
        servicoSom.setAtivado(!servicoSom.isAtivado());
        configuracoesAtuais.setSomAtivado(servicoSom.isAtivado());
        salvarConfiguracoesSilenciosamente();
        view.atualizarEstadoSom(servicoSom.isAtivado());
    }

    @Override
    public void aoAlternarMusica() {
        servicoMusica.setAtivado(!servicoMusica.isAtivado());
        configuracoesAtuais.setMusicaAtivada(servicoMusica.isAtivado());
        salvarConfiguracoesSilenciosamente();
        atualizarPainelMusicaNaView();
    }

    @Override
    public void aoAjustarVolumeMusica(float volume) {
        servicoMusica.setVolume(volume);
        configuracoesAtuais.setVolumeMusica(servicoMusica.getVolume());
        salvarConfiguracoesSilenciosamente();
        atualizarPainelMusicaNaView();
    }

    @Override
    public void aoProximaMusica() {
        servicoMusica.proxima();
        atualizarPainelMusicaNaView();
    }

    @Override
    public void aoMusicaAnterior() {
        servicoMusica.anterior();
        atualizarPainelMusicaNaView();
    }

    private void salvarConfiguracoesSilenciosamente() {
        try {
            configuracoesRepository.salvar(configuracoesAtuais);
        } catch (PersistenciaException ignorada) {
            // Preferências de áudio não são críticas o bastante para interromper o jogador com um erro.
        }
    }
}
