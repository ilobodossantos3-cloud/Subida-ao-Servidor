// Módulo: rootaccess.BoardTest
// Propósito: testes JUnit 5 do tabuleiro: composição das 50 casas, os 15
// efeitos, movimento básico (via Board + Player) e casas partilhadas.
// Aluno: A - Igor Santos (30016495)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import rootaccess.model.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    // ---------- composição do tabuleiro ----------

    @Test
    void tabuleiroTem50CasasE15Efeitos() {
        assertEquals(50, board.getCells().size());
        assertEquals(15, board.countEffects());
    }

    @Test
    void tiposDeEfeitoSao8Avancos4Firewalls3Honeypots() {
        int avancos = 0, firewalls = 0, honeypots = 0;
        for (Cell c : board.getCells()) {
            if (c instanceof AdvanceCell) avancos++;
            else if (c instanceof ReturnCell r) {
                if (r.getKind() == ReturnCell.Kind.FIREWALL) firewalls++;
                else honeypots++;
            }
        }
        assertEquals(8, avancos);
        assertEquals(4, firewalls);
        assertEquals(3, honeypots);
    }

    @Test
    void nenhumDestinoECasaDeEfeito() {
        for (Cell c : board.getCells()) {
            if (c instanceof Effect e) {
                assertFalse(board.getCell(e.getDestination()).hasEffect(),
                        "Destino de " + c.getNumber() + " é casa de efeito");
            }
        }
    }

    @Test
    void zonasCorrespondemAsLinhas() {
        assertEquals(Zone.INTERNET, board.getCell(1).getZone());
        assertEquals(Zone.INTERNET, board.getCell(10).getZone());
        assertEquals(Zone.DMZ, board.getCell(11).getZone());
        assertEquals(Zone.REDE_INTERNA, board.getCell(25).getZone());
        assertEquals(Zone.DATACENTER, board.getCell(40).getZone());
        assertEquals(Zone.NUCLEO, board.getCell(50).getZone());
    }

    @Test
    void casaInexistenteLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> board.getCell(0));
        assertThrows(IllegalArgumentException.class, () -> board.getCell(51));
    }

    // ---------- os 15 efeitos ----------

    @ParameterizedTest(name = "casa {0} -> {1}")
    @CsvSource({
        "4,13", "8,21", "16,27", "18,6", "22,34", "28,39",
        "30,19", "32,43", "36,46", "40,29", "41,48"
    })
    void efeitoLevaAoDestinoCorreto(int casa, int destino) {
        assertTrue(board.getCell(casa).hasEffect());
        assertEquals(destino, board.applyEffect(casa));
    }

    @ParameterizedTest(name = "firewall {0} fica à espera")
    @ValueSource(ints = {11, 25, 35, 44})
    void firewallMantemOPeaoNaCasaAteAResposta(int casa) {
        // A firewall continua a ser uma casa de efeito, mas já não recua sozinha.
        assertTrue(board.getCell(casa).hasEffect());
        assertTrue(board.isFirewall(casa));
        // Antes da pergunta, o tabuleiro devolve a própria casa.
        assertEquals(casa, board.applyEffect(casa));
    }

    @Test
    void efeitoAplicaSeUmaSoVez() {
        // 4 -> 13; aplicar de novo a partir de 13 não muda nada (13 é normal)
        int primeira = board.applyEffect(4);
        assertEquals(13, primeira);
        assertEquals(13, board.applyEffect(primeira));
    }

    @Test
    void casasNormaisNaoMudamAPosicao() {
        assertEquals(3, board.applyEffect(3));
        assertEquals(47, board.applyEffect(47));
        assertEquals(50, board.applyEffect(50));
        assertEquals(0, board.applyEffect(0));
    }

    @Test
    void tiposDeCasaCorretos() {
        assertInstanceOf(AdvanceCell.class, board.getCell(4));
        assertInstanceOf(NormalCell.class, board.getCell(5));
        ReturnCell fw = (ReturnCell) board.getCell(11);
        ReturnCell hp = (ReturnCell) board.getCell(18);
        assertEquals(ReturnCell.Kind.FIREWALL, fw.getKind());
        assertEquals(ReturnCell.Kind.HONEYPOT, hp.getKind());
    }

    // ---------- movimento básico (casa de chegada + efeito) ----------

    @ParameterizedTest(name = "de {0} com dado {1} fica em {2}")
    @CsvSource({
        "0,3,3",    // movimento normal
        "1,3,13",   // brecha 4 -> 13
        "9,2,11",   // firewall 11: fica à espera da pergunta
        "14,4,6",   // honeypot 18 -> 6
        "47,3,50"   // chegada exata ao Root
    })
    void movimentoSimples(int partida, int dado, int esperado) {
        Player p = new Player("Teste", "verde");
        p.moveTo(partida);
        p.moveTo(board.applyEffect(p.getPosition() + dado));
        assertEquals(esperado, p.getPosition());
    }

    // ---------- casas partilhadas ----------

    @Test
    void varioPeoesPodemPartilharAMesmaCasa() {
        Player a = new Player("Ana", "verde");
        Player b = new Player("Rui", "azul");
        a.moveTo(23);
        b.moveTo(23);
        assertEquals(a.getPosition(), b.getPosition());
        assertEquals(23, board.applyEffect(a.getPosition()));
        assertEquals(23, board.applyEffect(b.getPosition()));
    }

    @Test
    void doisPeoesNaMesmaCasaDeEfeitoVaoParaOMesmoDestino() {
        List<Player> jogadores = List.of(new Player("Ana", "verde"), new Player("Rui", "azul"));
        for (Player p : jogadores) {
            p.moveTo(board.applyEffect(4));
        }
        assertEquals(13, jogadores.get(0).getPosition());
        assertEquals(13, jogadores.get(1).getPosition());
    }

    // ---------- Player ----------

    @Test
    void jogadorComecaForaDoTabuleiro() {
        assertEquals(0, new Player("Ana", "verde").getPosition());
    }

    @Test
    void jogadorRejeitaPosicaoInvalida() {
        Player p = new Player("Ana", "verde");
        assertThrows(IllegalArgumentException.class, () -> p.moveTo(-1));
        assertThrows(IllegalArgumentException.class, () -> p.moveTo(51));
        assertEquals(0, p.getPosition());
    }

    @Test
    void reiniciarPoeOPeaoNaPosicaoZero() {
        Player p = new Player("Ana", "verde");
        p.moveTo(30);
        p.reset();
        assertEquals(0, p.getPosition());
    }

    @Test
    void jogadorExigeNomeECor() {
        assertThrows(IllegalArgumentException.class, () -> new Player(" ", "verde"));
        assertThrows(IllegalArgumentException.class, () -> new Player("Ana", null));
    }

    // ---------- validação das casas ----------

    @Test
    void avancoExigeDestinoMaiorQueACasa() {
        assertThrows(IllegalArgumentException.class, () -> new AdvanceCell(10, "x", 5));
    }

    @Test
    void recuoExigeDestinoMenorQueACasa() {
        assertThrows(IllegalArgumentException.class,
                () -> new ReturnCell(10, "x", ReturnCell.Kind.FIREWALL, 12));
    }
}
