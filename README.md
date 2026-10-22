# ROOT ACCESS: Subida ao Servidor

Jogo de tabuleiro em Java (Swing) para 2 a 4 jogadores.
Projeto de Programação Orientada a Objetos, UAL, 2026–2027.

> **Estado:** marco de outubro (rascunho). Regras, mapa do tabuleiro e desenho inicial.
> Os campos marcados com `[PREENCHER]` completam-se à medida que o projeto avança.

---

## 1. Tema

Numa noite qualquer, a rede da *NexaCorp* tem um servidor central, o **Root**, onde está guardado tudo o que a empresa tem. Dois a quatro *pentesters* competem num desafio de segurança ofensiva autorizado: o primeiro a escalar da internet pública até ao Root ganha o título de **Root Master**.

Cada jogador é um cursor a piscar num terminal. Tudo é simulado e o dado decide o que a rede deixa passar.

A mecânica de base inspira-se no jogo clássico *Cobras e Escadas*: tabuleiro numerado, dado de 1 a 6, casas que fazem avançar e casas que fazem recuar. O tema, as zonas, o fim do jogo e a representação em três níveis são do nosso projeto.

## 2. Regras

| Tema | Regra |
|---|---|
| Jogadores | 2 a 4, escolhidos no início, com nome para cada um |
| Tabuleiro | 30 casas numeradas de 1 (Internet) a 30 (Root), em serpentina |
| Início | Todos os peões começam na posição 0, fora do tabuleiro |
| Ordem dos turnos | Fixa: jogador 1, 2, 3, 4 e volta ao 1 |
| Turno | O jogador atual lança o dado (1 a 6) e o peão avança esse número de casas |
| Dados | No início escolhe-se **manual** ou **aleatório**. Os dois modos usam exatamente a mesma lógica de jogo |
| Dado manual | O jogador escreve um valor entre 1 e 6. Um valor inválido é rejeitado, o estado do jogo não muda e o mesmo jogador tem de voltar a introduzir |
| Dado aleatório | O valor sai de um gerador e é mostrado no ecrã |
| Casas partilhadas | Vários peões podem estar na mesma casa |
| Casas de avanço | Ao terminar o movimento numa casa de avanço, o peão passa para a casa de destino |
| Casas de recuo | Ao terminar o movimento numa casa de recuo, o peão passa para a casa de destino |
| Encadeamento | Cada efeito aplica-se **uma só vez** por movimento. Nesta versão nenhum destino é casa de efeito, por isso não há encadeamentos |
| Chegada ao Root | É preciso chegar **exatamente** à casa 30 |
| Ultrapassagem | Se o lançamento passar da casa 30, o peão ressalta: posição final = 30 − excedente (da casa 28 com um 5 passa-se 3, e fica na casa 27) |
| Vitória | Ganha o primeiro jogador a chegar à casa 30 |
| Fim sem jogo infinito | Limite de **40 rondas** (uma ronda = todos os jogadores jogam uma vez). Se ninguém chegou ao Root, ganha quem estiver mais à frente. Se houver jogadores empatados na frente, o resultado é empate entre eles |
| Sair e reiniciar | Botões disponíveis a qualquer momento. Reiniciar volta ao ecrã de configuração |

**Nota sobre o ressalto:** a partir de qualquer casa e com qualquer lançamento, uma ultrapassagem nunca deixa o peão abaixo da casa 25, por isso o ressalto nunca cai numa casa de efeito.

## 3. Mapa do tabuleiro

Serpentina de 5 linhas × 6 colunas. A casa 1 está em baixo à esquerda e a 30 em cima à direita.

```
 Núcleo         |  25    26    27    28    29   [30 ROOT]
 Datacenter     | [24 v11] 23    22    21    20   [19 ^26]
 Rede Interna   |  13    14    15    16    17    18
 DMZ            | [12 v4]  11    10     9     8   [7 ^15]
 Internet       |  [1]     2     3     4     5     6
```

Legenda: `^` avanço, `v` recuo. O número depois do símbolo é a casa de destino.

### Casas com efeito

| Casa | Nome | Tipo | Destino |
|---|---|---|---|
| 7 | Exploit encontrado | Avanço | 15 |
| 19 | Privilege escalation | Avanço | 26 |
| 12 | Firewall | Recuo | 4 |
| 24 | Honeypot | Recuo | 11 |

Todas as outras casas são normais. A casa 1 (Internet) é a primeira casa e a casa 30 (Root) é a casa final.

### As cinco zonas

| Casas | Zona | Cor |
|---|---|---|
| 1–6 | Internet | azul-frio |
| 7–12 | DMZ | verde |
| 13–18 | Rede Interna | âmbar |
| 19–24 | Datacenter | laranja |
| 25–30 | Núcleo | vermelho |

### Nomes das casas (cenário, sem efeito nas regras)

| Zona | Casas e nomes |
|---|---|
| Internet | 1 Internet, 2 Router doméstico, 3 Wi-Fi do café, 4 DNS público, 5 Fórum anónimo, 6 Servidor proxy |
| DMZ | 7 **Exploit encontrado**, 8 Servidor web, 9 Formulário de login, 10 API exposta, 11 Servidor de email, 12 **Firewall** |
| Rede Interna | 13 Portátil do estagiário, 14 Impressora de rede, 15 Partilha de ficheiros, 16 Servidor de impressão, 17 Intranet, 18 Switch interno |
| Datacenter | 19 **Privilege escalation**, 20 Base de dados, 21 Cluster de backups, 22 Servidor de logs, 23 Balanceador de carga, 24 **Honeypot** |
| Núcleo | 25 Cofre de chaves, 26 Controlador de domínio, 27 Consola de administração, 28 Servidor de certificados, 29 Servidor de auditoria, 30 **Root** |

## 4. Os três níveis de representação

O mesmo jogo é representado em três níveis. A tabela liga-os:

| Conceito de jogo | GUI (Swing) | Modelo (Java) | Mockup |
|---|---|---|---|
| Peão do jogador 1 | Cursor verde na casa 22 | `players.get(0).getPosition() == 22` | Cursor verde a piscar |
| Estado do jogo | Painel com a posição de cada jogador | `(22, 24, 9)`: posições dos jogadores 1, 2 e 3 | Ecrã de terminal com a rede |
| Casa de avanço (casa 7) | Casa a verde com seta | `AdvanceCell(7, 15)` | Ícone de brecha ou chave |
| Casa de recuo (casa 12) | Casa a vermelho | `ReturnCell(12, 4)` | Muro de chamas ou escudo |
| Dado | Botão aleatório e campo manual | `Dice` com `RandomDice` e `ManualDice` | Painel com dado e campo |
| Turno atual | Nome destacado no painel | `game.getCurrentPlayer()` | Painel lateral |
| Fim do jogo | Mensagem do vencedor | `GameResult` | Ecrã final com o Root Master |

Exemplo do modelo com 4 jogadores: `(22, 24, 9, 0)` significa que os jogadores 1, 2 e 3 estão nas casas 22, 24 e 9, e o jogador 4 ainda não saiu da posição 0.

## 5. Desenho das classes (versão inicial, a rever no UML)

**Modelo (sem qualquer dependência da GUI):**

| Classe | Função |
|---|---|
| `Cell` (abstrata) | Casa do tabuleiro, com número e nome |
| `NormalCell` | Casa sem efeito |
| `Effect` (interface) | Contrato dos efeitos, com o destino da casa |
| `AdvanceCell` / `ReturnCell` | Casas que implementam `Effect` e redirecionam o peão |
| `Board` | `List<Cell>` com as 30 casas e as regras de movimento |
| `Player` | Nome, cor e posição |
| `Dice` (interface) | Contrato de um dado |
| `RandomDice` / `ManualDice` | Os dois modos de dado |
| `Game` | `List<Player>`, turno atual, rondas, deteção do vencedor, reinício |
| `GameResult` | Resultado final (vencedor, empate ou limite de rondas) |

Conceitos de POO que vamos usar: encapsulamento, classes colaborantes, herança com *overriding* (`Cell`), interfaces e polimorfismo (`Dice`, `Effect`), coleções genéricas (`List<Player>`, `List<Cell>`) e um *overload* justificado (por exemplo `Game.start(int)` e `Game.start(List<String>)`).

**Interface gráfica (só desenho, separada do modelo):** `MainWindow`, `BoardPanel`, `ControlPanel`, `LogPanel`. Os *event handlers* limitam-se a chamar métodos de `Game`.

## 6. Esboço da GUI

```
+--------------------------------------------------------------+
|  ROOT ACCESS: Subida ao Servidor                             |
+--------------------------------------+-----------------------+
|  25 26 27 28 29 [30]                 |  Turno: Ana (verde)   |
|  [24] 23 22 21 20 [19]               |  Último dado: 5       |
|  13 14 15 16 17 18                   |                       |
|  [12] 11 10 9 8 [7]                  |  Dado manual: [__] OK |
|  [1] 2 3 4 5 6                       |  [ Lançar aleatório ] |
|                                      |  [ Reiniciar ] [Sair] |
|                                      |  Registo de eventos:  |
|                                      |  > Ana lança 5        |
|                                      |  > Exploit! 7 -> 15   |
+--------------------------------------+-----------------------+
```

## 7. Testes previstos

Cada verificação define estado inicial, entrada e resultado esperado, com lançamentos predeterminados e relatório de sucesso ou falha.

- Os dois modos de dado (manual e aleatório com sequência fixa)
- Entrada inválida (0, 7, texto, vazio) sem alterar o estado
- Movimento normal
- Cada efeito: casas 7, 19, 12 e 24
- Limites: ressalto a partir da casa 30, chegada exata
- Casas partilhadas
- Fim por limite de rondas e empate
- Jogos com 2, 3 e 4 jogadores
- Reinício e um jogo completo

## 8. Divisão de tarefas

Cada aluno implementa e testa cerca de um terço do código substantivo.

| Aluno | Área principal | Também contribui com |
|---|---|---|
| A: `[PREENCHER nome/número]` | Núcleo do jogo: `Board`, `Cell` e efeitos, `Player`, `Game` | Testes das regras |
| B: `[PREENCHER nome/número]` | Dados, validação e testes automáticos | Parte das regras ou da GUI |
| C: `[PREENCHER nome/número]` | GUI Swing, mockup artístico, screenshots | Testes da GUI e do modelo |

Os ficheiros Java têm no topo o comentário exigido (módulo e propósito, aluno, revisor e uso de IA). A contagem de linhas e o registo de revisões ficam em ficheiros próprios.

## 9. Marcos

| Marco | Prazo | Estado |
|---|---|---|
| Outubro: regras, mapa, esboço da GUI, UML inicial, tarefas e protótipo de movimento | 29 de outubro | `[PREENCHER]` |
| Novembro: GUI a correr com o tabuleiro, turno processado, os dois modos de dado, efeitos e testes iniciais | 26 de novembro | por fazer |
| Entrega final | 4 de janeiro de 2027 | por fazer |

## 10. Como compilar, correr e testar

`[PREENCHER]`: versão do Java, comando de compilação, comando para correr a GUI e comando para correr os testes.

## 11. Uso de IA

O registo está em `AI_USE.md` (ferramenta, finalidade, prompt, resultado retido, alterações e verificação), incluindo o prompt principal do mockup.
