// Módulo: rootaccess.model.ManualDice
// Propósito: valida e devolve o valor introduzido pelo jogador (1 a 6).
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

public class ManualDice implements Dice {

    private String input;

    public void setInput(String input) {
        this.input = input;
    }

    @Override
    public int roll() throws InvalidDiceValueException {
        if (input == null) {
            throw new InvalidDiceValueException(null);
        }

        final int value;
        try {
            value = Integer.parseInt(input.trim());
        } catch (NumberFormatException ex) {
            throw new InvalidDiceValueException(input);
        }

        if (value < 1 || value > 6) {
            throw new InvalidDiceValueException(input);
        }
        input = null;
        return value;
    }
}
