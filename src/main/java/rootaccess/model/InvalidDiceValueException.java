// Módulo: rootaccess.model.InvalidDiceValueException
// Propósito: sinaliza uma entrada manual de dado que não é um inteiro entre 1 e 6.
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

public class InvalidDiceValueException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String input;

    public InvalidDiceValueException(String input) {
        super("Valor de dado inválido: \"" + input + "\". Introduza um inteiro entre 1 e 6.");
        this.input = input;
    }

    public String getInput() {
        return input;
    }
}
