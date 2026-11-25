// Módulo: rootaccess.DiceModeTest
// Propósito: verificações repetíveis dos dois modos de dado (aleatório e manual),
//            da validação da entrada manual e de que ambos usam a mesma lógica de turno.
//            Cada teste indica no @DisplayName o estado inicial, a entrada e o resultado esperado.
// Aluno: [a preencher - aluno responsável pelos dados (D) depois de rever]
// Revisor: [a preencher]
// Uso de IA: Sim - Claude Code gerou a primeira versão destes testes (ver AI_USE.md, secção 3)
package rootaccess;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import rootaccess.model.*;

class DiceModeTest {

    // ---------- modo aleatório ----------

    @Test
    @DisplayName("Estado: RandomDice com semente 7 | Entrada: 600 lançamentos "
            + "| Esperado: todos entre 1 e 6 e as seis faces aparecem")
    void dadoAleatorioDaSoValoresDe1a6() {
        RandomDice dice = new RandomDice(new Random(7));
        Set<Integer> faces = new HashSet<>();
        for (int i = 0; i < 600; i++) {
            int value = dice.roll();
            assertTrue(value >= 1 && value <= 6, "Valor fora do intervalo: " + value);
            faces.add(value);
        }
        assertEquals(Set.of(1, 2, 3, 4, 5, 6), faces);
    }

    @Test
    @DisplayName("Estado: dois RandomDice com a mesma semente | Entrada: 20 lançamentos cada "
            + "| Esperado: sequências iguais (teste repetível)")
    void mesmaSementeDaMesmaSequencia() {
        RandomDice first = new RandomDice(new Random(2026));
        RandomDice second = new RandomDice(new Random(2026));
        for (int i = 0; i < 20; i++) {
            assertEquals(first.roll(), second.roll());
        }
    }

    @Test
    @DisplayName("Estado: jogo novo em modo aleatório, peões na casa 0 | Entrada: 1 lançamento "
            + "| Esperado: dado 1-6, casa de chegada = dado, registo mostra o modo")
    void turnoEmModoAleatorioMostraOResultado() throws InvalidDiceValueException {
        Game game = new Game(new RandomDice(new Random(11)));
        game.start(List.of("Ana", "Rui"));

        TurnEvent event = game.playTurn();

        assertTrue(event.getDice() >= 1 && event.getDice() <= 6);
        assertEquals(0, event.getFrom());
        assertEquals(event.getDice(), event.getLanded());
        assertEquals("aleatório", game.getDice().getModeName());
        assertTrue(event.getMessage().contains("lançou " + event.getDice() + " (dado aleatório)"),
                event.getMessage());
        assertEquals("Rui", game.getCurrentPlayer().getName());
    }

    @Test
    @DisplayName("Estado: RandomDice sem gerador | Entrada: null "
            + "| Esperado: IllegalArgumentException")
    void dadoAleatorioExigeGerador() {
        assertThrows(IllegalArgumentException.class, () -> new RandomDice(null));
    }

    // ---------- modo manual ----------

    @ParameterizedTest(name = "entrada \"{0}\" é rejeitada")
    @NullAndEmptySource
    @ValueSource(strings = {"0", "7", "-1", "abc", "   ", "3.5", "2 3", "99999999999"})
    @DisplayName("Estado: jogo novo em modo manual, peões na casa 0, vez da Ana, ronda 1 "
            + "| Entrada: valor inválido | Esperado: InvalidDiceValueException e estado inalterado")
    void entradaManualInvalidaNaoMudaOEstado(String input) {
        ManualDice dice = new ManualDice();
        Game game = new Game(dice);
        game.start(List.of("Ana", "Rui", "Eva"));
        Player ana = game.getCurrentPlayer();

        dice.setInput(input);
        InvalidDiceValueException error =
                assertThrows(InvalidDiceValueException.class, game::playTurn);

        assertFalse(error.getMessage().isBlank());
        for (Player player : game.getPlayers()) {
            assertEquals(0, player.getPosition());
        }
        assertSame(ana, game.getCurrentPlayer());
        assertEquals(1, game.getRound());
        assertNull(game.getResult());
        assertFalse(game.isAwaitingFirewall());
    }

    @Test
    @DisplayName("Estado: modo manual, Ana na casa 0 | Entrada: \"7\" e depois \" 3 \" "
            + "| Esperado: o 7 é recusado, o 3 é aceite (espaços ignorados) e a Ana fica na casa 3")
    void depoisDeUmaEntradaInvalidaOMesmoJogadorRepete() throws InvalidDiceValueException {
        ManualDice dice = new ManualDice();
        Game game = new Game(dice);
        game.start(List.of("Ana", "Rui"));
        Player ana = game.getCurrentPlayer();

        dice.setInput("7");
        assertThrows(InvalidDiceValueException.class, game::playTurn);
        dice.setInput(" 3 ");
        TurnEvent event = game.playTurn();

        assertSame(ana, event.getPlayer());
        assertEquals(3, ana.getPosition());
        assertTrue(event.getMessage().contains("(dado manual)"), event.getMessage());
        assertEquals("Rui", game.getCurrentPlayer().getName());
    }

    @Test
    @DisplayName("Estado: modo manual, Ana jogou um 3 | Entrada: novo lançamento sem escrever valor "
            + "| Esperado: recusado (o valor anterior não é reutilizado) e o Rui continua com a vez")
    void valorManualEConsumidoEmCadaTurno() throws InvalidDiceValueException {
        ManualDice dice = new ManualDice();
        Game game = new Game(dice);
        game.start(List.of("Ana", "Rui"));
        dice.setInput("3");
        game.playTurn();
        Player rui = game.getCurrentPlayer();

        assertThrows(InvalidDiceValueException.class, game::playTurn);

        assertSame(rui, game.getCurrentPlayer());
        assertEquals(0, rui.getPosition());
    }

    // ---------- os dois modos usam a mesma lógica ----------

    @Test
    @DisplayName("Estado: dois jogos de 3 jogadores, um aleatório (semente 2026) e outro manual "
            + "| Entrada: no manual escrevem-se os mesmos valores que o aleatório sorteou "
            + "| Esperado: posições e vencedor iguais em todos os turnos")
    void osDoisModosProduzemOMesmoJogo() throws InvalidDiceValueException {
        Game randomGame = new Game(new RandomDice(new Random(2026)));
        ManualDice manualDice = new ManualDice();
        Game manualGame = new Game(manualDice);
        List<String> names = List.of("Ana", "Rui", "Eva");
        randomGame.start(names);
        manualGame.start(names);

        int turns = 0;
        while (randomGame.getResult() == null && turns < 500) {
            TurnEvent randomEvent = randomGame.playTurn();
            // O manual recebe exatamente o valor que o aleatório produziu.
            manualDice.setInput(Integer.toString(randomEvent.getDice()));
            TurnEvent manualEvent = manualGame.playTurn();

            // As firewalls são respondidas da mesma forma nos dois jogos.
            if (randomEvent.isAwaitingFirewall()) {
                int answer = randomGame.getPendingQuestion().getCorrectIndex();
                randomEvent = randomGame.answerFirewall(answer);
                manualEvent = manualGame.answerFirewall(answer);
            }

            assertEquals(randomEvent.getFinalPos(), manualEvent.getFinalPos());
            assertEquals(positions(randomGame), positions(manualGame));
            turns++;
        }

        assertNotNull(randomGame.getResult(), "O jogo tem de terminar");
        assertEquals(randomGame.getResult().getType(), manualGame.getResult().getType());
        assertEquals(randomGame.getResult().getWinners().get(0).getName(),
                manualGame.getResult().getWinners().get(0).getName());
    }

    @Test
    @DisplayName("Estado: os dois dados | Entrada: getModeName() "
            + "| Esperado: \"aleatório\" e \"manual\" (polimorfismo da interface Dice)")
    void cadaDadoIndicaOSeuModo() {
        Dice random = new RandomDice();
        Dice manual = new ManualDice();
        assertEquals("aleatório", random.getModeName());
        assertEquals("manual", manual.getModeName());
    }

    // Posições de todos os jogadores, no formato do modelo: (p1, p2, p3).
    private static List<Integer> positions(Game game) {
        List<Integer> result = new ArrayList<>();
        for (Player player : game.getPlayers()) {
            result.add(player.getPosition());
        }
        return result;
    }
}
