// Módulo: rootaccess.model.NormalCell
// Propósito: casa sem efeito; o peão fica onde está.
// Aluno: A - Igor Santos (30016495)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

public class NormalCell extends Cell {

    public NormalCell(int number, String name) {
        super(number, name);
    }

    @Override
    public int redirect() {
        return getNumber();
    }

    @Override
    public String describe() {
        return getName();
    }
}
