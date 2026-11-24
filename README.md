# ROOT ACCESS: Subida ao Servidor

Jogo de tabuleiro em Java (Swing) para 2 a 4 jogadores, com **50 casas e 15 efeitos**.
Projeto de Programação Orientada a Objetos, UAL, 2026–2027.

---

## 1. Tema

Numa noite qualquer, a rede da *NexaCorp* tem um servidor central, o **Root**, onde está guardado tudo o que a empresa tem. Dois a quatro *pentesters* competem num desafio de segurança ofensiva autorizado: o primeiro a escalar da internet pública até ao Root ganha o título de **Root Master**.

Cada jogador é um cursor a piscar num terminal. Tudo é simulado e o dado decide o que a rede deixa passar.

A mecânica de base inspira-se no jogo clássico *Cobras e Escadas*: tabuleiro numerado, dado de 1 a 6, casas que fazem avançar e casas que fazem recuar. O tema, as zonas, o fim do jogo e a representação em três níveis são do nosso projeto.

## 2. Regras

| Tema | Regra |
|---|---|
| Jogadores | 2 a 4, escolhidos no início, com nome para cada um |
| Avatares | Cada jogador escolhe um avatar temático diferente dos restantes antes de começar |
| Tabuleiro | 50 casas numeradas de 1 (Internet) a 50 (Root), em serpentina, com **15 casas de efeito** |
| Início | Todos os peões começam na posição 0, fora do tabuleiro |
| Ordem dos turnos | Fixa: jogador 1, 2, 3, 4 e volta ao 1 |
| Turno | O jogador atual **clica no dado único** (1 a 6) e o peão avança esse número de casas (ver o protocolo na secção 3) |
| Dado | Existe **um único dado interativo**. Não roda ao passar o rato: só começa a girar com um clique (ou Espaço/Enter), revela a face e só depois o avatar arranca |
| Dado inválido | O dado visual só produz valores de 1 a 6; o modelo continua a rejeitar qualquer valor fora desse intervalo |
| Casas partilhadas | Vários peões podem estar na mesma casa |
| Casas de avanço | Ao terminar o movimento numa casa de avanço (8 casas), o peão passa para a casa de destino |
| Casas de recuo | Os **honeypots** (3 casas) recuam para o destino indicado. As firewalls já não recuam automaticamente: passam a ser pontos de verificação |
| Firewall | Ao terminar numa firewall (casas 11, 25, 35 e 44), abre-se uma **pergunta de múltipla escolha**. Acertar avança exatamente 1 casa; errar mantém o peão na firewall. Em ambos os casos a vez passa ao jogador seguinte |
| Encadeamento | Cada efeito aplica-se **uma só vez** por movimento. O avanço de 1 casa concedido pela firewall não aplica o efeito da casa de destino, para não criar cadeias |
| Chegada ao Root | É preciso chegar **exatamente** à casa 50 |
| Ultrapassagem | Se o lançamento passar da casa 50, o peão ressalta: posição final = 50 − excedente (da casa 48 com um 5 passa-se 3, e fica na casa 47) |
| Animação | O avatar avança casa a casa a um ritmo pausado e, ao chegar, executa uma animação de chegada com ondas, faíscas e salto |
| Vitória | Ganha o primeiro jogador a chegar à casa 50 |
| Fim sem jogo infinito | Limite de **40 rondas** (uma ronda = todos os jogadores jogam uma vez). Se ninguém chegou ao Root, ganha quem estiver mais à frente. Se houver jogadores empatados na frente, o resultado é empate entre eles |
| Sair e reiniciar | Botões disponíveis a qualquer momento. Reiniciar volta ao ecrã de configuração |

**Nota sobre o ressalto:** o excedente é no máximo 5 (da casa 49 com um 6), por isso uma ultrapassagem nunca deixa o peão abaixo da casa 45. A casa 44 é a última casa de efeito antes do Root, logo o ressalto nunca cai numa casa de efeito.

## 3. Protocolo de movimento

Um turno processa-se sempre pela mesma sequência de passos, em `Game.playTurn()`. O modelo não depende da GUI: a interface só chama este método e desenha o resultado.

| # | Passo | O que acontece |
|---|---|---|
| 1 | Verificar o estado | Se o jogo já terminou (`result != null`), o turno é recusado. Caso contrário, o jogador atual é `players[current]`. |
| 2 | Obter o dado | O `DicePanel` entrega a face ao `ManualDice`, que valida 1–6. Um valor inválido lança `InvalidDiceValueException`: o estado **não muda**. O `RandomDice` continua disponível para testes e para uso programático do modelo |
| 3 | Somar | `raw = posição + dado`. A posição inicial é 0, por isso o primeiro lançamento leva o peão à casa 1 a 6. |
| 4 | Ressalto | Se `raw > 50`: `posição = 100 − raw` (equivale a 50 − excedente). O ressalto nunca deixa o peão abaixo da casa 45, logo nunca cai numa casa de efeito. Caso contrário, `posição = raw`. |
| 5 | Aplicar o efeito | Se a casa de chegada for de avanço ou de recuo, o peão passa **uma só vez** para o destino. Nenhum destino é casa de efeito, por isso não há encadeamento. |
| 6 | Guardar e verificar vitória | O peão fica na posição final. Se for a casa 50, o jogador é o Root Master e o jogo termina. |
| 7 | Passar a vez | `current = (current + 1) % n`. Quando a vez volta ao jogador 1, termina uma ronda; ao fim de 40 rondas sem vencedor, ganha quem estiver mais à frente (empate se houver vários na frente). |
| 8 | Notificar | O `Game` devolve um `TurnEvent` (jogador, dado, casa de partida, casa de chegada, casa final, mensagem) e avisa os `GameObserver`. A GUI só desenha e escreve no registo. |

Em pseudocódigo (Java):

```java
TurnEvent playTurn() throws InvalidDiceValueException {
    if (result != null) throw new IllegalStateException("jogo terminado");
    if (pendingQuestion != null) throw new IllegalStateException("responde à firewall");
    Player p = players.get(current);

    int d = dice.roll();                                  // 1..6; inválido => exceção, nada muda
    int from = p.getPosition();
    int raw = from + d;
    int landed = (raw <= Board.SIZE) ? raw : bounce(raw); // bounce(raw) = 2 * Board.SIZE - raw

    if (board.isFirewall(landed)) {                       // fase 1: fica à espera da resposta
        p.moveTo(landed);
        pendingQuestion = questions.nextFor(landed);
        TurnEvent ev = new TurnEvent(p, d, from, raw, landed, landed, pedido);
        notifyObservers(ev);
        return ev;
    }

    int pos = board.applyEffect(landed);                  // avanço/recuo, uma só vez
    p.moveTo(pos);
    terminarTurno(p, d, raw, landed, pos);                // vencedor ou advanceTurn()
    TurnEvent ev = new TurnEvent(p, d, from, raw, landed, pos, mensagem);
    notifyObservers(ev);
    return ev;
}

TurnEvent answerFirewall(boolean correct) {               // fase 2: chamada pela GUI
    Player p = players.get(current);
    int landed = pendingLanded;
    int pos = correct ? Math.min(Board.SIZE, landed + 1) : landed;
    p.moveTo(pos);
    TurnEvent ev = new TurnEvent(p, pendingRoll, pendingFrom, pendingRaw,
            landed, pos, mensagem, null, correct);
    clearPendingFirewall();
    terminarTurno(...);                                   // a vez passa em ambos os casos
    notifyObservers(ev);
    return ev;
}
```

Exemplos (todos verificados por simulação):

| Caso | Casa de partida | Dado | Cálculo | Casa final |
|---|---|---|---|---|
| Movimento normal | 0 | 3 | 0 + 3 = 3 (casa normal) | 3 |
| Brecha (avanço) | 1 | 3 | 1 + 3 = 4 → casa 4 é brecha → 13 | 13 |
| Firewall (pergunta) | 9 | 2 | 9 + 2 = 11 → casa 11 é firewall → abre pergunta. Certa: 12. Errada: 11 | 12 ou 11 |
| Honeypot (recuo) | 14 | 4 | 14 + 4 = 18 → casa 18 é honeypot → 6 | 6 |
| Ressalto | 48 | 5 | 48 + 5 = 53 > 50 → excedente 3 → 50 − 3 = 47 | 47 |
| Ressalto máximo | 49 | 6 | 49 + 6 = 55 → excedente 5 → 45 | 45 |
| Chegada exata | 47 | 3 | 47 + 3 = 50 → Root, vence | 50 |
| Entrada inválida | 0 | `7` (manual) | rejeitado, estado inalterado, o mesmo jogador repete | (mantém-se) |

## 4. Mapa do tabuleiro

Serpentina de 5 linhas × 10 colunas. A casa 1 está em baixo à esquerda e a 50 em cima à direita. Cada linha é uma zona da rede.

```
 Núcleo       | [41 ^48]    42       43        [44 ?]    45       46       47       48       49    [50 ROOT]
 Datacenter   | [40 v29]    39       38       37    [36 ^46]    [35 ?]    34       33    [32 ^43]    31   
 Rede Interna |    21    [22 ^34]    23       24        [25 ?]    26       27    [28 ^39]    29    [30 v19]
 DMZ          |    20       19    [18 v6]     17    [16 ^27]    15       14       13       12        [11 ?] 
 Internet     |   [1]       2        3     [4 ^13]     5        6        7     [8 ^21]     9        10   
```

Legenda: `^` avanço, `v` recuo, `?` firewall com pergunta. O número depois do símbolo é a casa de destino.

### Casas com efeito (15)

| Casa | Nome | Tipo | Destino | Zona |
|---|---|---|---|---|
| 4 | Exploit encontrado | Brecha / exploit (avanço) | 13 | Internet |
| 8 | Credenciais fracas | Brecha / exploit (avanço) | 21 | Internet |
| 11 | Firewall perimetral | Firewall (pergunta) | resposta certa: 12 | DMZ |
| 16 | Vulnerabilidade zero-day | Brecha / exploit (avanço) | 27 | DMZ |
| 18 | Honeypot da DMZ | Honeypot (recuo) | 6 | DMZ |
| 22 | Privilege escalation | Brecha / exploit (avanço) | 34 | Rede Interna |
| 25 | Firewall interna | Firewall (pergunta) | resposta certa: 26 | Rede Interna |
| 28 | Pass-the-hash | Brecha / exploit (avanço) | 39 | Rede Interna |
| 30 | Honeypot interno | Honeypot (recuo) | 19 | Rede Interna |
| 32 | SQL injection | Brecha / exploit (avanço) | 43 | Datacenter |
| 35 | Firewall de aplicação | Firewall (pergunta) | resposta certa: 36 | Datacenter |
| 36 | Kerberoasting | Brecha / exploit (avanço) | 46 | Datacenter |
| 40 | Honeypot do datacenter | Honeypot (recuo) | 29 | Datacenter |
| 41 | Golden ticket | Brecha / exploit (avanço) | 48 | Núcleo |
| 44 | Firewall do núcleo | Firewall (pergunta) | resposta certa: 45 | Núcleo |

Total: 8 brechas que avançam, 4 firewalls que pedem uma pergunta e 3 honeypots que recuam. Todas as outras casas são normais. A casa 1 (Internet) é a primeira casa e a casa 50 (Root) é a casa final.

### As cinco zonas

| Casas | Zona | Cor |
|---|---|---|
| 1–10 | Internet | azul-frio |
| 11–20 | DMZ | verde |
| 21–30 | Rede Interna | âmbar |
| 31–40 | Datacenter | laranja |
| 41–50 | Núcleo | vermelho |

### Nomes das casas (cenário, sem efeito nas regras)

| Zona | Casas e nomes |
|---|---|
| Internet | 1 Internet, 2 Router doméstico, 3 Wi-Fi do café, 4 **Exploit encontrado**, 5 Fórum anónimo, 6 Servidor proxy, 7 DNS público, 8 **Credenciais fracas**, 9 Rede Tor, 10 Gateway da NexaCorp |
| DMZ | 11 **Firewall perimetral**, 12 Servidor web, 13 Formulário de login, 14 API exposta, 15 Servidor de email, 16 **Vulnerabilidade zero-day**, 17 Balanceador de carga, 18 **Honeypot da DMZ**, 19 Servidor FTP, 20 VPN de acesso remoto |
| Rede Interna | 21 Portátil do estagiário, 22 **Privilege escalation**, 23 Impressora de rede, 24 Partilha de ficheiros, 25 **Firewall interna**, 26 Intranet, 27 Servidor de impressão, 28 **Pass-the-hash**, 29 Switch interno, 30 **Honeypot interno** |
| Datacenter | 31 Base de dados, 32 **SQL injection**, 33 Cluster de backups, 34 Servidor de logs, 35 **Firewall de aplicação**, 36 **Kerberoasting**, 37 Hipervisor, 38 Servidor de aplicações, 39 Rack de armazenamento, 40 **Honeypot do datacenter** |
| Núcleo | 41 **Golden ticket**, 42 Cofre de chaves, 43 Controlador de domínio, 44 **Firewall do núcleo**, 45 Consola de administração, 46 Servidor de certificados, 47 Servidor de auditoria, 48 Gestor de segredos, 49 Porta do Root, 50 Root |

## 5. Os três níveis de representação

O mesmo jogo é representado em três níveis. A tabela liga-os:

| Conceito de jogo | GUI (Swing) | Modelo (Java) | Mockup |
|---|---|---|---|
| Peão do jogador 1 | Glifo do avatar dentro de um círculo verde na casa 23 | `players.get(0).getPosition() == 23` e `getAvatar() == GHOST` | Avatar temático a piscar |
| Estado do jogo | Painel com avatar e posição de cada jogador | `(23, 26, 9)`: posições dos jogadores 1, 2 e 3 | Ecrã de terminal com a rede |
| Casa de avanço (casa 4) | Casa a verde com chave e seta | `AdvanceCell(4, 13)` | Ícone de chave (brecha) |
| Firewall (casa 11) | Casa a vermelho com chama e `?` | `ReturnCell(11, Kind.FIREWALL)` + `QuestionBank.nextFor(11)` | Ícone de chama com pergunta |
| Casa de recuo (casa 18) | Casa a laranja com pote | `ReturnCell(18, 6)` | Ícone de pote (honeypot) |
| Dado | Um único cubo clicável com pips | `Dice` (`ManualDice` alimentado pelo `DicePanel`) | Dado grande com a face sorteada |
| Turno atual | Avatar e nome destacados no painel | `game.getCurrentPlayer()` | Painel lateral |
| Fim do jogo | Mensagem do vencedor com avatar | `GameResult` | Ecrã final com o Root Master |

Exemplo do modelo com 4 jogadores: `(23, 26, 9, 0)` significa que os jogadores 1, 2 e 3 estão nas casas 23, 26 e 9, e o jogador 4 ainda não saiu da posição 0. Um peão só fica parado numa casa de efeito quando essa casa é uma firewall e a pergunta ainda está aberta; nos restantes efeitos é logo redirecionado.

## 6. Desenho das classes (versão inicial, a rever)

Diagrama completo em [`docs/uml_classes.png`](docs/uml_classes.png).

**Modelo (sem qualquer dependência da GUI):**

| Classe | Função |
|---|---|
| `Cell` (abstrata) | Casa do tabuleiro, com número, nome e zona |
| `NormalCell` | Casa sem efeito |
| `Effect` (interface) | Contrato dos efeitos, com o destino da casa |
| `AdvanceCell` / `ReturnCell` | Casas que implementam `Effect` e redirecionam o peão (`ReturnCell` serve firewalls e honeypots) |
| `Zone` (enum) | As cinco zonas da rede |
| `Board` | `List<Cell>` com as 50 casas e `applyEffect(int)` |
| `Player` | Nome, cor, posição e avatar escolhido |
| `Avatar` (enum) | Catálogo fechado de oito avatares temáticos (glifo e nome) |
| `Dice` (interface) | Contrato de um dado |
| `RandomDice` / `ManualDice` | Os dois modos de dado (o segundo serve a GUI) |
| `InvalidDiceValueException` | Valor de dado inválido (modo manual) |
| `Game` | `List<Player>`, turno atual, rondas, ressalto, firewall em duas fases, deteção do vencedor, reinício |
| `FirewallQuestion` | Pergunta imutável: enunciado, opções, índice correto e explicação |
| `QuestionBank` | Perguntas associadas a cada firewall e rotação entre elas |
| `TurnEvent` | Resumo de um turno, incluindo pergunta pendente e resultado da resposta |
| `GameResult` | Resultado final (vencedor, empate ou limite de rondas) |
| `GameObserver` (interface) | Liga o modelo à GUI (padrão Observer) |

Conceitos de POO que vamos usar: encapsulamento, classes colaborantes, herança com *overriding* (`Cell`), interfaces e polimorfismo (`Dice`, `Effect`), coleções genéricas (`List<Player>`, `List<Cell>`) e um *overload* justificado (`Game.start(int)`, `Game.start(List<String>)` e `Game.start(List<String>, List<Avatar>)`).

**Interface gráfica (só desenho, separada do modelo):** `MainWindow`, `BoardPanel`, `ControlPanel`, `DicePanel`, `LogPanel` e `SoundEffects`. O `DicePanel` é o único dado visível: gira ao clique e entrega a face ao `ControlPanel`, que a injeta num `ManualDice`. O `BoardPanel` percorre as casas da jogada com passos de 340 ms e termina com uma animação de chegada. O diálogo da firewall é aberto pelo `MainWindow` quando o `Game` devolve um evento pendente. Sons sintetizados indicam cliques, passos, efeitos, chegada e respostas certas ou erradas; podem ser desligados no painel de controlos.

### Estrutura de ficheiros (projeto IntelliJ)

```
Subida-ao-Servidor-main/
├── pom.xml
├── README.md
└── src/
    ├── main/java/rootaccess/
    │   ├── model/
    │   │   ├── Cell.java            (abstrata)
    │   │   ├── NormalCell.java
    │   │   ├── Effect.java          (interface)
    │   │   ├── AdvanceCell.java
    │   │   ├── ReturnCell.java
    │   │   ├── Zone.java            (enum)
    │   │   ├── Board.java
    │   │   ├── Player.java
    │   │   ├── Avatar.java          (enum)
    │   │   ├── Dice.java            (interface)
    │   │   ├── RandomDice.java
    │   │   ├── ManualDice.java
    │   │   ├── InvalidDiceValueException.java
    │   │   ├── FirewallQuestion.java
    │   │   ├── QuestionBank.java
    │   │   ├── Game.java
    │   │   ├── TurnEvent.java
    │   │   ├── GameResult.java
    │   │   └── GameObserver.java    (interface)
    │   └── view/
    │       ├── MainWindow.java      (main)
    │       ├── BoardPanel.java
    │       ├── ControlPanel.java
    │       ├── DicePanel.java
    │       ├── LogPanel.java
    │       └── SoundEffects.java
    └── test/java/rootaccess/
        ├── BoardTest.java
        └── GameTest.java
```


## 6.1 Esboço da GUI

```
+-----------------------------------------------------------------------------+
|  ROOT ACCESS: Subida ao Servidor                                            |
+---------------------------------------------------+-------------------------+
|  41 42 43 [44] 45 46 47 48 49 [50]                |  Turno: 👻 Ana (verde)  |
|  [40] 39 38 37 [36] [35] 34 33 [32] 31            |  Ronda 3/40             |
|  21 [22] 23 24 [25] 26 27 [28] 29 [30]            |  +-------------------+  |
|  20 19 [18] 17 [16] 15 14 13 12 [11]              |  |     DADO          |  |
|  [1] 2 3 [4] 5 6 7 [8] 9 10                       |  |      ⚄            |  |
|                                                   |  |   clica para      |  |
|                                                   |  |     lançar        |  |
|                                                   |  +-------------------+  |
|                                                   |  [ Reiniciar ] [ Sair ] |
|                                                   |  Registo de eventos:    |
|                                                   |  > 👻 Ana lançou 3      |
|                                                   |  > Firewall 11: ...     |
+---------------------------------------------------+-------------------------+
```

O ecrã de configuração inicial pede o número de jogadores, o nome e um avatar diferente para cada um. As firewalls abrem um diálogo modal com quatro opções (A–D) e a regra visível: acertar avança 1 casa, errar mantém o peão.

## 7. Testes previstos

Cada verificação define estado inicial, entrada e resultado esperado, com lançamentos predeterminados e relatório de sucesso ou falha.

- Modelo de dado: `ManualDice` e `RandomDice` com sequência fixa; a GUI usa um único dado clicável que alimenta o `ManualDice`
- Entrada inválida (0, 7, texto, vazio) sem alterar o estado
- Movimento normal
- Cada um dos 15 efeitos: as 11 casas que redirecionam (4, 8, 16, 18, 22, 28, 30, 32, 36, 40 e 41) e as 4 firewalls que mantêm o peão até à resposta (11, 25, 35 e 44)
- Firewall: pergunta aberta, bloqueio de um novo lançamento, resposta certa (avança 1 casa), resposta errada (fica) e passagem de vez nos dois casos
- Banco de perguntas: as quatro firewalls têm pergunta válida e índice correto dentro das opções
- Limites: ressalto a partir da casa 50 (por exemplo 48 + 5 → 47 e 49 + 6 → 45), chegada exata
- Casas partilhadas
- Fim por limite de rondas e empate
- Jogos com 2, 3 e 4 jogadores
- Avatares: associação ao jogador e rejeição de duplicados
- Reinício e um jogo completo

Estado atual: **48 testes JUnit, todos a passar**.

### Perguntas das firewalls

| Firewall | Pergunta | Opções (correta em **negrito**) |
|---|---|---|
| 11 — Firewall perimetral | Qual é a função principal de uma DMZ numa rede empresarial? | A) Guardar as cópias de segurança da empresa. **B) Isolar os serviços expostos à Internet do resto da rede interna.** C) Aumentar a velocidade das ligações internas. D) Substituir por completo o firewall perimetral. |
| 25 — Firewall interna | Que técnica permite autenticar num servidor com o hash NTLM de outra conta, sem descobrir a palavra-passe? | **A) Pass-the-hash.** B) SQL injection. C) ARP spoofing. D) DNS tunneling. |
| 35 — Firewall de aplicação | Um firewall de aplicação bloqueia pedidos com o texto `' OR '1'='1`. Que ataque está a mitigar? | A) Phishing por email. B) Cross-site scripting (XSS). **C) SQL injection.** D) Negação de serviço distribuída (DDoS). |
| 44 — Firewall do núcleo | No Kerberos, o que é um Golden Ticket? | A) Um ticket de serviço legítimo emitido pelo KDC. **B) Um TGT forjado com o hash da conta krbtgt, que dá acesso persistente ao domínio.** C) Um certificado digital emitido para o controlador de domínio. D) Um registo de auditoria assinado pelo servidor Root. |

As explicações completas, mostradas no registo depois de cada resposta, estão em `QuestionBank.java`. Cada pergunta está ligada à firewall correspondente e o banco roda automaticamente se uma firewall receber mais do que uma pergunta no futuro.

## 8. Divisão de tarefas

Cada aluno implementa e testa cerca de um terço do código substantivo.

| Aluno | Área principal | Também contribui com |
|---|---|---|
| A: Igor Santos (30016495) | Estrutura e entidades: `Cell`, `NormalCell`, `AdvanceCell`, `ReturnCell`, `Effect`, `Zone`, `Board`, `Player` | Testes de movimento básico, casas partilhadas e dos 15 efeitos |
| B: Pedro Moroni (30016458) | Motor de jogo e controlo: `Game`, `GameResult`, `TurnEvent`, ressalto, `Dice`, `RandomDice`, `ManualDice`, validação, `GameObserver` | Testes de integração (jogos completos, ressalto do 50, fim às 40 rondas) |
| C: Cleiton Muangala (30016337) | GUI Swing: `MainWindow`, `BoardPanel`, `ControlPanel`, `LogPanel`; mockup artístico e screenshots | Testes da GUI e do modelo |

Os ficheiros Java têm no topo o comentário exigido (módulo e propósito, aluno, revisor e uso de IA). A contagem de linhas e o registo de revisões ficam em ficheiros próprios.

## 9. Marcos

| Marco | Prazo | Estado |
|---|---|---|
| Outubro: regras, mapa, esboço da GUI, UML inicial, tarefas e protótipo de movimento | 29 de outubro | Entregue |
| Novembro: GUI a correr com o tabuleiro, turno processado, dado único, avatares, movimento animado, firewalls com pergunta e testes | 26 de novembro | Em entrega (versão atual) |
| Entrega final: documentação completa, revisão de código e melhorias listadas em `MELHORIAS_FUTURAS.md` | 4 de janeiro de 2027 | Por fazer |

## 10. Como compilar, correr e testar

Requisito: JDK 17 ou superior. Maven 3.9 ou superior é necessário para executar os testes JUnit e também pode ser usado para compilar e iniciar a aplicação.

Na raiz do projeto:

```powershell
mvn test
mvn compile exec:java
```

O primeiro comando compila e executa os testes JUnit 5. O segundo abre a interface Swing.

Sem Maven, é possível compilar e iniciar a aplicação diretamente com o JDK:

```powershell
if (-not (Test-Path out)) { New-Item -ItemType Directory out | Out-Null }
javac --release 17 -d out -sourcepath src\main\java src\main\java\rootaccess\view\MainWindow.java
java -cp out rootaccess.view.MainWindow
```

Esta alternativa inicia a GUI, mas não executa os testes.

## 11. Uso de IA

O grupo desenvolveu a base do projeto, a lógica de jogo e a divisão de tarefas. A IA foi usada como ferramenta de apoio para refinar nomes, comentários, testes, documentação e pequenas otimizações — não para substituir a autoria do trabalho. O registo detalhado está em [`AI_USE.md`](AI_USE.md) e a explicação do código em [`EXPLICACAO.md`](EXPLICACAO.md).
