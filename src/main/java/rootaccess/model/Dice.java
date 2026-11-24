// Módulo: rootaccess.model.Dice
// Propósito: contrato comum para dados manuais e aleatórios.
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

public interface Dice {

    // Devolve um valor entre 1 e 6 ou rejeita uma entrada manual inválida.
    int roll() throws InvalidDiceValueException;
}
