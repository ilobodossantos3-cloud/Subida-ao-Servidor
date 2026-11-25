# Milestone 2 — ROOT ACCESS: Subida ao Servidor

**POO — UAL, 2026–2027 · Versão Swing, 2–4 jogadores · Relatório de estado de novembro**

| Campo | Valor |
|---|---|
| Repositório | https://github.com/ilobodossantos3-cloud/Subida-ao-Servidor |
| Tag / commit desta entrega | `milestone-2` · commit `[preencher: git rev-parse milestone-2]` |
| Como correr | `mvn test` (testes) · `mvn compile exec:java` (GUI) · JDK 17+, Maven 3.9+ |

## 1. O que funciona (com evidência)

| Requisito de novembro | Estado | Evidência |
|---|---|---|
| GUI a correr: tabuleiro, peões, jogador atual, registo, turno processado | Feito | [captura](docs/screenshots/05_honeypot_e_firewall.png) |
| **Dois modos de dado**, escolhidos no início: aleatório (`RandomDice`, botão «Lançar dado») e manual (campo «Valor (1-6)» + «Lançar», `ManualDice`) com a mesma lógica de turno | Feito | `DiceModeTest` (18 testes), [configuração](docs/screenshots/01_configuracao_modo_dado.png), [aleatório](docs/screenshots/02_modo_aleatorio_dado_a_girar.png) |
| **Validação:** entrada manual inválida (`0`, `7`, `abc`, vazio, …) recusada com mensagem, sem alterar o estado; opção inexistente na firewall recusada | Feito | `DiceModeTest`, `GameTest`, [captura](docs/screenshots/03_modo_manual_entrada_invalida.png) |
| Efeitos de avanço (8 brechas) e recuo (3 honeypots); firewalls com pergunta, avaliada no modelo | Feito | `BoardTest`, `GameTest`, [captura](docs/screenshots/04_firewall_pergunta.png) |
| Ressalto, vitória exata, limite de 40 rondas, empate, reinício e saída | Feito | `GameTest` |
| Testes iniciais | **70/70 a passar** | `mvn test` → `Tests run: 70, Failures: 0, Errors: 0` (Board 36, Game 16, DiceMode 18) |
| Conceito artístico | v0 feito | [`docs/artwork/conceito_artistico_v0.png`](docs/artwork/conceito_artistico_v0.png) |

Além dos testes JUnit, a GUI foi verificada com um percurso automático (2 partidas, 32 verificações, 0 falhas): três lançamentos aleatórios, quatro entradas inválidas sem mudar o estado, firewall certa, desistência, honeypot e reinício.

## 2. Alterações principais deste marco

- Escolha do modo do dado no ecrã de configuração. Até aqui o valor era sorteado na vista e passado ao `ManualDice` como texto, por isso não havia entrada manual.
- A resposta à firewall passou a ser avaliada pelo modelo (`Game.answerFirewall(int)`, `giveUpFirewall()`), não pela GUI.
- Erro corrigido: depois de responder a uma firewall, o avatar voltava à casa de partida e repetia o percurso.
- `pom.xml` adicionado: os comandos `mvn` do README não funcionavam sem ele.
- README corrigido quanto ao encadeamento (caso 35 → 36), diagrama UML em Mermaid, capturas e conceito artístico.

## 3. O que falta (até 4 de janeiro)

1. Testes: jogos completos com 2 e 4 jogadores, ordem de turnos com 3 e 4, `@DisplayName` (estado/entrada/esperado) nos testes antigos, e tirar a reflexão do teste de rondas.
2. Modelo: a firewall é um `ReturnCell` que não recua e tem destinos que nunca são usados; deve passar a ter classe própria. `Player.moveTo` é público.
3. Perguntas: há uma só por firewall e a explicação no registo revela a resposta aos jogadores seguintes.
4. Cabeçalhos no formato exigido (`/* Module and purpose … AI assistance … */`), campos «Revisor» preenchidos, `AI_USE.md` completo.
5. Mockup polido, UML final, relatório de 4 páginas, contagem de linhas por aluno (produção e testes em separado), capturas em Windows (verificar os emoji).

## 4. Contribuições

| Aluno | Área principal | Contribuições concretas |
|---|---|---|
| **A — Igor Santos (30016495)** | Estrutura e entidades do modelo | `Cell`, `NormalCell`, `AdvanceCell`, `ReturnCell`, `Effect`, `Zone`, `Board` e `Player`; catálogo `Avatar`; validação das casas e dos destinos; testes de movimento básico, casas partilhadas e dos 15 efeitos. |
| **B — Pedro Moroni (30016458)** | Motor de jogo e controlo de turnos | `Game`, `GameResult`, `TurnEvent`, `GameObserver`; ressalto, passagem de vez, vitória, limite de 40 rondas e empate; fluxo de turno em duas fases para as firewalls (`playTurn` e `answerFirewall`); validação de avatares únicos em `Game.start`; testes de integração, ressalto do 50 e fim às 40 rondas. |
| **C — Cleiton Muangala (30016337)** | Interface gráfica: janela e tabuleiro | `MainWindow` e `BoardPanel`; ecrã de configuração com escolha de avatares; desenho do tabuleiro em serpentina; movimento casa a casa e animação de chegada; diálogo da firewall; mockup artístico e screenshots. |
| **D — Gonçalo Veríssimo (n.º `[PREENCHER]`)** | Dados, perguntas das firewalls e painéis de controlo | `Dice`, `RandomDice`, `ManualDice` e `InvalidDiceValueException` (validação do dado); `FirewallQuestion` e `QuestionBank`; `ControlPanel`, `DicePanel` (dado único clicável com giro) e `LogPanel`; `SoundEffects` e opção de desligar os sons; testes dos dados e das perguntas. |

As alterações de 24/11 (secção 2) foram feitas com apoio de IA (Claude Code) e estão registadas em `AI_USE.md`, secção 3. Revistas e integradas por: `[a preencher]`.

## 5. Reflexão individual (escrita por cada aluno)

**Igor Santos** — Estrutura e entidades do modelo
Dificuldade: adaptar o modelo do tabuleiro e das regras de efeito quando as firewalls deixaram de aplicar um recuo imediato para passarem a depender de uma pergunta e de validação em duas fases, garantindo ao mesmo tempo que o catálogo de avatares e as validações de casas continuassem consistentes para o resto do motor de jogo.

Reflexão individual: este segundo marco mostrou-me a importância de manter as entidades do domínio limpas e bem delimitadas, mas também flexíveis o suficiente para absorver mudanças de regra sem quebrar o que já estava testado. Rever e ajustar as classes do modelo com a equipa ajudou a consolidar a base antes de avançar para a reta final.

**Pedro Moroni** — Motor de jogo e controlo de turnos
Dificuldade: coordenar o estado do turno entre o lançamento do dado, a espera pela resposta à firewall e a passagem de vez sem abrir espaço a estados inconsistentes, cliques duplicados ou testes lentos ao simular muitas rondas.

Reflexão individual: separar explicitamente as fases do turno e centralizar a validação das regras no Game tornou o comportamento muito mais previsível e fácil de testar. Aprendi que tornar o estado pendente explícito no modelo evita remendos na interface e facilita bastante a escrita de testes determinísticos.

**Cleiton Muangala** — Interface gráfica: janela e tabuleiro
Dificuldade: sincronizar o desenho do tabuleiro, o movimento casa a casa com ritmo pausado, a animação de chegada e o diálogo da firewall com os eventos emitidos pelo modelo, sem misturar lógica de jogo dentro dos componentes Swing.

Reflexão individual: ver o tabuleiro a ganhar movimento, avatares e feedback visual deu outra vida ao projeto, mas exigiu cuidado para que a vista apenas reagisse ao estado notificado. Esta fase reforçou a importância de manter os componentes gráficos focados na apresentação e na interação.

**Gonçalo Veríssimo** — Dados, perguntas das firewalls, controlo e som
Dificuldade: integrar o dado único clicável e com animação de giro, o suporte a entrada manual/determinística, o banco de perguntas das firewalls e os efeitos sonoros com opção de silêncio, mantendo tudo alinhado com o fluxo do jogo e com a interface.

Reflexão individual: entrar numa fase em que o jogo já tem ritmo e feedback torna cada detalhe de interação mais visível, desde a resposta do dado ao teclado até ao controlo do som. Trabalhar nestes módulos mostrou-me como pequenas peças de usabilidade e suporte fazem diferença na experiência global de quem joga.
## 6. Próximos passos

Dezembro: pontos 1–3 da secção 3, com commits de cada aluno na sua área. Primeira semana de janeiro: relatório, mockup final e contagem de linhas.
