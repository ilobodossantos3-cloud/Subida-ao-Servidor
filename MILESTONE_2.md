# Milestone 2 — ROOT ACCESS: Subida ao Servidor

**Unidade curricular:** Programação Orientada a Objetos — UAL, 2026–2027
**Data da entrega:** 24 de novembro de 2026
**Versão:** 1.0 (segundo marco)

---

## 1. GitHub link & Commit

| Campo | Valor |
|---|---|
| Repositório | `https://github.com/<organização-ou-utilizador>/<nome-do-repositório>` |
| Branch | `main` |
| Commit desta entrega | `<HASH_DO_COMMIT>` |
| Tag (opcional) | `milestone-2` |

> **Como preencher:** o checkout usado para preparar esta entrega não contém a pasta `.git`, por isso não foi possível ler o hash automaticamente. Na máquina que tem o repositório clonado, executar:
>
> ```bash
> git add .
> git commit -m "Milestone 2: dado unico, avatares, animacao e perguntas das firewalls"
> git rev-parse HEAD
> ```
>
> O valor devolvido por `git rev-parse HEAD` substitui `<HASH_DO_COMMIT>`. O endereço do repositório substitui o placeholder do link. Enquanto estes dois campos não forem preenchidos, a entrega está incompleta.

---

## 2. Current progress (Progresso atual)

### 2.1 Funcionalidades concluídas e a funcionar

| Funcionalidade | Estado | Onde está implementada |
|---|---|---|
| Tabuleiro de 50 casas em serpentina, 5 zonas e 15 efeitos | Concluído | `Board`, `Cell`, `AdvanceCell`, `ReturnCell`, `BoardPanel` |
| 2 a 4 jogadores, nomes e ordem de turnos fixa | Concluído | `Game`, `MainWindow` |
| Escolha de avatar antes do jogo, com pré-visualização e avatares únicos | Concluído | `Avatar`, `Player`, `MainWindow` |
| Um único dado clicável, sem rotação ao passar o rato | Concluído | `DicePanel`, `ControlPanel` |
| Giro do dado com face final legível e pausa antes do turno | Concluído | `DicePanel` |
| Movimento casa a casa a ritmo pausado (340 ms por casa) | Concluído | `BoardPanel` |
| Pausa e som nas casas de avanço e honeypot | Concluído | `BoardPanel`, `SoundEffects` |
| Animação de chegada com ondas, faíscas, salto e fanfarra (~1 s) | Concluído | `BoardPanel`, `SoundEffects` |
| Pergunta de múltipla escolha nas firewalls 11, 25, 35 e 44 | Concluído | `FirewallQuestion`, `QuestionBank`, `Game`, `MainWindow` |
| Regra da firewall: acertar avança 1 casa, errar mantém o peão | Concluído | `Game.answerFirewall` |
| Passagem de vez depois da resposta, certa ou errada | Concluído | `Game.advanceTurn` |
| Ressalto ao ultrapassar a casa 50 | Concluído | `Game.playTurn` |
| Vitória na casa 50, limite de 40 rondas, empate e desempate por posição | Concluído | `Game`, `GameResult` |
| Reinício completo e saída | Concluído | `ControlPanel`, `MainWindow` |
| Sons sintetizados com possibilidade de desligar | Concluído | `SoundEffects`, `ControlPanel` |
| Testes automatizados | Concluído — **48/48 a passar** | `BoardTest`, `GameTest` |
| Documentação de regras, mapa, classes e perguntas | Concluído | `README.md`, `EXPLICACAO.md` |

### 2.2 O que ainda está incompleto ou por finalizar

1. **Ligação GitHub/commit:** o link e o hash continuam em placeholder neste documento.
2. **Validação manual da GUI:** a lógica está coberta por testes, mas falta percorrer os quatro firewalls num jogo real, em Windows e Linux, e registar evidências (screenshots).
3. **UML e mockups:** o diagrama em `docs/uml_classes.png` ainda não inclui `Avatar`, `FirewallQuestion`, `QuestionBank` e `DicePanel`.
4. **Guia visual:** o README descreve a interface, mas ainda não tem capturas do ecrã de configuração, do dado a girar e do diálogo da firewall.
5. **Banco de perguntas:** existe uma pergunta por firewall. O código já suporta várias por casa e roda entre elas, mas ainda não há perguntas extra.
6. **Persistência:** não há gravação de resultados nem histórico de partidas. Não é exigido pelo enunciado, mas está na lista de melhorias.
7. **Empacotamento:** a aplicação corre por Maven ou `javac`; ainda não é entregue um `.jar` autónomo.
8. **Revisão final de estilo:** falta uma leitura completa dos comentários e dos nomes por um revisor do grupo, e preencher os campos `Revisor: [a preencher]`.

### 2.3 Como compilar e executar a versão atual

**Requisitos:** JDK 17 ou superior. Para os testes e para o comando de execução, Maven 3.9 ou superior.

**Com Maven (recomendado), na raiz do projeto:**

```bash
mvn test
```

Compila o modelo, a vista e os testes, e executa os 48 testes JUnit 5.

```bash
mvn compile exec:java
```

Abre a janela Swing. O `pom.xml` já aponta `rootaccess.view.MainWindow` como classe principal.

**Sem Maven, apenas com o JDK:**

```bash
mkdir -p out
javac --release 17 -d out $(find src/main/java -name "*.java")
java -cp out rootaccess.view.MainWindow
```

No Windows (PowerShell):

```powershell
if (-not (Test-Path out)) { New-Item -ItemType Directory out | Out-Null }
javac --release 17 -d out -sourcepath src\main\java src\main\java\rootaccess\view\MainWindow.java
java -cp out rootaccess.view.MainWindow
```

**Fluxo de utilização:**

1. Abre o ecrã **Configuração Inicial**.
2. Escolhe 2, 3 ou 4 jogadores, escreve os nomes e escolhe um avatar diferente para cada um.
3. Confirma. O tabuleiro aparece com os avatares na posição 0.
4. O jogador da vez clica no dado (ou usa Espaço/Enter). O dado gira, mostra a face durante 480 ms e só depois o avatar começa a andar.
5. O avatar percorre as casas uma a uma. Se cair numa firewall, aparece um diálogo com quatro opções (A–D).
6. Responde: **certa** → avança exatamente 1 casa; **errada** → fica na firewall. A vez passa em qualquer dos casos.
7. O jogo termina quando alguém chega à casa 50 ou quando se completam 40 rondas. O vencedor (ou o empate) é mostrado numa mensagem final.

**Verificação feita nesta entrega:**

```text
48 tests found
48 tests successful
0 tests failed
```

---

## 3. Individual contributions (Contribuições individuais)

Resumo fiel da secção **8. Divisão de tarefas** do `README.md`.

| Aluno | Área principal | Contribuições concretas |
|---|---|---|
| **A — Igor Santos (30016495)** | Estrutura e entidades do modelo | `Cell`, `NormalCell`, `AdvanceCell`, `ReturnCell`, `Effect`, `Zone`, `Board` e `Player`; catálogo `Avatar`; validação das casas e dos destinos; testes de movimento básico, casas partilhadas e dos 15 efeitos. |
| **B — Pedro Moroni (30016458)** | Motor de jogo e controlo de turnos | `Game`, `GameResult`, `TurnEvent`, `GameObserver`; ressalto, passagem de vez, vitória, limite de 40 rondas e empate; fluxo de turno em duas fases para as firewalls (`playTurn` e `answerFirewall`); validação de avatares únicos em `Game.start`; testes de integração, ressalto do 50 e fim às 40 rondas. |
| **C — Cleiton Muangala (30016337)** | Interface gráfica: janela e tabuleiro | `MainWindow` e `BoardPanel`; ecrã de configuração com escolha de avatares; desenho do tabuleiro em serpentina; movimento casa a casa e animação de chegada; diálogo da firewall; mockup artístico e screenshots. |
| **D — Gonçalo Veríssimo (n.º `[PREENCHER]`)** | Dados, perguntas das firewalls e painéis de controlo | `Dice`, `RandomDice`, `ManualDice` e `InvalidDiceValueException` (validação do dado); `FirewallQuestion` e `QuestionBank`; `ControlPanel`, `DicePanel` (dado único clicável com giro) e `LogPanel`; `SoundEffects` e opção de desligar os sons; testes dos dados e das perguntas. |

**Trabalho transversal:** as três áreas foram revistas em conjunto. A regra da firewall obrigou a alterar `Board`, `Game`, `TurnEvent`, `MainWindow` e os testes; essa alteração foi discutida e validada pelo grupo antes de ser integrada.

Os ficheiros Java têm no topo o comentário de módulo, aluno, revisor e uso de IA. Os comentários internos usam apenas `//`, como pedido no enunciado.

---

## 4. Reflection and next steps (Reflexão, uso de IA e próximos passos)

### 4.1 Dificuldade técnica real e resolução

**Problema:** a regra inicial dizia que uma firewall fazia recuar o peão (por exemplo, 11 → 2). O novo requisito transformou a firewall num ponto de verificação: o peão tem de **ficar visível na casa**, o jogador responde e só depois o turno continua. A primeira tentativa aplicava o efeito do tabuleiro e só depois abria a pergunta. O resultado era errado: o peão saltava para a casa 2 antes de o jogador responder, a animação mostrava um destino que já não interessava e o turno podia passar antes da resposta.

**Resolução:** o turno foi dividido em duas fases explícitas.

- `Board.applyEffect` deixou de redirecionar firewalls e passou a devolver a própria casa; foi criado `Board.isFirewall`.
- `Game.playTurn` deteta a firewall, guarda `pendingQuestion`, `pendingRoll`, `pendingFrom`, `pendingRaw` e `pendingLanded`, e devolve um `TurnEvent` com `isAwaitingFirewall() == true`.
- `MainWindow` anima a chegada até à firewall e só no callback final abre o diálogo. A resposta chama `Game.answerFirewall(correct)`.
- `answerFirewall` aplica a regra (certa: +1 casa; errada: fica), limpa o estado pendente, avança a vez e emite um segundo `TurnEvent` com `firewallCorrect` preenchido.
- Enquanto há pergunta pendente, `playTurn` recusa novos lançamentos, o que impede cliques duplos no dado.

O mesmo trabalho obrigou a atualizar os testes: a expectativa `11 → 2` passou a `11 → 11` e foram acrescentados testes para resposta certa, resposta errada e cobertura das quatro firewalls.

**Segunda dificuldade:** o teste do limite de 40 rondas simulava 80 turnos. Com firewalls pelo caminho, o jogo podia ficar bloqueado à espera de uma resposta e o teste tornava-se lento e frágil. A solução foi colocar a ronda diretamente na última através de um auxiliar de reflexão (`setRound`) e testar apenas a transição final, mantendo o teste determinístico.

**Terceira dificuldade:** avatares repetidos. A validação foi colocada em dois níveis — o diálogo avisa e volta a pedir a escolha, e `Game.start(names, avatars)` rejeita duplicados com `IllegalArgumentException`. Assim, a regra não depende da GUI.

### 4.2 Vantagens da abordagem

- **Separacão modelo/vista:** o `Game` não conhece Swing. Toda a lógica (movimento, efeitos, firewall, rondas, vitória) é testável sem abrir uma janela.
- **Observer:** o modelo comunica turnos e fim de jogo sem criar dependências diretas para a GUI.
- **Dado determinístico nos testes:** `ManualDice` permite fixar lançamentos; o `DicePanel` usa o mesmo contrato, por isso a GUI não tem uma lógica paralela.
- **Estado explícito:** a firewall pendente é um estado do modelo, não uma variável escondida na vista. Isso evita turnos a avançar duas vezes e cliques durante o diálogo.
- **Regras num só sítio:** a mensagem, a posição final e a passagem de vez são calculadas no `Game`; a GUI apenas desenha e pergunta.
- **Evolução simples:** o `QuestionBank` já aceita várias perguntas por firewall e roda entre elas, sem alterar o motor.
- **Acessibilidade e conforto:** o dado responde a clique, Espaço e Enter; os sons podem ser desligados; o avatar tem nome e glifo.

### 4.3 Papel da IA no desenvolvimento

A base do projeto — tema, regras, mapa do tabuleiro, divisão de tarefas, modelo de classes e lógica de jogo — foi desenvolvida pelo grupo. A IA foi usada como **ferramenta de apoio**, nomeadamente para:

- rever nomes e comentários, garantindo que cada função e cada linha nova tinha uma explicação coerente;
- sugerir testes adicionais para a firewall, avatares e limite de rondas;
- refinar pormenores visuais da animação e do dado;
- organizar e rever a documentação (`README.md`, este milestone e `EXPLICACAO.md`).

A IA não decidiu as regras do jogo nem substituiu a implementação. Todas as alterações foram lidas, compiladas e validadas com os testes do grupo. O registo completo está em `AI_USE.md`.

### 4.4 O que falta concluir antes da entrega final

1. Preencher o link do GitHub e o hash do commit neste documento.
2. Correr `mvn test` e `mvn compile exec:java` na máquina de entrega e confirmar os 48 testes.
3. Fazer um teste manual completo: 2, 3 e 4 jogadores; passar pelas quatro firewalls; responder certo e errado; chegar à casa 50; atingir o limite de 40 rondas.
4. Atualizar o UML (`docs/uml_classes.png`) com `Avatar`, `FirewallQuestion`, `QuestionBank` e `DicePanel`.
5. Acrescentar screenshots ao README: configuração inicial, dado a girar, animação de chegada e diálogo da firewall.
6. Preencher os campos `Revisor: [a preencher]` nos cabeçalhos e fazer a revisão final de comentários.
7. Avaliar a criação de um `.jar` executável e instruções de execução sem Maven.
8. Opcional: acrescentar mais perguntas por firewall, histórico de resultados e preferências persistentes (som e último número de jogadores).

---

**Resumo:** o segundo milestone entrega um jogo jogável de ponta a ponta, com dado único, avatares, movimento animado e firewalls com perguntas. A lógica está separada da interface, os 48 testes passam e o que resta é sobretudo documentação, evidências visuais, UML e a ligação ao repositório.
