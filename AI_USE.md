# AI_USE.md — Registo do uso de IA

## 1. Princípio

O grupo desenvolveu a base do projeto: tema, regras, mapa do tabuleiro, modelo de classes, lógica de jogo e divisão de tarefas. A IA foi usada como **ferramenta de apoio** para melhorar, refinar e otimizar código e documentação. Nenhuma regra de jogo foi delegada à IA e todas as sugestões foram lidas, compiladas e testadas pelo grupo.

## 2. Onde a IA foi usada

| Área | Tipo de apoio | Validação humana |
|---|---|---|
| Comentários | Tornar os comentários coerentes por função e por linha nova, usando apenas `//` | Revisão manual e compilação |
| Testes | Sugestões de casos para firewall, avatares e limite de rondas | Execução de `mvn test` / JUnit; 48/48 |
| GUI | Pequenos ajustes de layout, cores e animação | Teste manual e smoke test headless |
| Documentação | Estrutura do README, `MILESTONE_2.md`, `EXPLICACAO.md` e listas de melhorias | Leitura e correção pelo grupo |
| Refatoração | Nomes, extração de métodos e remoção de duplicação | Revisão e testes de regressão |

> **Por completar pelo grupo:** o enunciado pede, para cada uso significativo, a ferramenta, o objetivo, o pedido (ou um resumo), o que foi mantido, o que foi alterado e como foi verificado. Esta tabela ainda não diz que ferramenta(s) foram usadas nem resume os pedidos. A secção 3 segue esse formato.

## 3. Registo detalhado: alterações de 24/11/2026 (Claude Code)

| Campo | Registo |
|---|---|
| Ferramenta | Claude Code (Anthropic), modelo Claude Opus 5.5, a trabalhar diretamente no repositório local |
| Objetivo | Rever o milestone 2 face ao enunciado, completar o que faltava para o marco de novembro e corrigir os erros encontrados, respeitando as regras do projeto |
| Pedido (resumo) | «Analisa o milestone 2 e o enunciado, vê o que falta e o que está errado.» Depois: «Para o milestone 2, corrige ou faz o que falta; se encontrares problemas durante a execução, corrige-os de acordo com os objetivos e regras do projeto.» |
| Output mantido | Ver a lista abaixo |
| Alterações humanas | `[a preencher pelo grupo depois de rever cada ficheiro]` |
| Verificação | `mvn test`: 70 testes, 0 falhas. Compilação `javac -Xlint:all` sem avisos (exceto `serial`). Percurso automático da GUI real: 32 verificações, 0 falhas. Leitura visual das capturas e do conceito artístico |

**Código mantido (todos os ficheiros têm a linha «Uso de IA» atualizada):**

- `Dice`, `RandomDice`, `ManualDice`: `getModeName()` (polimorfismo: o registo mostra «dado aleatório» ou «dado manual»); `RandomDice(Random)` para testes com semente (sobrecarga de construtor).
- `InvalidDiceValueException`: mensagem legível para a entrada vazia.
- `Game`: `answerFirewall(int)` avalia a opção no modelo (antes era a GUI que comparava com a resposta certa); `giveUpFirewall()`; opção inexistente recusada sem mudar o estado; modo do dado nas mensagens; o campo `dice` passou a `final`.
- `DicePanel`: o clique só pede o lançamento; `showResult(...)` mostra a face que o modelo decidiu (antes o valor era sorteado na vista).
- `ControlPanel`: controlos dos dois modos (botão «Lançar dado» ou campo «Valor (1-6)» com «Lançar»), mensagem de erro sem mudar o estado, desacoplado do `MainWindow`, painel mais compacto para o registo ter espaço.
- `MainWindow`: escolha do modo do dado na configuração; sequência «mostrar dado → mover avatar»; diálogo da firewall com «Responder» e «Desistir»; cabeçalho «Jogador/Nome» que estava sobreposto; lista genérica de combos (sem *raw type*).
- `BoardPanel`: corrigido o erro em que, depois da resposta à firewall, o avatar voltava à casa de partida e repetia o percurso; peão retido na casa de partida enquanto o dado é revelado; legenda e avatares sem sobreposição.
- Testes: novo `DiceModeTest` (18); `GameTest` adaptado a `answerFirewall(int)`, com 4 testes novos.
- `pom.xml` (JUnit 5.10.2, Java 17, `exec:java`, *jar* executável) e `target/` no `.gitignore`.
- Documentação: `README.md` (modos do dado, encadeamento 35 → 36, UML em Mermaid, testes, compilação com `-encoding UTF-8`), `MILESTONE_2.md` reescrito como relatório de estado, este registo.

**Conceito artístico v0** (`docs/artwork/conceito_artistico_v0.png`): não foi usado um gerador de imagens. O Claude Code escreveu um programa Java2D (`docs/artwork/gerador/ConceptArt.java`, fora de `src/` e fora da contagem de linhas) que desenha a imagem. Descrição principal usada: *«o tabuleiro de 50 casas como uma torre de servidores com 5 pisos (as zonas Internet → Núcleo), casas em serpentina, brechas como feixes verdes para o destino real, honeypots como feixes laranja tracejados, firewalls como paredes vermelhas com ?, o Root como servidor iluminado no topo, peões Ana/Rui/Eva nas casas (23, 26, 9), um dado com a face 3 e um registo de eventos coerente com as regras»*.

**Capturas** (`docs/screenshots/`): tiradas da aplicação real durante o percurso automático de verificação, que não foi incluído no repositório.

Cada aluno deve rever as alterações da sua área e conseguir explicá-las na apresentação de janeiro.

## 4. Onde a IA não foi usada como autoria

- A definição das regras (movimento, ressalto, efeitos, firewall, limite de rondas) foi decidida pelo grupo.
- A divisão de tarefas da secção 8 do README foi definida pelo grupo.
- A implementação base de `Board`, `Game`, `Player`, `Dice` e da GUI foi escrita e integrada pelo grupo.
- As perguntas das firewalls foram produzidas para o projeto e revistas pelo grupo; não foram copiadas de fontes externas sem verificação.

## 5. Verificação

Antes de cada integração:

1. O código foi compilado com `javac --release 17` (agora com `-encoding UTF-8`, ou com `mvn test`).
2. Os testes JUnit foram executados (48 testes, 0 falhas, antes das alterações de 24/11; 70 testes, 0 falhas, depois delas).
3. A GUI foi verificada em modo headless para confirmar que o desenho não lança exceções.
4. As alterações de regras foram refletidas no README e nos testes.

## 6. Limitações e responsabilidade

A IA pode produzir sugestões incorretas ou desatualizadas. Por isso, o grupo assume a responsabilidade final pelo código entregue, pela exatidão das regras e pela documentação. Qualquer nova utilização de IA deve ser registada nesta tabela e validada antes da entrega.
