package view;

import controller.AcoesJogador;
import controller.ConfiguracaoPartida;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import model.LeituraTabuleiro;
import model.ModoJogadores;
import model.RegrasJogo;
import persistence.Configuracoes;
import persistence.RegistroPartida;
import service.Conquista;
import view.componentes.DialogConfiguracoes;
import view.componentes.DialogDificuldadeCustom;
import view.componentes.DialogEstatisticas;
import view.componentes.DialogPerfil;
import view.componentes.PainelConfete;
import view.componentes.PainelMusica;
import view.skin.SkinBandeira;
import view.skin.SkinCelula;
import view.tema.RegistroTemas;
import view.tema.TemaVisual;

/**
 * VIEW da arquitetura MVC: cuida só de desenhar a tela e capturar
 * interações do usuário. Nunca decide o que um clique "significa" em
 * termos de regra de jogo — ela apenas repassa o clique para quem
 * implementa {@link AcoesJogador} (o Controller) e espera ser chamada de
 * volta para atualizar o que aparece na tela.
 * <p>
 * As cores de fundo/tabuleiro que antes viviam soltas em campos
 * {@code corXxx}, trocadas manualmente em {@code aplicarTemaSelecionado},
 * agora vêm de um único {@link TemaVisual} (sugestão #1 — tema plugável)
 * escolhido em {@link RegistroTemas}. Isso também corrige uma
 * inconsistência do código original: o preenchimento das células
 * ocultas usava sempre a cor fixa {@code COR_CELULA_OCULTA}, enquanto só
 * a borda respeitava o tema do tabuleiro escolhido — então trocar para
 * "Noite" ou "Verde" mudava o contorno da célula, mas nunca o fundo dela.
 * Com um único {@code TemaVisual} lido em todo lugar, isso não pode mais
 * acontecer.
 */
public class CampoMinadoView extends JFrame {

    // Cores "semânticas": mantidas fixas independentemente do tema, porque
    // representam sempre a mesma coisa (mina = vermelho, vitória = verde,
    // bandeira = dourado) em qualquer esquema visual.
    private static final Color COR_MINA = new Color(220, 60, 60);
    private static final Color COR_VITORIA = new Color(50, 180, 80);
    private static final Color COR_BANDEIRA = new Color(230, 180, 50);

    private static final String[] TEMPOS_JOGO = {"Sem limite", "1 minuto", "2 minutos", "3 minutos", "5 minutos"};

    private static final Font FONTE_CELULA = new Font("Segoe UI Emoji", Font.BOLD, 20);
    private static final Font FONTE_TITULO = new Font("Segoe UI Emoji", Font.BOLD, 28);
    private static final Font FONTE_SUBTITULO = new Font("Segoe UI Emoji", Font.BOLD, 16);
    private static final Font FONTE_NORMAL = new Font("Segoe UI Emoji", Font.PLAIN, 14);
    private static final Font FONTE_NUMERO = new Font("Consolas", Font.BOLD, 18);
    private static final Font FONTE_PEQUENA = new Font("Segoe UI Emoji", Font.PLAIN, 12);

    private static final String EMOJI_BOMBA = "\uD83D\uDCA3";
    private static final String EMOJI_TROFEU = "\uD83C\uDFC6";
    private static final String EMOJI_EXPLOSAO = "\uD83D\uDCA5";
    private static final String EMOJI_RELOGIO = "\u23F1";
    private static final String EMOJI_JOGADA = "\uD83D\uDC46";
    private static final String EMOJI_ICONE_ESTATISTICA = EMOJI_BOMBA;

    // Esquema clássico do Campo Minado, pensado para boa leitura sobre o
    // fundo claro (getCelulaRevelada()) da célula já revelada. Mantido
    // fixo por número — mudar de tema não deveria fazer o "1" azul virar
    // outra cor, isso quebraria a leitura instantânea que quem já jogou
    // Campo Minado em qualquer lugar já tem treinada.
    private static final Color[] CORES_NUMEROS = {
            null,
            new Color(25, 118, 210),   // 1 - azul
            new Color(56, 142, 60),    // 2 - verde
            new Color(211, 47, 47),    // 3 - vermelho
            new Color(13, 71, 161),    // 4 - azul-marinho
            new Color(136, 14, 14),    // 5 - vinho
            new Color(0, 131, 143),    // 6 - teal
            new Color(33, 33, 33),     // 7 - preto
            new Color(97, 97, 97)      // 8 - cinza-escuro
    };

    // Símbolos extras para o modo de acessibilidade a daltonismo
    // (sugestão #25) — cada contagem ganha uma forma própria, então a
    // informação não depende só da cor do número.
    private static final String[] SIMBOLOS_DALTONICOS =
            {"\u25B2", "\u25CF", "\u25A0", "\u25C6", "\u25BC", "\u2605", "\u271A", "\u2630"};

    private AcoesJogador ouvinte;
    private JButton[][] botoes;
    private JPanel painelTabuleiroAtual;
    private JLabel labelStatus;
    private JLabel lblJogadorDaVez;
    private JLabel lblAvisoBandeiras;

    private JLabel lblTempo;
    private JLabel lblMinasRestantes;
    private JLabel lblCelulasReveladas;
    private JLabel lblJogadas;
    private JLabel lblDicasRestantes;
    private JProgressBar barraProgresso;

    private JComboBox<String> comboTema;
    private JComboBox<String> comboTempo;
    private JButton botaoSom;
    private JButton botaoDebug;
    private PainelMusica painelMusica;

    private final RegistroTemas registroTemas;
    private TemaVisual temaAtual;
    private SkinBandeira skinBandeiraAtual;
    private SkinCelula skinCelulaAtual;
    private boolean simbolosDaltonicosAtivo;
    private int tamanhoCelulaAtual;
    private Configuracoes configuracoesAtuais;

    private int cursorLinha;
    private int cursorColuna;

    public CampoMinadoView() {
        super("Campo Minado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.registroTemas = new RegistroTemas();
        this.temaAtual = registroTemas.obter("Escuro");
        this.skinBandeiraAtual = SkinBandeira.PADRAO;
        this.skinCelulaAtual = SkinCelula.NUMEROS;
        this.tamanhoCelulaAtual = 36;
        getContentPane().setBackground(temaAtual.getFundo());
        setLocationRelativeTo(null);
        // Redimensionável para suportar tela cheia (sugestão #47) e um
        // tabuleiro que se adapta ao tamanho da janela (sugestão #48).
        setResizable(true);
    }

    /** Define quem recebe os eventos de clique/escolha (o Controller). */
    public void setOuvinte(AcoesJogador ouvinte) {
        this.ouvinte = ouvinte;
        if (painelMusica != null) {
            painelMusica.setOuvinte(ouvinte);
        }
    }

    // ================================================================
    // TELA INICIAL
    // ================================================================

    /**
     * @param partidaSalvaDisponivel se true, mostra um botão de destaque
     *                               para retomar a partida salva do
     *                               perfil ativo (sugestão #29).
     */
    public void mostrarTelaInicial(boolean partidaSalvaDisponivel) {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JPanel painelCentral = new JPanel(new GridBagLayout());
        painelCentral.setBackground(temaAtual.getFundo());
        painelCentral.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        JPanel painelConteudo = new JPanel();
        painelConteudo.setLayout(new BoxLayout(painelConteudo, BoxLayout.Y_AXIS));
        painelConteudo.setBackground(temaAtual.getFundo());
        painelConteudo.setAlignmentX(Component.CENTER_ALIGNMENT);

        painelConteudo.add(criarBarraSuperiorInicial());
        painelConteudo.add(Box.createVerticalStrut(10));

        JLabel titulo = new JLabel(EMOJI_BOMBA + " Campo Minado");
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(temaAtual.getTextoPrincipal());
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelConteudo.add(titulo);

        JLabel subtitulo = new JLabel("Escolha sua dificuldade");
        subtitulo.setFont(FONTE_NORMAL);
        subtitulo.setForeground(temaAtual.getTextoSecundario());
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        painelConteudo.add(subtitulo);

        if (partidaSalvaDisponivel) {
            JButton continuarPartida = new JButton("\u25B6 Continuar partida salva");
            continuarPartida.setFont(FONTE_SUBTITULO);
            continuarPartida.setForeground(temaAtual.getDestaque());
            continuarPartida.setBackground(temaAtual.getCard());
            continuarPartida.setFocusPainted(false);
            continuarPartida.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(temaAtual.getDestaque()),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)));
            continuarPartida.setAlignmentX(Component.CENTER_ALIGNMENT);
            continuarPartida.setCursor(new Cursor(Cursor.HAND_CURSOR));
            continuarPartida.addActionListener(e -> {
                if (ouvinte != null) {
                    ouvinte.aoContinuarPartidaSalva();
                }
            });
            painelConteudo.add(continuarPartida);
            painelConteudo.add(Box.createVerticalStrut(20));
        }

        JPanel painelCards = new JPanel(new GridLayout(2, 2, 15, 15));
        painelCards.setBackground(temaAtual.getFundo());
        painelCards.setAlignmentX(Component.CENTER_ALIGNMENT);

        painelCards.add(criarCardDificuldade("Iniciante", "9 × 9", "10 minas", 9, 9, 10));
        painelCards.add(criarCardDificuldade("Intermediário", "16 × 16", "40 minas", 16, 16, 40));
        painelCards.add(criarCardDificuldade("Avançado", "16 × 30", "99 minas", 16, 30, 99));
        painelCards.add(criarCardPersonalizado());

        painelConteudo.add(painelCards);
        painelConteudo.add(Box.createVerticalStrut(12));
        painelConteudo.add(criarBotaoDesafioDiario());

        JLabel dica = new JLabel("<html><center>\uD83D\uDDB1\uFE0F Esquerdo: revelar • Direito: bandeira • "
                + "Setas + Espaço/F: teclado</center></html>");
        dica.setFont(FONTE_PEQUENA);
        dica.setForeground(temaAtual.getTextoSecundario());
        dica.setAlignmentX(Component.CENTER_ALIGNMENT);
        dica.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        painelConteudo.add(dica);
        painelConteudo.add(Box.createVerticalStrut(15));
        painelConteudo.add(criarPainelOpcoes());
        painelConteudo.add(Box.createVerticalStrut(10));

        if (painelMusica == null) {
            painelMusica = new PainelMusica();
        }
        painelMusica.setOuvinte(ouvinte);
        painelMusica.aplicarCorTexto(temaAtual.getTextoPrincipal());
        painelConteudo.add(painelMusica);

        painelCentral.add(painelConteudo);
        add(painelCentral, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    private JPanel criarBarraSuperiorInicial() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        barra.setBackground(temaAtual.getFundo());
        barra.setAlignmentX(Component.CENTER_ALIGNMENT);

        String nomePerfil = configuracoesAtuais != null ? configuracoesAtuais.getPerfilAtivo() : "Jogador";
        JButton botaoPerfil = criarBotaoSecundario("\uD83D\uDC64 " + nomePerfil);
        botaoPerfil.addActionListener(e -> {
            if (ouvinte != null) {
                ouvinte.aoAbrirSelecaoDePerfil();
            }
        });

        JButton botaoEstatisticas = criarBotaoSecundario("\uD83D\uDCCA Estatísticas");
        botaoEstatisticas.addActionListener(e -> {
            if (ouvinte != null) {
                ouvinte.aoAbrirEstatisticas();
            }
        });

        JButton botaoConfiguracoes = criarBotaoSecundario("\u2699 Configurações");
        botaoConfiguracoes.addActionListener(e -> abrirDialogoConfiguracoes());

        barra.add(botaoPerfil);
        barra.add(botaoEstatisticas);
        barra.add(botaoConfiguracoes);
        return barra;
    }

    /** Botão pequeno com o estilo padrão da aplicação, já com o efeito de hover embutido. */
    private JButton criarBotaoSecundario(String texto) {
        JButton botao = new JButton(texto);
        botao.setFont(FONTE_PEQUENA);
        botao.setForeground(temaAtual.getTextoPrincipal());
        Color corNormal = temaAtual.getFundoClaro();
        Color corHover = temaAtual.getCardHover();
        botao.setBackground(corNormal);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(temaAtual.getBorda()),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(corHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(corNormal);
            }
        });
        return botao;
    }

    private void abrirDialogoConfiguracoes() {
        if (configuracoesAtuais == null) {
            return;
        }
        List<String> nomesTemas = registroTemas.listarNomes();
        List<String> nomesSkinsBandeira = new ArrayList<>();
        for (SkinBandeira skin : SkinBandeira.values()) {
            nomesSkinsBandeira.add(skin.getNomeExibicao());
        }
        List<String> nomesSkinsCelula = new ArrayList<>();
        for (SkinCelula skin : SkinCelula.values()) {
            nomesSkinsCelula.add(skin.getNomeExibicao());
        }

        DialogConfiguracoes dialogo = new DialogConfiguracoes(this, configuracoesAtuais, nomesTemas,
                nomesSkinsBandeira, nomesSkinsCelula,
                novaConfig -> {
                    if (ouvinte != null) {
                        ouvinte.aoAtualizarConfiguracoes(novaConfig);
                    }
                });
        dialogo.setVisible(true);
    }

    private JPanel criarPainelOpcoes() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(temaAtual.getFundo());
        painel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel linhaTema = criarLinhaSelecao("Tema:", registroTemas.listarNomes().toArray(new String[0]));
        comboTema = (JComboBox<String>) linhaTema.getClientProperty("combo");
        if (configuracoesAtuais != null) {
            comboTema.setSelectedItem(configuracoesAtuais.getNomeTema());
        }
        comboTema.addActionListener(e -> aplicarTemaSelecionado());
        painel.add(linhaTema);
        painel.add(Box.createVerticalStrut(10));

        JPanel linhaTempo = criarLinhaSelecao("Tempo rápido:", TEMPOS_JOGO);
        comboTempo = (JComboBox<String>) linhaTempo.getClientProperty("combo");
        painel.add(linhaTempo);
        painel.add(Box.createVerticalStrut(10));

        JButton btnTutorial = criarBotaoSecundario("Ver tutorial");
        btnTutorial.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTutorial.addActionListener(e -> mostrarTutorial());
        painel.add(btnTutorial);

        return painel;
    }

    private JPanel criarLinhaSelecao(String texto, String[] opcoes) {
        JPanel painel = new JPanel(new BorderLayout(10, 0));
        painel.setBackground(temaAtual.getFundo());
        painel.setMaximumSize(new Dimension(320, 40));

        JLabel lbl = new JLabel(texto);
        lbl.setFont(FONTE_PEQUENA);
        lbl.setForeground(temaAtual.getTextoSecundario());
        painel.add(lbl, BorderLayout.WEST);

        JComboBox<String> combo = new JComboBox<>(opcoes);
        combo.setFont(FONTE_PEQUENA);
        combo.setBackground(temaAtual.getFundoClaro());
        combo.setForeground(temaAtual.getTextoPrincipal());
        combo.setBorder(BorderFactory.createLineBorder(temaAtual.getBorda()));
        painel.add(combo, BorderLayout.EAST);
        painel.putClientProperty("combo", combo);

        return painel;
    }

    private void mostrarTutorial() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(temaAtual.getFundo());
        painel.setBorder(BorderFactory.createEmptyBorder(45, 45, 120, 45));

        JLabel titulo = new JLabel("Como jogar Campo Minado");
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(temaAtual.getTextoPrincipal());
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(20));

        String texto = "1. Escolha uma dificuldade (ou personalize) e um tempo rápido.\n"
                + "2. Clique com o botão esquerdo para revelar uma célula.\n"
                + "3. Clique com o botão direito para ciclar a marcação: bandeira, interrogação, nada.\n"
                + "4. Revele todas as células sem minas para vencer.\n"
                + "5. Se explodir uma mina, o jogo termina em derrota (a menos que o modo 3 vidas esteja ativo).\n"
                + "6. O tempo selecionado limita a partida; se chegar a zero, você perde.\n"
                + "7. Também dá para jogar só pelo teclado: setas movem o cursor, Espaço revela, F marca.\n"
                + "8. Use \uD83D\uDCA1 para uma dica, \uD83D\uDCBE para salvar e continuar depois, e \uD83D\uDD01 para reproduzir a última partida.\n";

        JTextArea area = new JTextArea(texto);
        area.setFont(FONTE_NORMAL);
        area.setForeground(temaAtual.getTextoPrincipal());
        area.setBackground(temaAtual.getFundoClaro());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painel.add(area);
        painel.add(Box.createVerticalStrut(15));

        JLabel dicas = new JLabel("Dicas: use bandeiras para marcar minas e tente abrir áreas sem números.");
        dicas.setFont(FONTE_PEQUENA);
        dicas.setForeground(temaAtual.getTextoSecundario());
        dicas.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(dicas);
        painel.add(Box.createVerticalStrut(25));

        JButton voltar = criarBotaoSecundario("Voltar");
        voltar.addActionListener(e -> mostrarTelaInicial(false));

        JPanel linhaVoltar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linhaVoltar.setBackground(temaAtual.getFundo());
        linhaVoltar.setAlignmentX(Component.CENTER_ALIGNMENT);
        linhaVoltar.setMaximumSize(new Dimension(Integer.MAX_VALUE, voltar.getPreferredSize().height));
        linhaVoltar.add(voltar);
        painel.add(linhaVoltar);

        add(painel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    private JPanel criarCardBase(String titulo, String dimensao, String minasTexto) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(temaAtual.getCard());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(temaAtual.getBorda(), 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(FONTE_SUBTITULO);
        lblTitulo.setForeground(temaAtual.getDestaque());
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitulo);

        JLabel lblDim = new JLabel(dimensao);
        lblDim.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDim.setForeground(temaAtual.getTextoPrincipal());
        lblDim.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDim.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        card.add(lblDim);

        JLabel lblMinas = new JLabel(EMOJI_BOMBA + " " + minasTexto);
        lblMinas.setFont(FONTE_NORMAL);
        lblMinas.setForeground(temaAtual.getTextoSecundario());
        lblMinas.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblMinas);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(temaAtual.getCardHover());
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(temaAtual.getDestaque(), 2),
                        BorderFactory.createEmptyBorder(19, 24, 19, 24)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(temaAtual.getCard());
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(temaAtual.getBorda(), 1),
                        BorderFactory.createEmptyBorder(20, 25, 20, 25)
                ));
            }
        });

        return card;
    }

    private JPanel criarCardDificuldade(String titulo, String dimensao, String minasTexto,
                                         int linhas, int colunas, int minas) {
        JPanel card = criarCardBase(titulo, dimensao, minasTexto);
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (ouvinte != null) {
                    ouvinte.aoEscolherDificuldade(linhas, colunas, minas);
                }
            }
        });
        return card;
    }

    /** Card de dificuldade personalizada (sugestão #14), que também dá acesso às regras variantes e ao modo de jogadores. */
    private JPanel criarCardPersonalizado() {
        JPanel card = criarCardBase("Personalizado", "Você escolhe", "linhas × colunas");
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                double densidade = configuracoesAtuais != null ? configuracoesAtuais.getDensidadeMinasPadrao() : 0.15625;
                DialogDificuldadeCustom dialogo = new DialogDificuldadeCustom(CampoMinadoView.this, densidade,
                        configuracao -> {
                            if (ouvinte != null) {
                                ouvinte.aoEscolherDificuldade(configuracao);
                            }
                        });
                dialogo.setVisible(true);
            }
        });
        return card;
    }

    /** Desafio diário (sugestão #49): mesma semente para todo mundo que jogar hoje. */
    private JButton criarBotaoDesafioDiario() {
        JButton botao = criarBotaoSecundario("\uD83D\uDDD3 Desafio Diário — mesmo tabuleiro para todo mundo hoje");
        botao.setAlignmentX(Component.CENTER_ALIGNMENT);
        botao.addActionListener(e -> {
            if (ouvinte == null) {
                return;
            }
            long semente = LocalDate.now().toEpochDay();
            RegrasJogo regras = RegrasJogo.construir().comSemente(semente).build();
            ConfiguracaoPartida configuracao = new ConfiguracaoPartida(9, 9, 10, regras, ModoJogadores.INDIVIDUAL, "Diário");
            ouvinte.aoEscolherDificuldade(configuracao);
        });
        return botao;
    }

    // ================================================================
    // TEMA E CONFIGURAÇÕES
    // ================================================================

    public int getTempoLimiteSegundosSelecionado() {
        if (comboTempo == null) {
            return 0;
        }
        String selecionado = (String) comboTempo.getSelectedItem();
        if (selecionado == null || selecionado.startsWith("Sem")) {
            return 0;
        }
        if (selecionado.contains("1 minuto")) {
            return 60;
        }
        if (selecionado.contains("2 minutos")) {
            return 120;
        }
        if (selecionado.contains("3 minutos")) {
            return 180;
        }
        if (selecionado.contains("5 minutos")) {
            return 300;
        }
        return 0;
    }

    /** Aplica o tema escolhido no combo da tela inicial (pré-visualização imediata, sugestão #1). */
    public void aplicarTemaSelecionado() {
        if (comboTema != null) {
            String nome = (String) comboTema.getSelectedItem();
            temaAtual = registroTemas.obter(nome);
        }
        getContentPane().setBackground(temaAtual.getFundo());
    }

    /**
     * Aplica (e guarda uma cópia de leitura de) as configurações
     * persistentes vindas do Controller (sugestão #34): tema, skins,
     * símbolos para daltonismo (#25) e tamanho de célula (parte do
     * zoom, #28). É também a partir daqui que os diálogos de
     * configurações/dificuldade personalizada sabem os valores atuais.
     */
    public void aplicarConfiguracoes(Configuracoes configuracoes) {
        this.configuracoesAtuais = configuracoes;
        this.temaAtual = registroTemas.obter(configuracoes.getNomeTema());
        this.skinBandeiraAtual = SkinBandeira.porNomeExibicao(configuracoes.getNomeSkinBandeira());
        this.skinCelulaAtual = SkinCelula.porNomeExibicao(configuracoes.getNomeSkinCelula());
        this.simbolosDaltonicosAtivo = configuracoes.isSimbolosDaltonicos();
        this.tamanhoCelulaAtual = configuracoes.getTamanhoCelula();
        if (comboTema != null) {
            comboTema.setSelectedItem(configuracoes.getNomeTema());
        }
        getContentPane().setBackground(temaAtual.getFundo());
        if (painelMusica != null) {
            painelMusica.aplicarCorTexto(temaAtual.getTextoPrincipal());
        }
        revalidate();
        repaint();
    }

    // ================================================================
    // TELA DE JOGO
    // ================================================================

    /**
     * Monta a tela de jogo do zero para um tabuleiro de {@code linhas} x
     * {@code colunas}. Não recebe o Model, apenas as dimensões — quem
     * decide o que cada célula mostra depois é sempre o Controller,
     * chamando {@link #atualizarCelula}.
     */
    public void iniciarTelaDeJogo(int linhas, int colunas, int totalMinas, int totalCelulas, int tempoLimiteSegundos) {
        getContentPane().removeAll();
        setLayout(new BorderLayout(0, 0));

        add(criarPainelSuperior(), BorderLayout.NORTH);

        JPanel painelPrincipal = new JPanel(new BorderLayout(15, 0));
        painelPrincipal.setBackground(temaAtual.getFundo());
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        painelPrincipal.add(criarPainelTabuleiro(linhas, colunas), BorderLayout.CENTER);
        painelPrincipal.add(criarPainelEstatisticas(totalMinas, totalCelulas), BorderLayout.EAST);

        add(painelPrincipal, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    private JPanel criarPainelSuperior() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(temaAtual.getFundo());
        painel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JButton btnNovo = criarBotaoSecundario("\u2190 Novo Jogo");
        btnNovo.addActionListener(e -> {
            if (ouvinte != null) {
                ouvinte.aoPedirNovoJogo();
            }
        });

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setBackground(temaAtual.getFundo());

        labelStatus = new JLabel("Boa sorte!", SwingConstants.CENTER);
        labelStatus.setFont(FONTE_SUBTITULO);
        labelStatus.setForeground(temaAtual.getTextoSecundario());
        labelStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(labelStatus);

        lblJogadorDaVez = new JLabel(" ", SwingConstants.CENTER);
        lblJogadorDaVez.setFont(FONTE_PEQUENA);
        lblJogadorDaVez.setForeground(temaAtual.getDestaque());
        lblJogadorDaVez.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(lblJogadorDaVez);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        direita.setBackground(temaAtual.getFundo());

        botaoSom = criarBotaoSecundario("\uD83D\uDD0A");
        botaoSom.setToolTipText("Ligar/desligar efeitos sonoros (sugestão #35)");
        botaoSom.addActionListener(e -> {
            if (ouvinte != null) {
                ouvinte.aoAlternarSom();
            }
        });

        botaoDebug = criarBotaoSecundario("\uD83D\uDC1E");
        botaoDebug.setToolTipText("Modo de depuração: mostrar minas (sugestão #13)");
        botaoDebug.addActionListener(e -> {
            if (ouvinte != null) {
                ouvinte.aoAlternarModoDebugMinasVisiveis();
            }
        });

        JButton botaoDica = criarBotaoSecundario("\uD83D\uDCA1");
        botaoDica.setToolTipText("Pedir uma dica (sugestão #23)");
        botaoDica.addActionListener(e -> {
            if (ouvinte != null) {
                ouvinte.aoPedirDica();
            }
        });

        JButton botaoSalvar = criarBotaoSecundario("\uD83D\uDCBE");
        botaoSalvar.setToolTipText("Salvar partida (sugestão #29)");
        botaoSalvar.addActionListener(e -> {
            if (ouvinte != null) {
                ouvinte.aoSalvarPartida();
            }
        });

        JButton botaoCompartilhar = criarBotaoSecundario("\uD83D\uDCE4");
        botaoCompartilhar.setToolTipText("Compartilhar resultado (sugestão #44)");
        botaoCompartilhar.addActionListener(e -> {
            if (ouvinte != null) {
                ouvinte.aoCompartilharResultado();
            }
        });

        JButton botaoReplay = criarBotaoSecundario("\uD83D\uDD01");
        botaoReplay.setToolTipText("Reproduzir a última partida (sugestão #41)");
        botaoReplay.addActionListener(e -> {
            if (ouvinte != null) {
                ouvinte.aoIniciarReplay();
            }
        });

        JButton botaoZoomMenos = criarBotaoSecundario("\u2212");
        botaoZoomMenos.setToolTipText("Diminuir zoom (sugestão #28)");
        botaoZoomMenos.addActionListener(e -> diminuirZoom());

        JButton botaoZoomMais = criarBotaoSecundario("+");
        botaoZoomMais.setToolTipText("Aumentar zoom (sugestão #28)");
        botaoZoomMais.addActionListener(e -> aumentarZoom());

        JButton botaoTelaCheia = criarBotaoSecundario("\u26F6");
        botaoTelaCheia.setToolTipText("Tela cheia (sugestão #47)");
        botaoTelaCheia.addActionListener(e -> alternarTelaCheia());

        direita.add(botaoSom);
        direita.add(botaoDebug);
        direita.add(botaoDica);
        direita.add(botaoSalvar);
        direita.add(botaoCompartilhar);
        direita.add(botaoReplay);
        direita.add(botaoZoomMenos);
        direita.add(botaoZoomMais);
        direita.add(botaoTelaCheia);

        painel.add(btnNovo, BorderLayout.WEST);
        painel.add(centro, BorderLayout.CENTER);
        painel.add(direita, BorderLayout.EAST);

        return painel;
    }

    private JPanel criarPainelEstatisticas(int totalMinas, int totalCelulas) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(temaAtual.getFundoClaro());
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(temaAtual.getBorda(), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        int largura = 190 + Math.min(100, totalMinas * 2);
        painel.setPreferredSize(new Dimension(largura, 0));

        JLabel lblTitulo = new JLabel("Estatísticas");
        lblTitulo.setFont(FONTE_SUBTITULO);
        lblTitulo.setForeground(temaAtual.getDestaque());
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(lblTitulo);
        painel.add(Box.createVerticalStrut(20));

        JPanel pnlTempo = criarItemEstatistica(EMOJI_RELOGIO + " Tempo", "00:00");
        lblTempo = (JLabel) pnlTempo.getClientProperty("valor");
        painel.add(pnlTempo);
        painel.add(Box.createVerticalStrut(15));

        JPanel pnlMinas = criarItemEstatistica(EMOJI_BOMBA + " Minas", String.valueOf(totalMinas));
        lblMinasRestantes = (JLabel) pnlMinas.getClientProperty("valor");
        painel.add(pnlMinas);

        lblAvisoBandeiras = new JLabel(" ");
        lblAvisoBandeiras.setFont(FONTE_PEQUENA);
        lblAvisoBandeiras.setForeground(new Color(220, 90, 90));
        lblAvisoBandeiras.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(lblAvisoBandeiras);
        painel.add(Box.createVerticalStrut(10));

        JPanel pnlReveladas = criarItemEstatistica(EMOJI_ICONE_ESTATISTICA + " Reveladas", "0 / " + totalCelulas);
        lblCelulasReveladas = (JLabel) pnlReveladas.getClientProperty("valor");
        painel.add(pnlReveladas);
        painel.add(Box.createVerticalStrut(15));

        JPanel pnlJogadas = criarItemEstatistica(EMOJI_JOGADA + " Jogadas", "0");
        lblJogadas = (JLabel) pnlJogadas.getClientProperty("valor");
        painel.add(pnlJogadas);
        painel.add(Box.createVerticalStrut(15));

        JPanel pnlDicas = criarItemEstatistica("\uD83D\uDCA1 Dicas", "3");
        lblDicasRestantes = (JLabel) pnlDicas.getClientProperty("valor");
        painel.add(pnlDicas);
        painel.add(Box.createVerticalStrut(20));

        JLabel lblProgTitulo = new JLabel("Progresso");
        lblProgTitulo.setFont(FONTE_NORMAL);
        lblProgTitulo.setForeground(temaAtual.getTextoSecundario());
        lblProgTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(lblProgTitulo);

        barraProgresso = new JProgressBar(0, Math.max(totalCelulas, 1));
        barraProgresso.setValue(0);
        barraProgresso.setStringPainted(true);
        barraProgresso.setString("0%");
        barraProgresso.setForeground(temaAtual.getDestaque());
        barraProgresso.setBackground(temaAtual.getFundo());
        barraProgresso.setBorder(BorderFactory.createLineBorder(temaAtual.getBorda()));
        barraProgresso.setPreferredSize(new Dimension(150, 20));
        barraProgresso.setMaximumSize(new Dimension(150, 20));
        barraProgresso.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(barraProgresso);
        painel.add(Box.createVerticalStrut(15));

        painel.add(Box.createVerticalGlue());

        JLabel lblDica = new JLabel("<html><center>\uD83D\uDDB1\uFE0F Esquerdo: revelar<br>\uD83D\uDDB1\uFE0F Direito: bandeira</center></html>");
        lblDica.setFont(FONTE_PEQUENA);
        lblDica.setForeground(temaAtual.getTextoSecundario());
        lblDica.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(lblDica);
        painel.add(Box.createVerticalStrut(10));

        if (painelMusica == null) {
            painelMusica = new PainelMusica();
        }
        painelMusica.setOuvinte(ouvinte);
        painelMusica.aplicarCorTexto(temaAtual.getTextoPrincipal());
        painel.add(painelMusica);

        return painel;
    }

    /**
     * Cria um item de estatística (título + valor) como um único painel,
     * guardando a referência ao label de valor via putClientProperty para
     * que possa ser atualizado depois.
     */
    private JPanel criarItemEstatistica(String titulo, String valorInicial) {
        JPanel painelItem = new JPanel();
        painelItem.setLayout(new BoxLayout(painelItem, BoxLayout.Y_AXIS));
        painelItem.setBackground(temaAtual.getFundoClaro());
        painelItem.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(FONTE_PEQUENA);
        lblTitulo.setForeground(temaAtual.getTextoSecundario());
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblValor = new JLabel(valorInicial);
        lblValor.setFont(FONTE_NUMERO);
        lblValor.setForeground(temaAtual.getTextoPrincipal());
        lblValor.setAlignmentX(Component.CENTER_ALIGNMENT);

        painelItem.add(lblTitulo);
        painelItem.add(lblValor);
        painelItem.putClientProperty("valor", lblValor);

        return painelItem;
    }

    private JPanel criarPainelTabuleiro(int linhas, int colunas) {
        JPanel grade = new JPanel(new GridLayout(linhas, colunas, 2, 2));
        grade.setBackground(temaAtual.getFundo());

        botoes = new JButton[linhas][colunas];
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                JButton botao = criarBotaoCelula(i, j);
                botoes[i][j] = botao;
                grade.add(botao);
            }
        }

        cursorLinha = 0;
        cursorColuna = 0;
        SwingUtilities.invokeLater(() -> botoes[0][0].requestFocusInWindow());

        painelTabuleiroAtual = grade;
        return grade;
    }

    /**
     * Cria o botão de uma célula. O clique direito é detectado em
     * mouseReleased (e não mousePressed): em trackpads, o clique direito
     * simulado por toque com dois dedos nem sempre reporta corretamente
     * qual botão foi pressionado no evento de "pressed" — só fica
     * confiável no evento de "released".
     */
    private JButton criarBotaoCelula(int linha, int coluna) {
        JButton botao = new JButton();
        botao.setPreferredSize(new Dimension(tamanhoCelulaAtual, tamanhoCelulaAtual));
        botao.setFont(FONTE_CELULA);
        botao.setFocusPainted(false); // necessário para enxergar o cursor do teclado (sugestão #26)
        botao.setBackground(temaAtual.getCelulaOculta());
        botao.setForeground(temaAtual.getTextoPrincipal());
        botao.setMargin(new Insets(0, 0, 0, 0));
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(temaAtual.getBordaOculta(), 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

        botao.putClientProperty("revelada", Boolean.FALSE);

        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (Boolean.FALSE.equals(botao.getClientProperty("revelada"))) {
                    botao.setBackground(temaAtual.getCelulaOcultaHover());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (Boolean.FALSE.equals(botao.getClientProperty("revelada"))) {
                    botao.setBackground(temaAtual.getCelulaOculta());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // Sugestão #37: micro-animação de "pressionar" — escurece
                // brevemente a célula ao clicar, antes do redesenho vindo
                // do Controller substituir pelo resultado real do clique.
                if (Boolean.FALSE.equals(botao.getClientProperty("revelada"))) {
                    botao.setBackground(temaAtual.getCelulaOcultaHover().darker());
                }
            }

            @Override
            public void mouseReleased(MouseEvent evento) {
                if (ouvinte == null) {
                    return;
                }
                boolean botaoDireito = SwingUtilities.isRightMouseButton(evento)
                        || evento.getButton() == MouseEvent.BUTTON3;
                if (botaoDireito) {
                    ouvinte.aoMarcarCelula(linha, coluna);
                } else if (SwingUtilities.isLeftMouseButton(evento)) {
                    ouvinte.aoRevelarCelula(linha, coluna);
                }
            }
        });

        configurarAtalhosDeTeclado(botao, linha, coluna);

        return botao;
    }

    /** Navegação completa por teclado (sugestão #26): setas movem, Espaço revela, F marca. */
    private void configurarAtalhosDeTeclado(JButton botao, int linha, int coluna) {
        InputMap entradas = botao.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap acoes = botao.getActionMap();

        entradas.put(KeyStroke.getKeyStroke("UP"), "mover_cima");
        entradas.put(KeyStroke.getKeyStroke("DOWN"), "mover_baixo");
        entradas.put(KeyStroke.getKeyStroke("LEFT"), "mover_esquerda");
        entradas.put(KeyStroke.getKeyStroke("RIGHT"), "mover_direita");
        entradas.put(KeyStroke.getKeyStroke("SPACE"), "revelar_foco");
        entradas.put(KeyStroke.getKeyStroke("F"), "marcar_foco");

        acoes.put("mover_cima", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moverFoco(-1, 0);
            }
        });
        acoes.put("mover_baixo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moverFoco(1, 0);
            }
        });
        acoes.put("mover_esquerda", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moverFoco(0, -1);
            }
        });
        acoes.put("mover_direita", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moverFoco(0, 1);
            }
        });
        acoes.put("revelar_foco", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ouvinte != null) {
                    ouvinte.aoRevelarCelula(cursorLinha, cursorColuna);
                }
            }
        });
        acoes.put("marcar_foco", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ouvinte != null) {
                    ouvinte.aoMarcarCelula(cursorLinha, cursorColuna);
                }
            }
        });
    }

    private void moverFoco(int deltaLinha, int deltaColuna) {
        if (botoes == null || botoes.length == 0) {
            return;
        }
        int linhas = botoes.length;
        int colunas = botoes[0].length;
        cursorLinha = Math.max(0, Math.min(linhas - 1, cursorLinha + deltaLinha));
        cursorColuna = Math.max(0, Math.min(colunas - 1, cursorColuna + deltaColuna));
        botoes[cursorLinha][cursorColuna].requestFocusInWindow();
    }

    /** Zoom do tabuleiro (sugestão #28). */
    private void aumentarZoom() {
        ajustarZoom(4);
    }

    private void diminuirZoom() {
        ajustarZoom(-4);
    }

    private void ajustarZoom(int delta) {
        tamanhoCelulaAtual = Math.max(20, Math.min(64, tamanhoCelulaAtual + delta));
        if (botoes == null) {
            return;
        }
        for (JButton[] linha : botoes) {
            for (JButton botao : linha) {
                botao.setPreferredSize(new Dimension(tamanhoCelulaAtual, tamanhoCelulaAtual));
            }
        }
        if (painelTabuleiroAtual != null) {
            painelTabuleiroAtual.revalidate();
        }
        pack();
    }

    /** Alterna entre janela normal e maximizada (sugestão #47). */
    private void alternarTelaCheia() {
        if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    // ================================================================
    // ATUALIZAÇÕES CHAMADAS PELO CONTROLLER
    // ================================================================

    /** Redesenha uma célula com base no estado atual do tabuleiro. */
    public void atualizarCelula(int linha, int coluna, LeituraTabuleiro leitura) {
        JButton botao = botoes[linha][coluna];
        botao.putClientProperty("revelada", leitura.isRevelada(linha, coluna));

        if (leitura.isMarcada(linha, coluna)) {
            botao.setText(skinBandeiraAtual.getEmoji());
            botao.setForeground(COR_BANDEIRA);
            botao.setBackground(temaAtual.getCelulaOculta());
            botao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COR_BANDEIRA, 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
            return;
        }

        if (leitura.isInterrogada(linha, coluna)) {
            botao.setText("?");
            botao.setForeground(temaAtual.getDestaque());
            botao.setBackground(temaAtual.getCelulaOculta());
            botao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(temaAtual.getDestaque(), 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
            return;
        }

        if (!leitura.isRevelada(linha, coluna)) {
            botao.setText("");
            botao.setBackground(temaAtual.getCelulaOculta());
            botao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(temaAtual.getBordaOculta(), 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
            return;
        }

        if (leitura.isMinada(linha, coluna)) {
            botao.setText(EMOJI_BOMBA);
            botao.setBackground(temaAtual.getMinaFundo());
            botao.setForeground(COR_MINA);
            botao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COR_MINA, 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
        } else {
            // Célula revelada e segura: fundo claro e "afundado",
            // nitidamente diferente do fundo escuro da célula oculta.
            botao.setBackground(temaAtual.getCelulaRevelada());
            botao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(temaAtual.getBordaRevelada(), 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
            int vizinhas = leitura.getMinasVizinhas(linha, coluna);
            if (vizinhas == 0) {
                botao.setText("");
                botao.setForeground(temaAtual.getTextoSobreRevelada());
            } else {
                botao.setText(skinCelulaAtual.obterSimbolo(vizinhas) + sufixoDaltonico(vizinhas));
                botao.setForeground(CORES_NUMEROS[vizinhas]);
            }
        }
    }

    /** Marca extra (sugestão #25) para não depender só da cor do número. */
    private String sufixoDaltonico(int vizinhas) {
        if (!simbolosDaltonicosAtivo || skinCelulaAtual != SkinCelula.NUMEROS) {
            return "";
        }
        if (vizinhas < 1 || vizinhas > SIMBOLOS_DALTONICOS.length) {
            return "";
        }
        return SIMBOLOS_DALTONICOS[vizinhas - 1];
    }

    public void atualizarTempo(String texto) {
        if (lblTempo != null) {
            lblTempo.setText(texto);
        }
    }

    public void atualizarEstatisticas(int minasRestantes, int celulasReveladas, int totalCelulas, int jogadas) {
        if (lblMinasRestantes != null) {
            lblMinasRestantes.setText(String.valueOf(minasRestantes));
        }
        if (lblCelulasReveladas != null) {
            lblCelulasReveladas.setText(celulasReveladas + " / " + totalCelulas);
        }
        if (lblJogadas != null) {
            lblJogadas.setText(String.valueOf(jogadas));
        }

        int progresso = totalCelulas > 0 ? (int) ((celulasReveladas * 100.0) / totalCelulas) : 0;
        if (barraProgresso != null) {
            barraProgresso.setValue(celulasReveladas);
            barraProgresso.setString(progresso + "%");
            if (progresso < 30) {
                barraProgresso.setForeground(new Color(220, 80, 80));
            } else if (progresso < 70) {
                barraProgresso.setForeground(new Color(220, 180, 60));
            } else {
                barraProgresso.setForeground(COR_VITORIA);
            }
        }
    }

    /** Sugestão #23: quantas dicas ainda restam nesta partida. */
    public void atualizarDicasRestantes(int restantes) {
        if (lblDicasRestantes != null) {
            lblDicasRestantes.setText(String.valueOf(restantes));
        }
    }

    /** Sugestão #19: de quem é a vez, no modo cooperativo local. Um valor 0 ou negativo limpa o rótulo. */
    public void atualizarJogadorDaVez(int jogador) {
        if (lblJogadorDaVez != null) {
            lblJogadorDaVez.setText(jogador > 0 ? "Vez do jogador " + jogador : " ");
        }
    }

    /** Sugestão #24: aviso visual quando o número de bandeiras passa do total de minas. */
    public void indicarExcessoDeBandeiras(boolean excedeu) {
        if (lblAvisoBandeiras != null) {
            lblAvisoBandeiras.setText(excedeu ? "Bandeiras além das minas!" : " ");
        }
    }

    public void mostrarDerrota() {
        labelStatus.setText(EMOJI_EXPLOSAO + " Você perdeu!");
        labelStatus.setForeground(COR_MINA);
    }

    public void mostrarVitoria() {
        labelStatus.setText(EMOJI_TROFEU + " Você venceu!");
        labelStatus.setForeground(COR_VITORIA);
    }

    public void piscarFundoDeExplosao(boolean explodindo) {
        getContentPane().setBackground(explodindo ? temaAtual.getMinaFundo() : temaAtual.getFundo());
    }

    public void marcarMinaExplodida(int linha, int coluna) {
        JButton botao = botoes[linha][coluna];
        botao.setText(EMOJI_BOMBA);
        botao.setForeground(COR_MINA);
        botao.setBackground(temaAtual.getMinaFundo());
        botao.setBorder(BorderFactory.createLineBorder(COR_MINA, 1));
    }

    public void destacarCelulaVencedora(int linha, int coluna) {
        botoes[linha][coluna].setBackground(new Color(40, 100, 60));
    }

    /** Sugestão #10: sacode a janela ao pisar numa mina. */
    public void sacudirJanela() {
        Point original = getLocation();
        int[] deslocamentos = {8, -8, 6, -6, 4, -4, 2, -2, 0};
        int[] contagem = {0};
        Timer timer = new Timer(30, null);
        timer.addActionListener(e -> {
            if (contagem[0] >= deslocamentos.length) {
                timer.stop();
                setLocation(original);
                return;
            }
            setLocation(original.x + deslocamentos[contagem[0]], original.y);
            contagem[0]++;
        });
        timer.start();
    }

    /** Sugestão #11: confete na tela de vitória. */
    public void mostrarConfete() {
        PainelConfete confete = new PainelConfete(Math.max(1, getWidth()), Math.max(1, getHeight()));
        confete.exibirSobre(this, 6000);
    }

    /** Sugestão #13: realce do botão de depuração conforme o modo está ligado ou não. */
    public void indicarModoDebug(boolean ativo) {
        if (botaoDebug != null) {
            botaoDebug.setBackground(ativo ? temaAtual.getDestaque() : temaAtual.getFundoClaro());
        }
    }

    public void indicarMinaDebug(int linha, int coluna, boolean ativo) {
        JButton botao = botoes[linha][coluna];
        if (ativo) {
            botao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 140, 0), 3),
                    BorderFactory.createEmptyBorder(1, 1, 1, 1)
            ));
        } else {
            botao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(temaAtual.getBordaOculta(), 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
        }
    }

    /** Mensagem simples para o jogador (confirmações, avisos, erros de persistência). */
    public void mostrarMensagem(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Campo Minado", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Sugestão #50: aviso de conquistas novas desbloqueadas. */
    public void mostrarConquistasDesbloqueadas(List<Conquista> conquistas) {
        StringBuilder texto = new StringBuilder(EMOJI_TROFEU + " Nova conquista desbloqueada!\n\n");
        for (Conquista conquista : conquistas) {
            texto.append("• ").append(conquista.getTitulo()).append(" — ").append(conquista.getDescricao()).append('\n');
        }
        JOptionPane.showMessageDialog(this, texto.toString(), "Conquista desbloqueada", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Sugestão #44: copia o resultado formatado para a área de transferência. */
    public void copiarParaAreaDeTransferencia(String texto) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(texto), null);
    }

    /** Sugestão #36: estado do mini-player de música ambiente. */
    public void atualizarPainelMusica(String tituloFaixa, boolean tocando, boolean ativado, float volume) {
        if (painelMusica != null) {
            painelMusica.atualizarEstado(tituloFaixa, tocando, ativado, volume);
        }
    }

    /** Sugestão #35: ícone do botão de som conforme ligado/desligado. */
    public void atualizarEstadoSom(boolean ativado) {
        if (botaoSom != null) {
            botaoSom.setText(ativado ? "\uD83D\uDD0A" : "\uD83D\uDD07");
        }
    }

    // ================================================================
    // MODO COMPETITIVO (sugestão #43)
    // ================================================================

    /** Tela de transição entre os dois tabuleiros do modo competitivo. */
    public void mostrarTransicaoCompetitiva(int proximoJogador, Runnable aoContinuar) {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(temaAtual.getFundo());
        painel.setBorder(BorderFactory.createEmptyBorder(60, 60, 60, 60));

        JLabel titulo = new JLabel("Vez do Jogador " + proximoJogador);
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(temaAtual.getTextoPrincipal());
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(15));

        JLabel aviso = new JLabel("O tabuleiro tem o mesmo layout do Jogador 1. Boa sorte!");
        aviso.setFont(FONTE_NORMAL);
        aviso.setForeground(temaAtual.getTextoSecundario());
        aviso.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(aviso);
        painel.add(Box.createVerticalStrut(25));

        JButton continuar = criarBotaoSecundario("Começar");
        continuar.addActionListener(e -> aoContinuar.run());
        painel.add(continuar);

        add(painel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    /** Tela de resultado final do modo competitivo. */
    public void mostrarResultadoCompetitivo(int tempoJogador1, boolean vitoriaJogador1,
                                             int tempoJogador2, boolean vitoriaJogador2) {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(temaAtual.getFundo());
        painel.setBorder(BorderFactory.createEmptyBorder(60, 60, 60, 60));

        JLabel titulo = new JLabel("Resultado do modo competitivo");
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(temaAtual.getTextoPrincipal());
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(20));

        String vencedor;
        if (vitoriaJogador1 && !vitoriaJogador2) {
            vencedor = "Jogador 1 venceu!";
        } else if (vitoriaJogador2 && !vitoriaJogador1) {
            vencedor = "Jogador 2 venceu!";
        } else if (vitoriaJogador1) {
            vencedor = tempoJogador1 <= tempoJogador2 ? "Jogador 1 venceu (melhor tempo)!" : "Jogador 2 venceu (melhor tempo)!";
        } else {
            vencedor = "Nenhum dos dois completou o tabuleiro.";
        }

        JLabel resultado1 = new JLabel("Jogador 1: " + (vitoriaJogador1 ? "venceu" : "perdeu") + " em " + formatarTempo(tempoJogador1));
        JLabel resultado2 = new JLabel("Jogador 2: " + (vitoriaJogador2 ? "venceu" : "perdeu") + " em " + formatarTempo(tempoJogador2));
        JLabel resultadoFinal = new JLabel(vencedor);

        resultado1.setFont(FONTE_NORMAL);
        resultado1.setForeground(temaAtual.getTextoPrincipal());
        resultado1.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(resultado1);

        resultado2.setFont(FONTE_NORMAL);
        resultado2.setForeground(temaAtual.getTextoPrincipal());
        resultado2.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(resultado2);

        painel.add(Box.createVerticalStrut(15));
        resultadoFinal.setFont(FONTE_SUBTITULO);
        resultadoFinal.setForeground(temaAtual.getDestaque());
        resultadoFinal.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(resultadoFinal);
        painel.add(Box.createVerticalStrut(25));

        JButton voltar = criarBotaoSecundario("Voltar ao início");
        voltar.addActionListener(e -> {
            if (ouvinte != null) {
                ouvinte.aoPedirNovoJogo();
            }
        });
        painel.add(voltar);

        add(painel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    private String formatarTempo(int segundos) {
        return String.format("%02d:%02d", segundos / 60, segundos % 60);
    }

    // ================================================================
    // ESTATÍSTICAS E PERFIL (sugestões #30, #31, #32, #33, #42, #50)
    // ================================================================

    public void mostrarEstatisticas(List<RegistroPartida> historico, List<RegistroPartida> ranking,
                                     Set<String> conquistasDesbloqueadas) {
        DialogEstatisticas dialogo = new DialogEstatisticas(this, historico, ranking, conquistasDesbloqueadas,
                arquivo -> {
                    if (ouvinte != null) {
                        ouvinte.aoExportarEstatisticasCsv(arquivo);
                    }
                });
        dialogo.setVisible(true);
    }

    public void mostrarSelecaoDePerfil(List<String> perfis, String perfilAtual) {
        DialogPerfil dialogo = new DialogPerfil(this, perfis, perfilAtual,
                nome -> {
                    if (ouvinte != null) {
                        ouvinte.aoSelecionarPerfil(nome);
                    }
                });
        dialogo.setVisible(true);
    }
}
