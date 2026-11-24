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

## 3. Onde a IA não foi usada como autoria

- A definição das regras (movimento, ressalto, efeitos, firewall, limite de rondas) foi decidida pelo grupo.
- A divisão de tarefas da secção 8 do README foi definida pelo grupo.
- A implementação base de `Board`, `Game`, `Player`, `Dice` e da GUI foi escrita e integrada pelo grupo.
- As perguntas das firewalls foram produzidas para o projeto e revistas pelo grupo; não foram copiadas de fontes externas sem verificação.

## 4. Verificação

Antes de cada integração:

1. O código foi compilado com `javac --release 17`.
2. Os testes JUnit foram executados (48 testes, 0 falhas).
3. A GUI foi verificada em modo headless para confirmar que o desenho não lança exceções.
4. As alterações de regras foram refletidas no README e nos testes.

## 5. Limitações e responsabilidade

A IA pode produzir sugestões incorretas ou desatualizadas. Por isso, o grupo assume a responsabilidade final pelo código entregue, pela exatidão das regras e pela documentação. Qualquer nova utilização de IA deve ser registada nesta tabela e validada antes da entrega.
