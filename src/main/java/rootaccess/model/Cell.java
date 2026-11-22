/*
 * Módulo: rootaccess.model.Cell
 * Propósito: classe abstrata que representa uma casa do tabuleiro (número,
 *            nome e zona). As subclasses decidem para onde o peão fica
 *            depois de terminar o movimento nessa casa (polimorfismo).
 * Aluno: A - Igor Santos (30016495)
 * Revisor: [a preencher]
 * Uso de IA: Sim - ver AI_USE.md
 */
package rootaccess.model;

public abstract class Cell {

    private final int number;
    private final String name;

    protected Cell(int number, String name) {
        if (number < 1 || number > Board.SIZE) {
            throw new IllegalArgumentException("Número de casa inválido: " + number);
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A casa " + number + " precisa de um nome");
        }
        this.number = number;
        this.name = name;
    }

    public int getNumber() { return number; }

    public String getName() { return name; }

    public Zone getZone() { return Zone.of(number); }

    /**
     * Casa onde o peão fica depois de terminar o movimento nesta casa.
     * Numa casa normal é a própria casa; numa casa de efeito é o destino.
     */
    public abstract int redirect();

    /** Indica se a casa tem efeito (avanço ou recuo). */
    public boolean hasEffect() { return false; }

    /** Texto para o registo de eventos. */
    public abstract String describe();

    @Override
    public String toString() {
        return number + " " + name;
    }
}
