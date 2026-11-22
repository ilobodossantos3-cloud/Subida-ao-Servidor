/*
 * Módulo: rootaccess.model.AdvanceCell
 * Propósito: casa de avanço (brecha / exploit): leva o peão para uma casa
 *            mais à frente.
 * Aluno: A - Igor Santos (30016495)
 * Revisor: [a preencher]
 * Uso de IA: Sim - ver AI_USE.md
 */
package rootaccess.model;

public class AdvanceCell extends Cell implements Effect {

    private final int destination;

    public AdvanceCell(int number, String name, int destination) {
        super(number, name);
        if (destination <= number || destination > Board.SIZE) {
            throw new IllegalArgumentException(
                    "Destino de avanço inválido na casa " + number + ": " + destination);
        }
        this.destination = destination;
    }

    @Override
    public int getDestination() { return destination; }

    @Override
    public int redirect() { return destination; }

    @Override
    public boolean hasEffect() { return true; }

    @Override
    public String describeEffect() {
        return "Brecha! " + getNumber() + " -> " + destination;
    }

    @Override
    public String describe() {
        return getName() + " (" + describeEffect() + ")";
    }
}
