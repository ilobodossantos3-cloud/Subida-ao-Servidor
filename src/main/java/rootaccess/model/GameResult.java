// Módulo: rootaccess.model.GameResult
// Propósito: guarda o vencedor, empate ou classificação no limite de rondas.
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GameResult {

    public enum Type {
        ROOT_WIN,
        ROUND_LIMIT,
        DRAW
    }

    private final Type type;
    private final List<Player> winners;

    private GameResult(Type type, List<Player> winners) {
        if (type == null || winners == null || winners.isEmpty()) {
            throw new IllegalArgumentException("O resultado precisa de um tipo e pelo menos um jogador");
        }
        this.type = type;
        this.winners = Collections.unmodifiableList(new ArrayList<>(winners));
    }

    public static GameResult winner(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("O vencedor é obrigatório");
        }
        return new GameResult(Type.ROOT_WIN, Collections.singletonList(player));
    }

    public static GameResult roundLimit(List<Player> leaders) {
        if (leaders == null || leaders.isEmpty()) {
            throw new IllegalArgumentException("É necessário indicar pelo menos um jogador");
        }
        Type type = leaders.size() > 1 ? Type.DRAW : Type.ROUND_LIMIT;
        return new GameResult(type, leaders);
    }

    public Type getType() { return type; }
    public List<Player> getWinners() { return winners; }
    public boolean isDraw() { return type == Type.DRAW; }
    public boolean isRoundLimit() { return type == Type.ROUND_LIMIT || type == Type.DRAW; }
}
