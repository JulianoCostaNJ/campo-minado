# Campo Minado

Versão em Java do jogo Campo Minado, com interface gráfica Swing, arquitetura MVC
e um conjunto grande de funcionalidades extras (temas, skins, som e música, modos de
jogo alternativos, perfis, estatísticas, conquistas, replay, modo cooperativo e
competitivo, e salvar/retomar uma partida).

## Estrutura do projeto

```
src/
  main/         Pontos de entrada (GUI e console).
  model/        Domínio do jogo: Tabuleiro, Celula, regras, marcação, modos de jogadores.
  view/         Interface Swing: tela principal, temas, skins, componentes/diálogos.
  controller/   Orquestra Model + View; ações do jogador; ligação com os serviços abaixo.
  service/      Regras de apoio que não são puramente Model nem View: dica, conquistas, replay.
  audio/        Efeitos sonoros e música ambiente (interfaces + implementação com javax.sound).
  persistence/  Modelos de dados salvos em disco (partida salva, histórico, config, perfil).
  repository/   Camada de persistência (interfaces + implementação em arquivo).
  test/         Testes unitários (JUnit 5).
```

Cada uma dessas pastas corresponde a um pacote Java (`model`, `view`, `controller`,
`service`, `audio`, `persistence`, `repository`, `main`). `view` tem três
subpacotes: `view.tema` (temas visuais), `view.skin` (skins de bandeira/célula) e
`view.componentes` (diálogos e widgets reutilizáveis).

## Funcionalidades

Além do jogo clássico (dificuldades prontas, cronômetro, tema visual, tutorial),
este projeto implementa:

**Visual:** 9 temas plugáveis, incluindo terminal retrô, cyberpunk neon, Halloween,
oceano/tropical e alto contraste; detecção do tema do sistema; skins de bandeira e
de célula; símbolos extras para daltonismo; zoom; tela cheia; tabuleiro responsivo;
micro-animações de hover/clique; tremor de tela e confete.

**Regras de jogo:** modo sem cascata; modo relâmpago; modo 3 vidas; tabuleiro
toroidal; marcação de três estados (bandeira/interrogação/nenhuma); dificuldade
personalizada; aviso de excesso de bandeiras; desafio diário com semente fixa.

**Progressão:** dica limitada por partida; modo de depuração (minas visíveis);
melhor tempo, histórico, ranking e exportação CSV; perfis de jogador; conquistas;
replay da última partida; **salvar e retomar uma partida em andamento**.

**Multiplayer local:** modo cooperativo (dois jogadores revezam no mesmo
tabuleiro) e modo competitivo (dois jogadores, tabuleiros idênticos, por tempo).

**Áudio:** efeitos sonoros (clique, bandeira, explosão, vitória) e música ambiente
com playlist, volume e faixas adicionadas apenas copiando arquivos `.wav` — veja
"Áudio" abaixo.

## Compilação

A partir da pasta do projeto (todos os pacotes precisam ser compilados juntos):

```powershell
javac -d out src\main\*.java src\model\*.java src\view\*.java src\view\tema\*.java src\view\skin\*.java src\view\componentes\*.java src\controller\*.java src\service\*.java src\audio\*.java src\persistence\*.java src\repository\*.java
```

No Linux/macOS, o mesmo comando com barras normais:

```bash
javac -d out src/main/*.java src/model/*.java src/view/*.java src/view/tema/*.java src/view/skin/*.java src/view/componentes/*.java src/controller/*.java src/service/*.java src/audio/*.java src/persistence/*.java src/repository/*.java
```

## Execução

```powershell
java -cp out main.JogoCampoMinadoGUI
```

Versão em console (sem as funcionalidades de View/Controller, só o Model):

```powershell
java -cp out main.JogoCampoMinado
```

## Testes

`src/test/CampoMinadoTest.java` usa JUnit 5 (Jupiter). Compile e execute com o
runner do seu ambiente (IDE, Maven/Gradle, ou o `junit-platform-console-standalone`),
incluindo `src/model` no classpath.

## Dados salvos

Partida salva, histórico de partidas, configurações e perfis ficam em
`~/.campominado/` (pasta oculta na home do usuário), como arquivos serializados.
Apagar essa pasta reseta tudo para o estado inicial.

## Áudio

O projeto não veio com arquivos de áudio prontos. Para ativar:

- **Efeitos sonoros:** coloque `clique.wav`, `bandeira.wav`, `explosao.wav` e
  `vitoria.wav` em `recursos/sons/` (relativa a onde o jogo é executado).
- **Música ambiente:** coloque qualquer `.wav` em `recursos/musicas/` — cada
  arquivo vira uma faixa automaticamente, sem precisar mexer em nenhuma classe.

Sem esses arquivos, o jogo funciona normalmente; os efeitos e a música
simplesmente não tocam (com um aviso no console, não um erro).

## Observações

- A interface gráfica usa Swing e o Look and Feel cross-platform, para respeitar
  as cores definidas independentemente do sistema operacional.
- A janela agora é redimensionável (tela cheia e tabuleiro responsivo).
- Antes de executar, certifique-se de compilar todos os pacotes listados acima.
