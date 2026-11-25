// Módulo: rootaccess.model.InvalidDiceValueException
// Propósito: sinaliza uma entrada manual de dado que não é um inteiro entre 1 e 6.
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - Claude Code: mensagem para entrada vazia (ver AI_USE.md, secção 3)
package rootaccess.model;

public class InvalidDiceValueException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String input;

    public InvalidDiceValueException(String input) {
        super(describe(input));
        this.input = input;
    }

    public String getInput() {
        return input;
    }

    // Mensagem legível: um campo vazio não deve aparecer como "null".
    private static String describe(String input) {
        if (input == null || input.isBlank()) {
            return "Nenhum valor introduzido. Escreve um inteiro entre 1 e 6.";
        }
        return "Valor de dado inválido: \"" + input + "\". Escreve um inteiro entre 1 e 6.";
    }
}
