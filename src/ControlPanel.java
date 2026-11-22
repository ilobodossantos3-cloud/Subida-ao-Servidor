/* Module and purpose: rootaccess.gui.BoardPanel - Renderização gráfica do tabuleiro em serpentina (50 casas, 5 zonas, efeitos e peões).
- Student name/number and implemented contributions: Cleiton Muangala (30016337) - Implementação de paintComponent, drawCell, drawPawn, cálculo de coordenadas em serpentina e legenda.
- Collaborator/reviewer and contribution: Igor Santos (30016495) - Revisão do mapeamento das casas, zonas e efeitos do modelo.
- AI assistance: Auxílio no cálculo geométrico da grelha em serpentina em Graphics2D (ver AI_USE.md).
 */
package rootaccess.gui;

import rootaccess.model.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class BoardPanel extends JPanel {

    private static final Color BG_BOARD = new Color(8, 13, 20);
    private static final Color BG_CELL = new Color(14, 24, 34);
    private static final Color BORDER_PANEL = new Color(25, 95, 110);
    private static final Color TEXT_MAIN = new Color(220, 235, 245);
    private static final Color COLOR_ADVANCE = new Color(0, 210, 110);
    private static final Color COLOR_FIREWALL = new Color(235, 65, 65);
    private static final Color COLOR_HONEYPOT = new Color(240, 160, 30);
    private static final Color COLOR_ROOT = new Color(40, 160, 255);

    private static final int ROWS = 5;
    private static final int COLS = 10;

    private Game game;

    public BoardPanel(Game game) {
        this.game = game;
        setBackground(BG_BOARD);
        setBorder(new LineBorder(BORDER_PANEL, 1, true));
        setPreferredSize(new Dimension(820, 620));
    }

    public void setGame(Game game) {
        this.game = game;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (game == null || game.getBoard() == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawHeader(g2);
        drawZoneLabelsAndConnections(g2);

        Board board = game.getBoard();
        for (int i = 1; i <= Board.SIZE; i++) {
            Cell cell = board.getCell(i);
            if (cell != null) {
                drawCell(g2, cell);
            }
        }

        List<Player> players = game.getPlayers();
        if (players != null) {
            for (Player player : players) {
                drawPawn(g2, player);
            }
        }

        drawLegend(g2);
        g2.dispose();
    }

    private void drawHeader(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2.setColor(new Color(0, 230, 180));
        g2.drawString("// ROOT ACCESS", 24, 34);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(new Color(130, 170, 190));
        g2.drawString("> 50 casas | 15 efeitos | 1 objetivo: Root", 230, 33);

        g2.setColor(new Color(60, 150, 190));
        g2.drawString("Subida ao Servidor", getWidth() - 160, 33);
    }

    private void drawZoneLabelsAndConnections(Graphics2D g2) {
        String[] zoneNames = {"NÚCLEO", "DATACENTER", "REDE INTERNA", "DMZ", "INTERNET"};
        String[] zoneRanges = {"41-50", "31-40", "21-30", "11-20", "1-10"};
        Color[] zoneColors = {
                new Color(230, 75, 75),   // Núcleo (vermelho)
                new Color(235, 135, 55),  // Datacenter (laranja)
                new Color(225, 185, 65),  // Rede Interna (âmbar)
                new Color(55, 195, 120),  // DMZ (verde)
                new Color(70, 165, 235)   // Internet (azul-frio)
        };

        // Linhas de ligação da serpentina
        g2.setColor(new Color(30, 85, 105));
        g2.setStroke(new BasicStroke(2f));
        for (int n = 1; n < Board.SIZE; n++) {
            Rectangle r1 = getCellBounds(n);
            Rectangle r2 = getCellBounds(n + 1);
            int x1 = r1.x + r1.width / 2;
            int y1 = r1.y + r1.height / 2;
            int x2 = r2.x + r2.width / 2;
            int y2 = r2.y + r2.height / 2;
            g2.drawLine(x1, y1, x2, y2);
        }

        // Etiquetas das 5 zonas à esquerda
        for (int row = 0; row < ROWS; row++) {
            int sampleCell = (row == 0) ? 41 : (row == 1) ? 40 : (row == 2) ? 21 : (row == 3) ? 20 : 1;
            Rectangle rect = getCellBounds(sampleCell);
            int centerY = rect.y + rect.height / 2;

            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.setColor(zoneColors[row]);
            g2.drawString(zoneNames[row], 20, centerY - 2);

            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2.setColor(new Color(120, 145, 160));
            g2.drawString("casas " + zoneRanges[row], 20, centerY + 13);
        }
    }

    private void drawCell(Graphics g, Cell cell) {
        Graphics2D g2 = (Graphics2D) g;
        int num = cell.getNumber();
        Rectangle rect = getCellBounds(num);

        // Fundo da casa
        g2.setColor(BG_CELL);
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

        // Cor da borda conforme zona ou efeito
        Color borderColor = getZoneColor(cell.getZone());
        float strokeWidth = 1.4f;

        if (num == 1 || num == Board.SIZE) {
            borderColor = COLOR_ROOT;
            strokeWidth = 2.4f;
        } else if (cell instanceof AdvanceCell) {
            borderColor = COLOR_ADVANCE;
            strokeWidth = 2.4f;
        } else if (cell instanceof ReturnCell) {
            borderColor = isHoneypot(num) ? COLOR_HONEYPOT : COLOR_FIREWALL;
            strokeWidth = 2.4f;
        }

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(strokeWidth));
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

        // Número da casa
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.setColor(TEXT_MAIN);
        g2.drawString(String.valueOf(num), rect.x + 8, rect.y + 18);

        // Indicador de início, Root ou efeito (Avanço / Recuo)
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        if (num == 1) {
            g2.setColor(COLOR_ROOT);
            g2.drawString("INÍCIO", rect.x + 8, rect.y + rect.height - 8);
        } else if (num == Board.SIZE) {
            g2.setColor(COLOR_ROOT);
            g2.drawString("⚑ ROOT", rect.x + 8, rect.y + rect.height - 8);
        } else if (cell instanceof Effect) {
            Effect effect = (Effect) cell;
            int dest = effect.getDestination();
            if (cell instanceof AdvanceCell) {
                g2.setColor(COLOR_ADVANCE);
                g2.drawString("🔑 ▲" + dest, rect.x + 6, rect.y + rect.height - 8);
            } else {
                boolean honeypot = isHoneypot(num);
                g2.setColor(honeypot ? COLOR_HONEYPOT : COLOR_FIREWALL);
                String icon = honeypot ? "🍯 ▼" : "🔥 ▼";
                g2.drawString(icon + dest, rect.x + 6, rect.y + rect.height - 8);
            }
        }
    }

    private void drawPawn(Graphics g, Player player) {
        int pos = player.getPosition();
        if (pos < 1 || pos > Board.SIZE) {
            return; // Posição 0 está fora do tabuleiro
        }

        Graphics2D g2 = (Graphics2D) g;
        Rectangle rect = getCellBounds(pos);

        List<Player> players = game.getPlayers();
        int index = players.indexOf(player);
        if (index < 0) index = 0;

        // Deslocamento para que até 4 peões partilhem a mesma casa sem se sobreporem
        int offsetX = rect.x + rect.width - 28 + (index % 2) * 12;
        int offsetY = rect.y + 6 + (index / 2) * 12;

        Color pawnColor = player.getColor() != null ? player.getColor() : getDefaultPlayerColor(index);
        g2.setColor(pawnColor);
        g2.fillRect(offsetX, offsetY, 9, 10);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(offsetX, offsetY, 9, 10);
    }

    private void drawLegend(Graphics2D g2) {
        int y = getHeight() - 28;
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));

        g2.setColor(COLOR_ADVANCE);
        g2.drawString("▲ Brecha / exploit (avança)", 130, y);

        g2.setColor(COLOR_FIREWALL);
        g2.drawString("▼ Firewall (recua)", 350, y);

        g2.setColor(COLOR_HONEYPOT);
        g2.drawString("▼ Honeypot (recua)", 505, y);

        g2.setColor(COLOR_ROOT);
        g2.drawString("⚑ Root (casa 50)", 665, y);
    }

    private Rectangle getCellBounds(int cellNumber) {
        int leftMargin = 130;
        int topMargin = 55;
        int bottomMargin = 50;
        int rightMargin = 20;

        int availWidth = Math.max(500, getWidth() - leftMargin - rightMargin);
        int availHeight = Math.max(350, getHeight() - topMargin - bottomMargin);

        int gapX = 10;
        int gapY = 14;
        int cellW = (availWidth - (COLS - 1) * gapX) / COLS;
        int cellH = (availHeight - (ROWS - 1) * gapY) / ROWS;

        // Linha 0 é o topo (41-50), Linha 4 é a base (1-10)
        int rowFromBottom = (cellNumber - 1) / COLS; // 0..4
        int row = (ROWS - 1) - rowFromBottom;
        int posInRow = (cellNumber - 1) % COLS;

        // Linhas pares a contar de baixo (0, 2, 4) vão da esquerda para a direita; ímpares (1, 3) da direita para a esquerda
        int col = (rowFromBottom % 2 == 0) ? posInRow : (COLS - 1 - posInRow);

        int x = leftMargin + col * (cellW + gapX);
        int y = topMargin + row * (cellH + gapY);
        return new Rectangle(x, y, cellW, cellH);
    }

    private boolean isHoneypot(int cellNumber) {
        return cellNumber == 18 || cellNumber == 30 || cellNumber == 40;
    }

    private Color getZoneColor(Zone zone) {
        if (zone == null) return new Color(45, 110, 140);
        switch (zone) {
            case INTERNET:     return new Color(70, 165, 235);
            case DMZ:          return new Color(55, 195, 120);
            case REDE_INTERNA: return new Color(225, 185, 65);
            case DATACENTER:   return new Color(235, 135, 55);
            case NUCLEO:       return new Color(230, 75, 75);
            default:           return new Color(45, 110, 140);
        }
    }

    private Color getDefaultPlayerColor(int index) {
        Color[] palette = {
                new Color(0, 255, 140),
                new Color(0, 195, 255),
                new Color(255, 210, 50),
                new Color(255, 90, 200)
        };
        return palette[index % palette.length];
    }
}