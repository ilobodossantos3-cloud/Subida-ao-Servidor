// Módulo: rootaccess.GameTest
// Propósito: testes de turnos, validação do dado, ressalto e fim do jogo.
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import rootaccess.model.*;

class GameTest {

    @Test
    void entradaManualInvalidaNaoAlteraEstadoNemTurno() {
        ManualDice dice = new ManualDice();
        Game game = new Game(dice);
        game.start(List.of("Ana", "Rui"));
        Player firstPlayer = game.getCurrentPlayer();

        dice.setInput("7");
        assertThrows(InvalidDiceValueException.class, game::playTurn);

        assertEquals(0, firstPlayer.getPosition());
        assertSame(firstPlayer, game.getCurrentPlayer());
        assertEquals(1, game.getRound());
    }

    @Test
    void turnoAplicaEfeitoDeAvancoUmaVez() throws InvalidDiceValueException {
        ManualDice dice = new ManualDice();
        Game game = new Game(dice);
        game.start(List.of("Ana", "Rui"));
        game.getCurrentPlayer().moveTo(1);
        dice.setInput("3");

        TurnEvent event = game.playTurn();

        assertEquals(4, event.getLanded());
        assertEquals(13, event.getFinalPos());
        assertEquals(13, event.getPlayer().getPosition());
    }

    @Test
    void ultrapassagemRessalta() throws InvalidDiceValueException {
        ManualDice dice = new ManualDice();
        Game game = new Game(dice);
        game.start(List.of("Ana", "Rui"));
        game.getCurrentPlayer().moveTo(49);
        dice.setInput("6");

        TurnEvent event = game.playTurn();

        assertEquals(55, event.getRaw());
        assertEquals(45, event.getLanded());
        assertEquals(45, event.getFinalPos());
    }

    @Test
    void chegadaExataTerminaOJogoENotificaObservadores() throws InvalidDiceValueException {
        ManualDice dice = new ManualDice();
        Game game = new Game(dice);
        game.start(List.of("Ana", "Rui"));
        Player player = game.getCurrentPlayer();
        player.moveTo(47);
        dice.setInput("3");
        GameResult[] observedResult = new GameResult[1];
        game.addObserver(new GameObserver() {
            @Override
            public void onTurn(TurnEvent event) { }

            @Override
            public void onGameOver(GameResult result) {
                observedResult[0] = result;
            }
        });

        game.playTurn();

        assertEquals(50, player.getPosition());
        assertSame(player, game.getResult().getWinners().get(0));
        assertFalse(game.getResult().isDraw());
        assertSame(game.getResult(), observedResult[0]);
    }

    @Test
    void avataresEscolhidosFicamAssociadosAosJogadores() {
        // Um dado manual evita aleatoriedade no arranque do teste.
        ManualDice dice = new ManualDice();
        Game game = new Game(dice);
        // Arranca com dois avatares diferentes e explícitos.
        game.start(List.of("Ana", "Rui"), List.of(Avatar.GHOST, Avatar.BOT));

        // Cada jogador guarda precisamente o avatar que recebeu.
        assertEquals(Avatar.GHOST, game.getPlayers().get(0).getAvatar());
        assertEquals(Avatar.BOT, game.getPlayers().get(1).getAvatar());
    }

    @Test
    void avataresRepetidosSaoRejeitados() {
        // O modelo também protege contra duplicados, não só a GUI.
        Game game = new Game(() -> 1);
        // Dois jogadores não podem partilhar o mesmo avatar.
        assertThrows(IllegalArgumentException.class,
                () -> game.start(List.of("Ana", "Rui"),
                        List.of(Avatar.GHOST, Avatar.GHOST)));
        // A partida anterior (vazia) não ficou parcialmente iniciada.
        assertTrue(game.getPlayers().isEmpty());
    }

    @Test
    void firewallPedePerguntaEAcertarAvancaUmaCasa() throws Exception {
        // Chega à firewall 11 com um lançamento controlado.
        ManualDice dice = new ManualDice();
        Game game = new Game(dice);
        game.start(List.of("Ana", "Rui"));
        Player ana = game.getCurrentPlayer();
        ana.moveTo(10);
        dice.setInput("1");

        // O primeiro evento fica pendente, sem passar a vez.
        TurnEvent waiting = game.playTurn();
        assertTrue(waiting.isAwaitingFirewall());
        assertNotNull(waiting.getQuestion());
        assertEquals(11, waiting.getQuestion().getFirewallCell());
        assertTrue(game.isAwaitingFirewall());
        assertEquals(11, ana.getPosition());
        assertSame(ana, game.getCurrentPlayer());

        // Enquanto a pergunta está aberta, o dado não pode lançar de novo.
        assertThrows(IllegalStateException.class, game::playTurn);

        // A resposta certa liberta a passagem para a casa seguinte.
        TurnEvent resolved = game.answerFirewall(true);
        assertEquals(12, ana.getPosition());
        assertFalse(resolved.isAwaitingFirewall());
        assertEquals(Boolean.TRUE, resolved.getFirewallCorrect());
        // Só agora a vez passa ao jogador seguinte.
        assertNotSame(ana, game.getCurrentPlayer());
    }

    @Test
    void firewallComRespostaErradaMantemOPeaoNoSitio() throws Exception {
        // Mesmo cenário, mas com resposta errada.
        ManualDice dice = new ManualDice();
        Game game = new Game(dice);
        game.start(List.of("Ana", "Rui"));
        Player ana = game.getCurrentPlayer();
        ana.moveTo(10);
        dice.setInput("1");

        // A pergunta abre na casa 11.
        game.playTurn();
        TurnEvent resolved = game.answerFirewall(false);

        // O peão fica retido e o turno passa na mesma.
        assertEquals(11, ana.getPosition());
        assertEquals(Boolean.FALSE, resolved.getFirewallCorrect());
        assertFalse(game.isAwaitingFirewall());
        assertNotSame(ana, game.getCurrentPlayer());
    }

    @Test
    void todasAsFirewallsTemPerguntasValidas() {
        // O banco cobre exatamente as quatro firewalls do tabuleiro.
        QuestionBank bank = new QuestionBank();
        assertEquals(4, bank.size());
        for (int cell : List.of(11, 25, 35, 44)) {
            // Cada casa tem pelo menos uma pergunta associada.
            assertTrue(bank.hasQuestion(cell), "Falta pergunta para a firewall " + cell);
            FirewallQuestion question = bank.nextFor(cell);
            assertEquals(cell, question.getFirewallCell());
            // O índice da resposta correta tem de existir nas opções.
            assertTrue(question.getCorrectIndex() >= 0);
            assertTrue(question.getCorrectIndex() < question.getOptions().size());
            // A explicação é usada no registo de eventos.
            assertFalse(question.getExplanation().isBlank());
        }
    }

    @Test
    void reiniciarMantemJogadoresMasLimpaEstadoDaPartida() throws InvalidDiceValueException {
        ManualDice dice = new ManualDice();
        Game game = new Game(dice);
        game.start(List.of("Ana", "Rui"));
        dice.setInput("3");
        game.playTurn();

        game.restart();

        assertEquals(0, game.getPlayers().get(0).getPosition());
        assertEquals(0, game.getPlayers().get(1).getPosition());
        assertSame(game.getPlayers().get(0), game.getCurrentPlayer());
        assertEquals(1, game.getRound());
        assertNull(game.getResult());
    }

    @Test
    void terminaEmEmpateQuandoChegaAoLimiteDeRondas() throws Exception {
        // Arranca uma partida normal e deixa a vez no segundo jogador.
        Game game = new Game(() -> 1);
        game.start(List.of("Ana", "Rui"));
        game.playTurn();
        // Em vez de simular 80 turnos, coloca a ronda já na última.
        setRound(game, Game.MAX_ROUNDS);

        // Os dois jogadores ficam empatados na firewall 11 depois da resposta.
        game.getPlayers().get(0).moveTo(11);
        Player second = game.getCurrentPlayer();
        second.moveTo(10);
        TurnEvent waiting = game.playTurn();
        assertTrue(waiting.isAwaitingFirewall());
        // A resposta errada mantém o segundo jogador na casa 11.
        game.answerFirewall(false);

        // O limite de rondas produz empate entre os dois líderes.
        assertEquals(Game.MAX_ROUNDS, game.getRound());
        assertTrue(game.getResult().isRoundLimit());
        assertTrue(game.getResult().isDraw());
        assertEquals(2, game.getResult().getWinners().size());
    }

    @Test
    void limiteDeRondasComUmSoLiderNaoEEmpate() throws Exception {
        // Prepara a última ronda com o segundo jogador a chegar à frente.
        Game game = new Game(() -> 1);
        game.start(List.of("Ana", "Rui"));
        game.playTurn();
        setRound(game, Game.MAX_ROUNDS);
        game.getPlayers().get(0).moveTo(8);
        Player second = game.getCurrentPlayer();
        second.moveTo(10);

        // O segundo jogador fica na firewall 11, à frente da Ana.
        game.playTurn();
        game.answerFirewall(false);

        // Um único líder produz ROUND_LIMIT, não DRAW.
        assertTrue(game.getResult().isRoundLimit());
        assertFalse(game.getResult().isDraw());
        assertSame(second, game.getResult().getWinners().get(0));
    }

    // Ajusta a ronda por reflexão para não simular 40 rondas completas.
    private static void setRound(Game game, int round) throws ReflectiveOperationException {
        // O campo é privado porque só o motor deve controlar a ronda.
        Field field = Game.class.getDeclaredField("round");
        field.setAccessible(true);
        field.setInt(game, round);
    }
}
