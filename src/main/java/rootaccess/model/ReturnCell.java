// Módulo: rootaccess.model.ReturnCell
// Propósito: casa de recuo (firewall ou honeypot): leva o peão para uma
// casa mais atrás. O tipo serve para a GUI escolher o ícone.
// Aluno: A - Igor Santos (30016495)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

public class ReturnCell extends Cell implements Effect {

    // Os dois tipos de casa de recuo.
    public enum Kind {
        FIREWALL("Firewall"),
        HONEYPOT("Honeypot");

        private final String label;

        Kind(String label) { this.label = label; }

        public String getLabel() { return label; }
    }

    private final Kind kind;
    private final int destination;

    public ReturnCell(int number, String name, Kind kind, int destination) {
        super(number, name);
        if (kind == null) {
            throw new IllegalArgumentException("O tipo de recuo é obrigatório");
        }
        if (destination < 1 || destination >= number) {
            throw new IllegalArgumentException(
                    "Destino de recuo inválido na casa " + number + ": " + destination);
        }
        this.kind = kind;
        this.destination = destination;
    }

    public Kind getKind() { return kind; }

    @Override
    public int getDestination() { return destination; }

    @Override
    public int redirect() {
        // A firewall não move o peão: fica à espera da resposta no Game.
        if (kind == Kind.FIREWALL) {
            return getNumber();
        }
        // Honeypots recuam para o destino definido.
        return destination;
    }

    @Override
    public boolean hasEffect() { return true; }

    @Override
    public String describeEffect() {
        // A firewall é um ponto de verificação, não um recuo para outra casa.
        if (kind == Kind.FIREWALL) {
            return "Firewall! " + getNumber() + ": responde para passar";
        }
        // O honeypot continua a mostrar origem e destino.
        return kind.getLabel() + "! " + getNumber() + " -> " + destination;
    }

    @Override
    public String describe() {
        return getName() + " (" + describeEffect() + ")";
    }
}
