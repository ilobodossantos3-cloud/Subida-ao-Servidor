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
| Tabuleiro | 50 casas numeradas de 1 (Internet) a 50 (Root), em serpentina, com **15 casas de efeito** |
| Início | Todos os peões começam na posição 0, fora do tabuleiro |
| Ordem dos turnos | Fixa: jogador 1, 2, 3, 4 e volta ao 1 |
| Turno | O jogador atual lança o dado (1 a 6) e o peão avança esse número de casas (ver o protocolo na secção 3) |
| Dados | No início escolhe-se **manual** ou **aleatório**. Os dois modos usam exatamente a mesma lógica de jogo |
| Dado manual | O jogador escreve um valor entre 1 e 6. Um valor inválido é rejeitado, o estado do jogo não muda e o mesmo jogador tem de voltar a introduzir |
| Dado aleatório | O valor sai de um gerador e é mostrado no ecrã |
| Casas partilhadas | Vários peões podem estar na mesma casa |
| Casas de avanço | Ao terminar o movimento numa casa de avanço (8 casas), o peão passa para a casa de destino |
| Casas de recuo | Ao terminar o movimento numa casa de recuo (4 firewalls e 3 honeypots), o peão passa para a casa de destino |
| Encadeamento | Cada efeito aplica-se **uma só vez** por movimento. Nenhum destino é casa de efeito, por isso não há encadeamentos |
| Chegada ao Root | É preciso chegar **exatamente** à casa 50 |
| Ultrapassagem | Se o lançamento passar da casa 50, o peão ressalta: posição final = 50 − excedente (da casa 48 com um 5 passa-se 3, e fica na casa 47) |
| Vitória | Ganha o primeiro jogador a chegar à casa 50 |
| Fim sem jogo infinito | Limite de **40 rondas** (uma ronda = todos os jogadores jogam uma vez). Se ninguém chegou ao Root, ganha quem estiver mais à frente. Se houver jogadores empatados na frente, o resultado é empate entre eles |
| Sair e reiniciar | Botões disponíveis a qualquer momento. Reiniciar volta ao ecrã de configuração |

**Nota sobre o ressalto:** o excedente é no máximo 5 (da casa 49 com um 6), por isso uma ultrapassagem nunca deixa o peão abaixo da casa 45. A casa 44 é a última casa de efeito antes do Root, logo o ressalto nunca cai numa casa de efeito.

## 3. Protocolo de movimento

Um turno processa-se sempre pela mesma sequência de passos, em `Game.playTurn()`. O modelo não depende da GUI: a interface só chama este método e desenha o resultado.

| # | Passo | O que acontece |
|---|---|---|
| 1 | Verificar o estado | Se o jogo já terminou (`result != null`), o turno é recusado. Caso contrário, o jogador atual é `players[current]`. |
| 2 | Obter o dado | `dice.roll()` devolve um valor de 1 a 6. No modo manual, um valor inválido (0, 7, texto, vazio) lança `InvalidDiceValueException`: o estado **não muda** e o mesmo jogador volta a introduzir. O modo aleatório e o manual usam o mesmo caminho a partir daqui. |
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
    Player p = players.get(current);

    int d = dice.roll();                                  // 1..6; inválido => exceção, nada muda
    int raw = p.getPosition() + d;
    int pos = (raw <= Board.SIZE) ? raw : bounce(raw);    // bounce(raw) = 2 * Board.SIZE - raw
    int landed = pos;
    pos = board.applyEffect(pos);                         // avanço/recuo, uma só vez
    p.moveTo(pos);

    if (pos == Board.SIZE) result = GameResult.winner(p);
    else advanceTurn();                                   // current++, rondas++ ao voltar ao 1.º; limite de 40

    TurnEvent ev = new TurnEvent(p, d, raw, landed, pos);
    notifyObservers(ev);
    return ev;
}
```

Exemplos (todos verificados por simulação):

| Caso | Casa de partida | Dado | Cálculo | Casa final |
|---|---|---|---|---|
| Movimento normal | 0 | 3 | 0 + 3 = 3 (casa normal) | 3 |
| Brecha (avanço) | 1 | 3 | 1 + 3 = 4 → casa 4 é brecha → 13 | 13 |
| Firewall (recuo) | 9 | 2 | 9 + 2 = 11 → casa 11 é firewall → 2 | 2 |
| Honeypot (recuo) | 14 | 4 | 14 + 4 = 18 → casa 18 é honeypot → 6 | 6 |
| Ressalto | 48 | 5 | 48 + 5 = 53 > 50 → excedente 3 → 50 − 3 = 47 | 47 |
| Ressalto máximo | 49 | 6 | 49 + 6 = 55 → excedente 5 → 45 | 45 |
| Chegada exata | 47 | 3 | 47 + 3 = 50 → Root, vence | 50 |
| Entrada inválida | 0 | `7` (manual) | rejeitado, estado inalterado, o mesmo jogador repete | (mantém-se) |

## 4. Mapa do tabuleiro

Serpentina de 5 linhas × 10 colunas. A casa 1 está em baixo à esquerda e a 50 em cima à direita. Cada linha é uma zona da rede.

```
 Núcleo       | [41 ^48]    42       43    [44 v33]    45       46       47       48       49    [50 ROOT]
 Datacenter   | [40 v29]    39       38       37    [36 ^46] [35 v24]    34       33    [32 ^43]    31   
 Rede Interna |    21    [22 ^34]    23       24    [25 v14]    26       27    [28 ^39]    29    [30 v19]
 DMZ          |    20       19    [18 v6]     17    [16 ^27]    15       14       13       12    [11 v2] 
 Internet     |   [1]       2        3     [4 ^13]     5        6        7     [8 ^21]     9        10   
```

Legenda: `^` avanço, `v` recuo. O número depois do símbolo é a casa de destino.

### Casas com efeito (15)

| Casa | Nome | Tipo | Destino | Zona |
|---|---|---|---|---|
| 4 | Exploit encontrado | Brecha / exploit (avanço) | 13 | Internet |
| 8 | Credenciais fracas | Brecha / exploit (avanço) | 21 | Internet |
| 11 | Firewall perimetral | Firewall (recuo) | 2 | DMZ |
| 16 | Vulnerabilidade zero-day | Brecha / exploit (avanço) | 27 | DMZ |
| 18 | Honeypot da DMZ | Honeypot (recuo) | 6 | DMZ |
| 22 | Privilege escalation | Brecha / exploit (avanço) | 34 | Rede Interna |
| 25 | Firewall interna | Firewall (recuo) | 14 | Rede Interna |
| 28 | Pass-the-hash | Brecha / exploit (avanço) | 39 | Rede Interna |
| 30 | Honeypot interno | Honeypot (recuo) | 19 | Rede Interna |
| 32 | SQL injection | Brecha / exploit (avanço) | 43 | Datacenter |
| 35 | Firewall de aplicação | Firewall (recuo) | 24 | Datacenter |
| 36 | Kerberoasting | Brecha / exploit (avanço) | 46 | Datacenter |
| 40 | Honeypot do datacenter | Honeypot (recuo) | 29 | Datacenter |
| 41 | Golden ticket | Brecha / exploit (avanço) | 48 | Núcleo |
| 44 | Firewall do núcleo | Firewall (recuo) | 33 | Núcleo |

Total: 8 brechas que avançam, 4 firewalls e 3 honeypots que recuam. Todas as outras casas são normais. A casa 1 (Internet) é a primeira casa e a casa 50 (Root) é a casa final.

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
| Peão do jogador 1 | Cursor verde na casa 23 | `players.get(0).getPosition() == 23` | Cursor verde a piscar |
| Estado do jogo | Painel com a posição de cada jogador | `(23, 26, 9)`: posições dos jogadores 1, 2 e 3 | Ecrã de terminal com a rede |
| Casa de avanço (casa 4) | Casa a verde com chave e seta | `AdvanceCell(4, 13)` | Ícone de chave (brecha) |
| Casa de recuo (casa 11) | Casa a vermelho com chama | `ReturnCell(11, 2)` | Ícone de chama (firewall) |
| Casa de recuo (casa 18) | Casa a laranja com pote | `ReturnCell(18, 6)` | Ícone de pote (honeypot) |
| Dado | Botão aleatório e campo manual | `Dice` com `RandomDice` e `ManualDice` | Painel com dado e campo |
| Turno atual | Nome destacado no painel | `game.getCurrentPlayer()` | Painel lateral |
| Fim do jogo | Mensagem do vencedor | `GameResult` | Ecrã final com o Root Master |

Exemplo do modelo com 4 jogadores: `(23, 26, 9, 0)` significa que os jogadores 1, 2 e 3 estão nas casas 23, 26 e 9, e o jogador 4 ainda não saiu da posição 0. Os exemplos usam casas normais, porque um peão nunca fica parado numa casa de efeito: é logo redirecionado.

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
| `Player` | Nome, cor e posição |
| `Dice` (interface) | Contrato de um dado |
| `RandomDice` / `ManualDice` | Os dois modos de dado |
| `InvalidDiceValueException` | Valor de dado inválido (modo manual) |
| `Game` | `List<Player>`, turno atual, rondas, ressalto, deteção do vencedor, reinício |
| `TurnEvent` | Resumo de um turno, enviado à GUI |
| `GameResult` | Resultado final (vencedor, empate ou limite de rondas) |
| `GameObserver` (interface) | Liga o modelo à GUI (padrão Observer; nome provisório) |

Conceitos de POO que vamos usar: encapsulamento, classes colaborantes, herança com *overriding* (`Cell`), interfaces e polimorfismo (`Dice`, `Effect`), coleções genéricas (`List<Player>`, `List<Cell>`) e um *overload* justificado (por exemplo `Game.start(int)` e `Game.start(List<String>)`).

**Interface gráfica (só desenho, separada do modelo):** `MainWindow`, `BoardPanel`, `ControlPanel`, `LogPanel`. Os *event handlers* limitam-se a chamar métodos de `Game`.

### Estrutura de ficheiros (projeto IntelliJ)

```
SubidaAoServidor/
├── README.md
├── AI_USE.md
├── docs/
│   └── uml_classes.png
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
    │   │   ├── Dice.java            (interface)
    │   │   ├── RandomDice.java
    │   │   ├── ManualDice.java
    │   │   ├── InvalidDiceValueException.java
    │   │   ├── Game.java
    │   │   ├── TurnEvent.java
    │   │   ├── GameResult.java
    │   │   └── GameObserver.java    (interface)
    │   └── gui/
    │       ├── MainWindow.java      (main)
    │       ├── BoardPanel.java
    │       ├── ControlPanel.java
    │       └── LogPanel.java
    └── test/java/rootaccess/
        ├── BoardTest.java
        ├── DiceTest.java
        ├── GameTest.java
        └── GameFlowTest.java
```


## 6. Esboço da GUI

```
+-----------------------------------------------------------------------------+
|  ROOT ACCESS: Subida ao Servidor                                            |
+---------------------------------------------------+-------------------------+
|  41 42 43 [44] 45 46 47 48 49 [50]                |  Turno: Ana (verde)     |
|  [40] 39 38 37 [36] [35] 34 33 [32] 31            |  Último dado: 5         |
|  21 [22] 23 24 [25] 26 27 [28] 29 [30]            |                         |
|  20 19 [18] 17 [16] 15 14 13 12 [11]              |  Dado manual: [__] OK   |
|  [1] 2 3 [4] 5 6 7 [8] 9 10                       |  [ Lançar aleatório ]   |
|                                                   |  [ Reiniciar ] [ Sair ] |
|                                                   |  Registo de eventos:    |
|                                                   |  > Ana lança 3          |
|                                                   |  > Brecha! 4 -> 13      |
+---------------------------------------------------+-------------------------+
```

## 7. Testes previstos

Cada verificação define estado inicial, entrada e resultado esperado, com lançamentos predeterminados e relatório de sucesso ou falha.

- Os dois modos de dado (manual e aleatório com sequência fixa)
- Entrada inválida (0, 7, texto, vazio) sem alterar o estado
- Movimento normal
- Cada um dos 15 efeitos: casas 4, 8, 11, 16, 18, 22, 25, 28, 30, 32, 35, 36, 40, 41 e 44
- Limites: ressalto a partir da casa 50 (por exemplo 48 + 5 → 47 e 49 + 6 → 45), chegada exata
- Casas partilhadas
- Fim por limite de rondas e empate
- Jogos com 2, 3 e 4 jogadores
- Reinício e um jogo completo

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
| Outubro: regras, mapa, esboço da GUI, UML inicial, tarefas e protótipo de movimento | 29 de outubro | Em entrega |
| Novembro: GUI a correr com o tabuleiro, turno processado, os dois modos de dado, efeitos e testes iniciais | 26 de novembro | por fazer |
| Entrega final | 4 de janeiro de 2027 | por fazer |

## 10. Como compilar, correr e testar

`[PREENCHER]`: versão do Java, comando de compilação, comando para correr a GUI e comando para correr os testes.

## 11. Uso de IA

O registo está em `AI_USE.md` (ferramenta, finalidade, prompt, resultado retido, alterações e verificação), incluindo o prompt principal do mockup.
