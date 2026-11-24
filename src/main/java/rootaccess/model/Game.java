// Módulo: rootaccess.model.Game
// Propósito: coordena jogadores, lançamentos, movimento, efeitos e fim do jogo.
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game {

    public static final int MAX_PLAYERS = 4;
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_ROUNDS = 40;

    // Cores fixas por ordem de entrada; o avatar é escolhido à parte.
    private static final String[] PLAYER_COLORS = {"verde", "azul", "amarelo", "rosa"};

    private final Board board;
    private final List<GameObserver> observers = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private Dice dice;
    private int currentPlayerIndex;
    private int round;
    private GameResult result;
    // Banco fixo de perguntas; cada firewall tem a sua.
    private final QuestionBank questions = new QuestionBank();
    // Estado guardado enquanto o jogador responde à firewall.
    private FirewallQuestion pendingQuestion;
    private int pendingRoll;
    private int pendingFrom;
    private int pendingRaw;
    private int pendingLanded;

    public Game() {
        this(new RandomDice());
    }

    public Game(Dice dice) {
        // Sem dado não há turnos possíveis.
        if (dice == null) {
            throw new IllegalArgumentException("O dado é obrigatório");
        }
        this.board = new Board();
        this.dice = dice;
    }

    public void start(int playerCount) {
        // O tabuleiro do projeto foi desenhado para dois a quatro pentesters.
        if (playerCount < MIN_PLAYERS || playerCount > MAX_PLAYERS) {
            throw new IllegalArgumentException("O número de jogadores deve ser entre 2 e 4");
        }
        // Nomes genéricos para arranques rápidos (testes e API).
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            names.add("Jogador " + i);
        }
        // Avatares distintos, um por cada posição inicial.
        List<Avatar> avatars = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            avatars.add(Avatar.at(i));
        }
        start(names, avatars);
    }

    public void start(List<String> names) {
        // A lista nula é rejeitada pela mesma regra da entrada com avatares.
        if (names == null) {
            throw new IllegalArgumentException("O jogo precisa de 2 a 4 jogadores");
        }
        // Chamadas antigas recebem avatares automáticos, sem mudar a assinatura.
        List<Avatar> avatars = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            avatars.add(Avatar.at(i));
        }
        start(names, avatars);
    }

    // Nova entrada do jogo: nomes e avatares escolhidos na configuração.
    public void start(List<String> names, List<Avatar> avatars) {
        // A contagem tem de estar dentro do intervalo permitido.
        if (names == null || names.size() < MIN_PLAYERS || names.size() > MAX_PLAYERS) {
            throw new IllegalArgumentException("O jogo precisa de 2 a 4 jogadores");
        }
        // Cada nome tem de ter exatamente um avatar associado.
        if (avatars == null || avatars.size() != names.size()) {
            throw new IllegalArgumentException("É preciso um avatar por jogador");
        }

        // Constrói os jogadores antes de mexer no estado atual.
        List<Player> newPlayers = new ArrayList<>();
        // Este conjunto impede dois jogadores com o mesmo avatar.
        Set<Avatar> used = new HashSet<>();
        for (int i = 0; i < names.size(); i++) {
            // O avatar tem de ser válido e ainda não estar escolhido.
            if (avatars.get(i) == null || !used.add(avatars.get(i))) {
                throw new IllegalArgumentException("Os avatares têm de ser diferentes");
            }
            // A cor continua a vir da posição para manter a paleta do tabuleiro.
            newPlayers.add(new Player(names.get(i), PLAYER_COLORS[i], avatars.get(i)));
        }

        // Só agora o estado antigo é substituído, para uma falha não o corromper.
        players.clear();
        players.addAll(newPlayers);
        currentPlayerIndex = 0;
        round = 1;
        result = null;
        // Uma pergunta da partida anterior nunca transita para a nova.
        clearPendingFirewall();
    }

    public TurnEvent playTurn() throws InvalidDiceValueException {
        // Um jogo terminado não aceita mais lançamentos.
        if (result != null) {
            throw new IllegalStateException("O jogo já terminou");
        }
        // Uma firewall pendente bloqueia novos lançamentos até à resposta.
        if (pendingQuestion != null) {
            throw new IllegalStateException("Responde à pergunta da firewall antes de lançar");
        }
        // Sem jogadores não há turno a processar.
        if (players.isEmpty()) {
            throw new IllegalStateException("O jogo ainda não foi iniciado");
        }

        Player player = players.get(currentPlayerIndex);
        int roll = dice.roll();
        // Dados personalizados também têm de respeitar o intervalo 1-6.
        if (roll < 1 || roll > 6) {
            throw new IllegalStateException("O dado devolveu um valor fora do intervalo 1-6: " + roll);
        }

        int from = player.getPosition();
        int raw = from + roll;
        // Ressalto: 2 * SIZE - raw equivale a 50 - excedente.
        int landed = raw <= Board.SIZE ? raw : 2 * Board.SIZE - raw;

        // Cair numa firewall abre a pergunta antes de qualquer outro efeito.
        if (board.isFirewall(landed)) {
            return openFirewallQuestion(player, roll, from, raw, landed);
        }

        int finalPos = board.applyEffect(landed);
        player.moveTo(finalPos);

        String message = createTurnMessage(player, roll, from, raw, landed, finalPos);
        TurnEvent event = new TurnEvent(player, roll, from, raw, landed, finalPos, message);

        // Chegar ao Root encerra imediatamente a partida.
        if (finalPos == Board.SIZE) {
            result = GameResult.winner(player);
        } else {
            advanceTurn();
        }

        // Notifica uma cópia da lista: um observador pode remover-se durante o evento.
        notifyTurn(event);
        // O fim do jogo é comunicado depois do evento do turno.
        if (result != null) {
            notifyGameOver();
        }
        return event;
    }

    private TurnEvent openFirewallQuestion(Player player, int roll, int from, int raw, int landed) {
        // O peão fica visível na firewall enquanto a pergunta está aberta.
        player.moveTo(landed);
        // Guarda o lançamento para o evento de resposta ser coerente.
        pendingQuestion = questions.nextFor(landed);
        pendingRoll = roll;
        pendingFrom = from;
        pendingRaw = raw;
        pendingLanded = landed;
        // Mensagem curta; o diálogo mostra o enunciado completo.
        String message = player.getAvatar().getGlyph() + " " + player.getName()
                + " lançou " + roll + ": casa " + from + " -> " + landed
                + ". Firewall! Responde à pergunta para passar.";
        TurnEvent event = new TurnEvent(player, roll, from, raw, landed, landed,
                message, pendingQuestion, null);
        // O turno ainda não avança: a GUI abre a pergunta.
        notifyTurn(event);
        return event;
    }

    public TurnEvent answerFirewall(boolean correct) {
        // Não há resposta sem pergunta pendente.
        if (pendingQuestion == null) {
            throw new IllegalStateException("Não há pergunta de firewall pendente");
        }
        // Guarda a pergunta para a mensagem antes de limpar o estado pendente.
        FirewallQuestion question = pendingQuestion;
        Player player = players.get(currentPlayerIndex);
        int landed = pendingLanded;
        // Acertar passa exatamente uma casa; não se aplica o efeito dessa casa
        // para evitar encadeamentos (por exemplo 35 -> 36 -> 46).
        int finalPos = correct ? Math.min(Board.SIZE, landed + 1) : landed;
        player.moveTo(finalPos);

        // A explicação da pergunta fica no registo, seja certa ou errada.
        String message = (correct ? "✅ Resposta certa. " : "❌ Resposta errada. ")
                + question.getExplanation()
                + (correct
                ? " Passa da casa " + landed + " para a casa " + finalPos + "."
                : " Fica retido na casa " + landed + ".");
        TurnEvent event = new TurnEvent(player, pendingRoll, pendingFrom, pendingRaw,
                landed, finalPos, message, null, correct);
        // O estado pendente tem de desaparecer antes de avançar o turno.
        clearPendingFirewall();

        if (finalPos == Board.SIZE) {
            // Uma firewall nunca está na casa 50, mas a regra fica protegida.
            result = GameResult.winner(player);
        } else {
            // A vez passa mesmo quando a resposta está errada.
            advanceTurn();
        }

        notifyTurn(event);
        // O limite de rondas pode ter sido atingido ao passar a vez.
        if (result != null) {
            notifyGameOver();
        }
        return event;
    }

    private void notifyTurn(TurnEvent event) {
        // Cópia defensiva: um observador pode alterar a lista original.
        for (GameObserver observer : new ArrayList<>(observers)) {
            observer.onTurn(event);
        }
    }

    private void clearPendingFirewall() {
        // Limpa pergunta e lançamento guardados.
        pendingQuestion = null;
        pendingRoll = 0;
        pendingFrom = 0;
        pendingRaw = 0;
        pendingLanded = 0;
    }

    // Indica à GUI que o dado deve permanecer bloqueado.
    public boolean isAwaitingFirewall() {
        return pendingQuestion != null;
    }

    // Permite ao painel mostrar a pergunta pendente, se necessário.
    public FirewallQuestion getPendingQuestion() {
        return pendingQuestion;
    }

    private String createTurnMessage(Player player, int roll, int from, int raw,
                                     int landed, int finalPos) {
        // Mensagem base comum a todos os lançamentos.
        StringBuilder message = new StringBuilder()
                .append(player.getAvatar().getGlyph()).append(' ')
                .append(player.getName()).append(" lançou ").append(roll)
                .append(": casa ").append(from).append(" -> ").append(landed).append('.');
        if (raw > Board.SIZE) {
            // Explica o ressalto para o jogador não achar que perdeu casas.
            message.append(" Ultrapassou o Root e ressaltou para a casa ").append(landed).append('.');
        }
        if (landed != finalPos) {
            // Acrescenta a descrição do efeito que mudou a casa final.
            message.append(' ').append(board.getCell(landed).describe());
        }
        return message.toString();
    }

    private void advanceTurn() {
        // Passa ao jogador seguinte na ordem fixa.
        currentPlayerIndex++;
        if (currentPlayerIndex == players.size()) {
            // Voltou ao primeiro jogador: uma ronda completa terminou.
            currentPlayerIndex = 0;
            if (round == MAX_ROUNDS) {
                result = determineRoundLimitResult();
            } else {
                round++;
            }
        }
    }

    private GameResult determineRoundLimitResult() {
        // Procura a maior posição e todos os jogadores que a partilham.
        int maxPosition = -1;
        List<Player> leaders = new ArrayList<>();
        for (Player player : players) {
            if (player.getPosition() > maxPosition) {
                maxPosition = player.getPosition();
                leaders.clear();
                leaders.add(player);
            } else if (player.getPosition() == maxPosition) {
                leaders.add(player);
            }
        }
        return GameResult.roundLimit(leaders);
    }

    private void notifyGameOver() {
        // Cópia defensiva, tal como na notificação de turnos.
        for (GameObserver observer : new ArrayList<>(observers)) {
            observer.onGameOver(result);
        }
    }

    public void addObserver(GameObserver observer) {
        // Observadores nulos seriam inúteis e escondem erros de ligação.
        if (observer == null) {
            throw new IllegalArgumentException("O observador não pode ser nulo");
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    public void restart() {
        // Repõe os peões fora do tabuleiro.
        for (Player player : players) {
            player.reset();
        }
        currentPlayerIndex = 0;
        round = players.isEmpty() ? 0 : 1;
        result = null;
        // Um reinício no meio de uma pergunta não deixa a firewall pendente.
        clearPendingFirewall();
    }

    public Board getBoard() { return board; }
    public Dice getDice() { return dice; }
    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public Player getCurrentPlayer() {
        // A GUI usa null para saber que ainda não há partida.
        return players.isEmpty() ? null : players.get(currentPlayerIndex);
    }
    public int getRound() { return round; }
    public GameResult getResult() { return result; }
}
