// Módulo: rootaccess.model.Board
// Propósito: tabuleiro com as 50 casas (15 com efeito). Constrói as casas,
// valida que nenhum destino é casa de efeito e aplica o efeito
// de uma casa uma só vez.
// Aluno: A - Igor Santos (30016495)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board {

    public static final int SIZE = 50;

    private static final String[] NAMES = {
        // Internet (1-10)
        "Internet", "Router doméstico", "Wi-Fi do café", "Exploit encontrado", "Fórum anónimo",
        "Servidor proxy", "DNS público", "Credenciais fracas", "Rede Tor", "Gateway da NexaCorp",
        // DMZ (11-20)
        "Firewall perimetral", "Servidor web", "Formulário de login", "API exposta", "Servidor de email",
        "Vulnerabilidade zero-day", "Balanceador de carga", "Honeypot da DMZ", "Servidor FTP", "VPN de acesso remoto",
        // Rede Interna (21-30)
        "Portátil do estagiário", "Privilege escalation", "Impressora de rede", "Partilha de ficheiros", "Firewall interna",
        "Intranet", "Servidor de impressão", "Pass-the-hash", "Switch interno", "Honeypot interno",
        // Datacenter (31-40)
        "Base de dados", "SQL injection", "Cluster de backups", "Servidor de logs", "Firewall de aplicação",
        "Kerberoasting", "Hipervisor", "Servidor de aplicações", "Rack de armazenamento", "Honeypot do datacenter",
        // Núcleo (41-50)
        "Golden ticket", "Cofre de chaves", "Controlador de domínio", "Firewall do núcleo", "Consola de administração",
        "Servidor de certificados", "Servidor de auditoria", "Gestor de segredos", "Porta do Root", "Root"
    };

    // {casa, destino}
    private static final int[][] ADVANCES = {
        {4, 13}, {8, 21}, {16, 27}, {22, 34}, {28, 39}, {32, 43}, {36, 46}, {41, 48}
    };
    private static final int[][] FIREWALLS = {{11, 2}, {25, 14}, {35, 24}, {44, 33}};
    private static final int[][] HONEYPOTS = {{18, 6}, {30, 19}, {40, 29}};

    private final List<Cell> cells = new ArrayList<>();

    public Board() {
        for (int n = 1; n <= SIZE; n++) {
            cells.add(createCell(n));
        }
        validate();
    }

    private static Cell createCell(int n) {
        String name = NAMES[n - 1];
        for (int[] a : ADVANCES) {
            if (a[0] == n) return new AdvanceCell(n, name, a[1]);
        }
        for (int[] f : FIREWALLS) {
            if (f[0] == n) return new ReturnCell(n, name, ReturnCell.Kind.FIREWALL, f[1]);
        }
        for (int[] h : HONEYPOTS) {
            if (h[0] == n) return new ReturnCell(n, name, ReturnCell.Kind.HONEYPOT, h[1]);
        }
        return new NormalCell(n, name);
    }

    // Garante que não há encadeamentos: nenhum destino é casa de efeito.
    private void validate() {
        for (Cell c : cells) {
            if (c instanceof Effect e && getCell(e.getDestination()).hasEffect()) {
                throw new IllegalStateException(
                        "O destino da casa " + c.getNumber() + " é uma casa de efeito");
            }
        }
    }

    // Devolve a casa com o número indicado (1 a 50).
    public Cell getCell(int number) {
        if (number < 1 || number > SIZE) {
            throw new IllegalArgumentException("Casa inexistente: " + number);
        }
        return cells.get(number - 1);
    }

    public List<Cell> getCells() { return Collections.unmodifiableList(cells); }

    // Indica se a casa é uma firewall, que agora funciona como ponto de verificação.
    public boolean isFirewall(int position) {
        // Posições fora do tabuleiro nunca são firewalls.
        if (position < 1 || position > SIZE) {
            return false;
        }
        // O tipo da casa vem do modelo ReturnCell.
        Cell cell = cells.get(position - 1);
        return cell instanceof ReturnCell returnCell
                && returnCell.getKind() == ReturnCell.Kind.FIREWALL;
    }

    // Aplica o efeito da casa uma só vez.
    // As firewalls não recuam automaticamente: o Game trata da pergunta.
    // A posição 0 (fora do tabuleiro) fica como está.
    public int applyEffect(int position) {
        // Fora do tabuleiro não há casa para consultar.
        if (position == 0) {
            return 0;
        }
        // A firewall mantém o peão no sítio até à resposta do jogador.
        if (isFirewall(position)) {
            return position;
        }
        // Avanços e honeypots continuam a redirecionar na hora.
        return getCell(position).redirect();
    }

    public int countEffects() {
        int total = 0;
        for (Cell c : cells) {
            if (c.hasEffect()) total++;
        }
        return total;
    }
}
