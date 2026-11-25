// Módulo: rootaccess.model.Dice
// Propósito: contrato comum para dados manuais e aleatórios.
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - Claude Code: getModeName() para o registo mostrar o modo do dado (ver AI_USE.md, secção 3)
package rootaccess.model;

public interface Dice {

    // Devolve um valor entre 1 e 6 ou rejeita uma entrada manual inválida.
    int roll() throws InvalidDiceValueException;

    // Nome do modo, mostrado no registo de cada turno ("aleatório" ou "manual").
    // Tem implementação por omissão para os dados de teste escritos como lambda.
    default String getModeName() {
        return "predefinido";
    }
}
