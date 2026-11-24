// Módulo: rootaccess.model.RandomDice
// Propósito: gera lançamentos aleatórios de 1 a 6.
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

import java.util.concurrent.ThreadLocalRandom;

public class RandomDice implements Dice {

    @Override
    public int roll() {
        return ThreadLocalRandom.current().nextInt(1, 7);
    }
}
