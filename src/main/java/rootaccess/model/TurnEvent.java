// Módulo: rootaccess.model.TurnEvent
// Propósito: representa de forma imutável o resultado de um turno, incluindo
//            o estado intermédio em que uma firewall espera resposta.
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

public final class TurnEvent {

    private final Player player;
    private final int dice;
    private final int from;
    private final int raw;
    private final int landed;
    private final int finalPos;
    private final String message;
    // Preenchida apenas no evento que pede uma resposta ao jogador.
    private final FirewallQuestion question;
    // Null enquanto não há resposta; true/false depois de responder.
    private final Boolean firewallCorrect;

    // Construtor antigo: turnos normais não têm pergunta de firewall.
    public TurnEvent(Player player, int dice, int from, int raw, int landed,
                     int finalPos, String message) {
        // Delega para o construtor completo, sem pergunta e sem resposta.
        this(player, dice, from, raw, landed, finalPos, message, null, null);
    }

    // Construtor completo: usado pelas fases de pergunta e de resposta.
    public TurnEvent(Player player, int dice, int from, int raw, int landed,
                     int finalPos, String message, FirewallQuestion question,
                     Boolean firewallCorrect) {
        // O jogador é a única informação verdadeiramente obrigatória.
        if (player == null) {
            throw new IllegalArgumentException("O jogador do turno é obrigatório");
        }
        // Os restantes valores vêm do modelo já validado.
        this.player = player;
        this.dice = dice;
        this.from = from;
        this.raw = raw;
        this.landed = landed;
        this.finalPos = finalPos;
        // Mensagem nula nunca deve rebentar a GUI; fica vazia.
        this.message = message == null ? "" : message;
        // Pergunta e resposta podem ser nulas em turnos sem firewall.
        this.question = question;
        this.firewallCorrect = firewallCorrect;
    }

    public Player getPlayer() { return player; }
    public int getDice() { return dice; }
    public int getFrom() { return from; }
    public int getRaw() { return raw; }
    public int getLanded() { return landed; }
    public int getFinalPos() { return finalPos; }
    public String getMessage() { return message; }

    // Pergunta pendente, ou null se o turno não estiver bloqueado.
    public FirewallQuestion getQuestion() { return question; }

    // Resultado da resposta: null, true (certa) ou false (errada).
    public Boolean getFirewallCorrect() { return firewallCorrect; }

    // Indica se a GUI deve abrir o diálogo da pergunta.
    public boolean isAwaitingFirewall() {
        // O evento está pendente quando tem pergunta e ainda não tem resposta.
        return question != null && firewallCorrect == null;
    }
}
