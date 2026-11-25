// Módulo: rootaccess.model.RandomDice
// Propósito: gera lançamentos aleatórios de 1 a 6 (modo aleatório do jogo).
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - Claude Code: construtor com semente para testes repetíveis (ver AI_USE.md, secção 3)
package rootaccess.model;

import java.util.Random;

public class RandomDice implements Dice {

    private final Random random;

    // Construtor usado pelo jogo: cada partida tem lançamentos imprevisíveis.
    public RandomDice() {
        this(new Random());
    }

    // Sobrecarga justificada: os testes passam um Random com semente fixa e
    // obtêm sempre a mesma sequência, sem mudar a lógica do dado.
    public RandomDice(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("O gerador aleatório é obrigatório");
        }
        this.random = random;
    }

    @Override
    public int roll() {
        // nextInt(6) devolve 0 a 5; somar 1 dá as faces 1 a 6.
        return random.nextInt(6) + 1;
    }

    @Override
    public String getModeName() {
        return "aleatório";
    }
}
