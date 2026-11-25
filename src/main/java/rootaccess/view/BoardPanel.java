// Módulo: rootaccess.view.BoardPanel
// Propósito: desenha o tabuleiro em serpentina, os avatares e as animações
//            de percurso, efeito e chegada.
// Aluno: C - Cleiton Muangala (30016337)
// Revisor: [a preencher]
// Uso de IA: Sim - Claude Code: correção da animação depois da resposta à firewall,
//            peão retido durante a revelação do dado, legenda e avatares sem sobreposição
//            (ver AI_USE.md, secção 3)
package rootaccess.view;

import rootaccess.model.AdvanceCell;
import rootaccess.model.Avatar;
import rootaccess.model.Board;
import rootaccess.model.Cell;
import rootaccess.model.Game;
import rootaccess.model.Player;
import rootaccess.model.ReturnCell;
import rootaccess.model.TurnEvent;
import rootaccess.model.Zone;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
    // 340 ms por casa: dá tempo de seguir o percurso sem parecer travado.
    private static final int STEP_DELAY_MS = 340;
    // Pequeno atraso antes da primeira casa, para o jogador perceber o arranque.
    private static final int STEP_INITIAL_DELAY_MS = 150;
    // Tempo de pausa na casa de efeito antes do salto para o destino.
    private static final int EFFECT_PAUSE_MS = 520;
    // Animação de chegada: 24 frames a 40 ms dão cerca de um segundo.
    private static final int ARRIVAL_FRAME_MS = 40;
    private static final int ARRIVAL_FRAMES = 24;

    private Game game;
    // Timers separados por fase: passo, pausa de efeito e chegada.
    private Timer stepTimer;
    private Timer effectTimer;
    private Timer arrivalTimer;
    private Player movingPlayer;
    private List<Integer> movementPath = Collections.emptyList();
    private int movementIndex;
    private int animatedPosition;
    private int arrivalFrame;
    private int highlightedCell = -1;

    public BoardPanel(Game game) {
        // O jogo é fornecido pelo MainWindow e pode ser trocado no reinício.
        this.game = game;
        setBackground(BG_BOARD);
        setBorder(new LineBorder(BORDER_PANEL, 1, true));
        setPreferredSize(new Dimension(820, 620));
    }

    public void setGame(Game game) {
        // Uma partida nova nunca deve herdar uma animação da anterior.
        stopAnimation();
        this.game = game;
        repaint();
    }

    // O modelo já atualizou a posição; enquanto o dado é revelado, o avatar
    // continua desenhado na casa de partida para não antecipar o resultado.
    public void holdAtStart(TurnEvent event) {
        stopAnimation();
        movingPlayer = event.getPlayer();
        animatedPosition = startPosition(event);
        repaint();
    }

    public void animateTurn(TurnEvent event, Runnable onStep, Runnable onEffect, Runnable onFinished) {
        // Cancela qualquer animação antiga antes de começar esta.
        stopAnimation();
        movingPlayer = event.getPlayer();
        animatedPosition = startPosition(event);
        // A resposta à firewall não volta a percorrer o lançamento: o avatar
        // já está na firewall e só falta mostrar o avanço de 1 casa (ou nada).
        movementPath = isFirewallAnswer(event)
                ? Collections.<Integer>emptyList()
                : createMovementPath(event);
        movementIndex = 0;
        arrivalFrame = 0;
        highlightedCell = -1;

        // O passo muda de casa em casa; os sons são disparados no callback onStep.
        stepTimer = new Timer(STEP_DELAY_MS, timerEvent ->
                advanceStep(event, onStep, onEffect, onFinished));
        // O primeiro passo é ligeiramente mais rápido para não arrastar o arranque.
        stepTimer.setInitialDelay(STEP_INITIAL_DELAY_MS);
        stepTimer.start();
        repaint();
    }

    private void advanceStep(TurnEvent event, Runnable onStep, Runnable onEffect, Runnable onFinished) {
        // Enquanto houver casas no percurso, avança uma por tick.
        if (movementIndex < movementPath.size()) {
            animatedPosition = movementPath.get(movementIndex);
            movementIndex++;
            if (onStep != null) {
                onStep.run();
            }
            repaint();
        }

        // O percurso só termina depois de percorrer o dado (ou o ressalto).
        if (movementIndex < movementPath.size()) {
            return;
        }
        // A partir daqui já não há novos passos.
        stepTimer.stop();
        stepTimer = null;

        if (event.getLanded() != event.getFinalPos()) {
            // Mostra a casa de efeito antes de saltar para o destino.
            highlightedCell = animatedPosition;
            arrivalFrame = 0;
            if (onEffect != null) {
                onEffect.run();
            }
            repaint();
            // A pausa deixa ler o efeito e ouvir o respetivo som.
            effectTimer = new Timer(EFFECT_PAUSE_MS, timerEvent -> {
                // O modelo já calculou o destino; a vista apenas o mostra.
                animatedPosition = event.getFinalPos();
                effectTimer = null;
                startArrival(onFinished);
            });
            effectTimer.setRepeats(false);
            effectTimer.start();
        } else {
            // Sem efeito, a chegada começa imediatamente.
            startArrival(onFinished);
        }
    }

    private void startArrival(Runnable onFinished) {
        // A casa final fica destacada durante toda a animação.
        highlightedCell = animatedPosition;
        arrivalFrame = 0;
        // Fanfarra curta para marcar a chegada.
        SoundEffects.playArrival();
        // Timer da animação de chegada, independente do passo.
        arrivalTimer = new Timer(ARRIVAL_FRAME_MS, timerEvent -> {
            arrivalFrame++;
            repaint();
            if (arrivalFrame >= ARRIVAL_FRAMES) {
                // Terminada a animação, o peão passa a ser desenhado na posição real.
                arrivalTimer.stop();
                arrivalTimer = null;
                movingPlayer = null;
                highlightedCell = -1;
                repaint();
                if (onFinished != null) {
                    onFinished.run();
                }
            }
        });
        arrivalTimer.start();
        repaint();
    }

    // Evento emitido por answerFirewall/giveUpFirewall (tem resultado da resposta).
    private static boolean isFirewallAnswer(TurnEvent event) {
        return event.getFirewallCorrect() != null;
    }

    // Casa onde a animação começa: a firewall, numa resposta; a partida, num lançamento.
    private static int startPosition(TurnEvent event) {
        return isFirewallAnswer(event) ? event.getLanded() : event.getFrom();
    }

    private List<Integer> createMovementPath(TurnEvent event) {
        // Constrói a lista de casas visitadas, incluindo o ressalto.
        List<Integer> path = new ArrayList<>();
        int position = event.getFrom();
        for (int step = 0; step < event.getDice(); step++) {
            // Antes do 50 avança; depois do 50 recua no ressalto.
            position = position < Board.SIZE ? position + 1 : position - 1;
            path.add(position);
        }
        return path;
    }

    private void stopAnimation() {
        // Para e esquece os três timers possíveis.
        if (stepTimer != null) {
            stepTimer.stop();
            stepTimer = null;
        }
        if (effectTimer != null) {
            effectTimer.stop();
            effectTimer = null;
        }
        if (arrivalTimer != null) {
            arrivalTimer.stop();
            arrivalTimer = null;
        }
        // Limpa todo o estado visual associado.
        movingPlayer = null;
        movementPath = Collections.emptyList();
        movementIndex = 0;
        arrivalFrame = 0;
        highlightedCell = -1;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        // Sem jogo não há tabuleiro para desenhar.
        if (game == null || game.getBoard() == null) {
            return;
        }

        // Contexto próprio para as definições gráficas não escaparem.
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        drawHeader(g2);
        drawZoneLabelsAndConnections(g2);

        // As casas são sempre desenhadas a partir do modelo.
        Board board = game.getBoard();
        for (int i = 1; i <= Board.SIZE; i++) {
            Cell cell = board.getCell(i);
            if (cell != null) {
                drawCell(g2, cell);
            }
        }

        // As ondas de chegada ficam atrás dos avatares.
        drawArrivalWaves(g2);

        // Desenha cada avatar na posição animada ou na posição real.
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
        // Título do terminal.
        g2.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2.setColor(new Color(0, 230, 180));
        g2.drawString("// ROOT ACCESS", 24, 34);

        // Resumo calculado do modelo, para não ficar desatualizado.
        int effects = game.getBoard().countEffects();
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(new Color(130, 170, 190));
        g2.drawString("> " + Board.SIZE + " casas | " + effects
                + " efeitos | 1 objetivo: Root", 230, 33);

        // Assinatura visual do projeto.
        g2.setColor(new Color(60, 150, 190));
        g2.drawString("Subida ao Servidor", getWidth() - 160, 33);
    }

    private void drawZoneLabelsAndConnections(Graphics2D g2) {
        // Nomes e intervalos das cinco linhas, de cima para baixo.
        String[] zoneNames = {"NÚCLEO", "DATACENTER", "REDE INTERNA", "DMZ", "INTERNET"};
        String[] zoneRanges = {"41-50", "31-40", "21-30", "11-20", "1-10"};
        // Cores correspondentes ao enum Zone.
        Color[] zoneColors = {
                new Color(230, 75, 75),
                new Color(235, 135, 55),
                new Color(225, 185, 65),
                new Color(55, 195, 120),
                new Color(70, 165, 235)
        };

        // Linhas de ligação da serpentina entre casas consecutivas.
        g2.setColor(new Color(30, 85, 105));
        g2.setStroke(new BasicStroke(2f));
        for (int n = 1; n < Board.SIZE; n++) {
            Rectangle first = getCellBounds(n);
            Rectangle second = getCellBounds(n + 1);
            // Liga os centros das duas casas.
            int x1 = first.x + first.width / 2;
            int y1 = first.y + first.height / 2;
            int x2 = second.x + second.width / 2;
            int y2 = second.y + second.height / 2;
            g2.drawLine(x1, y1, x2, y2);
        }

        // Etiquetas das zonas à esquerda, alinhadas com cada linha.
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

    private void drawCell(Graphics2D g2, Cell cell) {
        int num = cell.getNumber();
        Rectangle rect = getCellBounds(num);

        // Fundo da casa.
        g2.setColor(BG_CELL);
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

        // Brilho na casa de destino da animação, pulsando suavemente.
        if (num == highlightedCell) {
            Composite previousComposite = g2.getComposite();
            float glow = 0.34f + 0.16f * (float) Math.sin(arrivalFrame * 0.55);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glow));
            g2.setColor(new Color(0, 255, 180));
            g2.fillRoundRect(rect.x - 3, rect.y - 3,
                    rect.width + 6, rect.height + 6, 14, 14);
            g2.setComposite(previousComposite);
        }

        // A borda segue a zona, mas os efeitos têm prioridade visual.
        Color borderColor = getZoneColor(cell.getZone());
        float strokeWidth = 1.4f;
        if (num == 1 || num == Board.SIZE) {
            borderColor = COLOR_ROOT;
            strokeWidth = 2.4f;
        } else if (cell instanceof AdvanceCell) {
            borderColor = COLOR_ADVANCE;
            strokeWidth = 2.4f;
        } else if (cell instanceof ReturnCell returnCell) {
            // O tipo do modelo decide a cor; não há listas de números na vista.
            borderColor = returnCell.getKind() == ReturnCell.Kind.HONEYPOT
                    ? COLOR_HONEYPOT : COLOR_FIREWALL;
            strokeWidth = 2.4f;
        }

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(strokeWidth));
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

        // Número da casa no canto superior esquerdo.
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.setColor(TEXT_MAIN);
        g2.drawString(String.valueOf(num), rect.x + 8, rect.y + 18);

        // Indicador inferior: início, Root, avanço ou recuo.
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        if (num == 1) {
            g2.setColor(COLOR_ROOT);
            g2.drawString("INÍCIO", rect.x + 8, rect.y + rect.height - 8);
        } else if (num == Board.SIZE) {
            g2.setColor(COLOR_ROOT);
            g2.drawString("⚑ ROOT", rect.x + 8, rect.y + rect.height - 8);
        } else if (cell instanceof AdvanceCell advance) {
            // A chave identifica uma brecha/exploit e a seta recorda o avanço.
            g2.setColor(COLOR_ADVANCE);
            g2.drawString("🔑 ▲" + advance.getDestination(),
                    rect.x + 6, rect.y + rect.height - 8);
        } else if (cell instanceof ReturnCell returnCell) {
            // Honeypot e firewall partilham a seta, mas mudam de ícone e cor.
            boolean honeypot = returnCell.getKind() == ReturnCell.Kind.HONEYPOT;
            g2.setColor(honeypot ? COLOR_HONEYPOT : COLOR_FIREWALL);
            // A firewall já não recua: mostra um ponto de interrogação.
            String text = honeypot
                    ? "🍯 ▼" + returnCell.getDestination()
                    : "🔥 ?";
            g2.drawString(text, rect.x + 6, rect.y + rect.height - 8);
        }
    }

    private void drawArrivalWaves(Graphics2D g2) {
        // Só há ondas durante a animação de chegada.
        if (arrivalFrame <= 0 || highlightedCell < 1) {
            return;
        }
        Rectangle rect = getCellBounds(highlightedCell);
        int centerX = rect.x + rect.width / 2;
        int centerY = rect.y + rect.height / 2;
        // Progresso normalizado de 0 a 1.
        float progress = Math.min(1f, arrivalFrame / (float) ARRIVAL_FRAMES);

        // A cor das ondas acompanha o avatar que está a chegar.
        Color base = COLOR_ROOT;
        if (movingPlayer != null) {
            int index = Math.max(0, game.getPlayers().indexOf(movingPlayer));
            base = getPlayerColor(movingPlayer.getColor(), index);
        }

        // Três anéis desfasados criam uma onda contínua.
        for (int ring = 0; ring < 3; ring++) {
            float local = progress * 1.5f - ring * 0.18f;
            if (local <= 0f || local >= 1f) {
                continue;
            }
            int radius = (int) (10 + local * 44);
            int alpha = Math.max(0, (int) (150 * (1f - local)));
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
            g2.setStroke(new BasicStroke(2.2f));
            g2.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }

        // Pequenas faíscas a afastar-se do centro.
        for (int spark = 0; spark < 8; spark++) {
            double angle = (2 * Math.PI * spark / 8) + progress * 1.4;
            int distance = (int) (10 + progress * 42);
            int dotX = centerX + (int) (Math.cos(angle) * distance);
            int dotY = centerY + (int) (Math.sin(angle) * distance);
            int dotSize = Math.max(1, (int) (5 * (1f - progress)));
            int alpha = Math.max(0, (int) (190 * (1f - progress)));
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
            g2.fillOval(dotX - dotSize / 2, dotY - dotSize / 2, dotSize, dotSize);
        }
    }

    private void drawPawn(Graphics2D g2, Player player) {
        // Durante o movimento a posição vem da animação; depois vem do modelo.
        int position = player == movingPlayer ? animatedPosition : player.getPosition();
        // A posição 0 fica fora do tabuleiro e não é desenhada.
        if (position < 1 || position > Board.SIZE) {
            return;
        }

        Rectangle rect = getCellBounds(position);
        List<Player> players = game.getPlayers();
        int index = Math.max(0, players.indexOf(player));

        // Fase de chegada: salto curto e crescimento do avatar.
        float progress = arrivalFrame / (float) ARRIVAL_FRAMES;
        int lift = 0;
        double scale = 1.0;
        if (player == movingPlayer && arrivalFrame > 0) {
            // Uma só meia onda: sobe e desce dentro do tempo da animação.
            double wave = Math.sin(progress * Math.PI);
            lift = (int) Math.round(wave * 14);
            scale = 1.0 + wave * 0.35;
        }

        // Grelha 2x2 centrada na casa, para até quatro avatares sem se sobreporem
        // nem taparem o número (em cima) ou o ícone do efeito (em baixo).
        int offsetX = rect.x + (rect.width - 38) / 2 + (index % 2) * 20;
        int offsetY = rect.y + (rect.height - 38) / 2 + (index / 2) * 20 - lift;
        int badge = (int) Math.round(18 * scale);

        // Sombra no chão, que fica para trás quando o avatar salta.
        if (lift > 0) {
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillOval(offsetX + 2, offsetY + lift + badge - 3, badge, 6);
        }

        // Círculo na cor do jogador para distinguir avatares parecidos.
        Color pawnColor = getPlayerColor(player.getColor(), index);
        g2.setColor(pawnColor);
        g2.fillOval(offsetX, offsetY, badge, badge);
        // Contorno branco leve para destacar do fundo da casa.
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(offsetX, offsetY, badge, badge);

        // Glifo do avatar centrado no círculo.
        Avatar avatar = player.getAvatar();
        String glyph = avatar == null ? "" : avatar.getGlyph();
        // Fallback para sistemas sem a fonte de emoji: usa a inicial do nome.
        if (glyph.isBlank()) {
            glyph = player.getName().substring(0, 1).toUpperCase(Locale.ROOT);
        }
        int fontSize = Math.max(10, (int) Math.round(13 * scale));
        g2.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        FontMetrics metrics = g2.getFontMetrics();
        int glyphX = offsetX + (badge - metrics.stringWidth(glyph)) / 2;
        int glyphY = offsetY + (badge - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.setColor(Color.WHITE);
        g2.drawString(glyph, glyphX, glyphY);
    }

    private void drawLegend(Graphics2D g2) {
        int y = getHeight() - 28;
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        FontMetrics metrics = g2.getFontMetrics();
        // Cada entrada começa depois da anterior, medida pela largura real do texto
        // (com posições fixas, "Firewall" e "Honeypot" sobrepunham-se).
        String[] labels = {"▲ Brecha / exploit (avança)", "🔥 Firewall (pergunta)",
                "▼ Honeypot (recua)", "⚑ Root (casa 50)"};
        Color[] colors = {COLOR_ADVANCE, COLOR_FIREWALL, COLOR_HONEYPOT, COLOR_ROOT};
        int x = 130;
        for (int i = 0; i < labels.length; i++) {
            g2.setColor(colors[i]);
            g2.drawString(labels[i], x, y);
            x += metrics.stringWidth(labels[i]) + 28;
        }
    }

    private Rectangle getCellBounds(int cellNumber) {
        // Margens que reservam espaço para etiquetas e legenda.
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

        // A linha 0 é o topo (41-50) e a linha 4 é a base (1-10).
        int rowFromBottom = (cellNumber - 1) / COLS;
        int row = (ROWS - 1) - rowFromBottom;
        int posInRow = (cellNumber - 1) % COLS;

        // Linhas pares (0, 2, 4) crescem da esquerda para a direita; ímpares ao contrário.
        int col = (rowFromBottom % 2 == 0) ? posInRow : (COLS - 1 - posInRow);

        int x = leftMargin + col * (cellW + gapX);
        int y = topMargin + row * (cellH + gapY);
        return new Rectangle(x, y, cellW, cellH);
    }

    private Color getZoneColor(Zone zone) {
        // Cinzento-azulado seguro para zonas desconhecidas.
        if (zone == null) {
            return new Color(45, 110, 140);
        }
        // Cores fixas por zona, iguais às etiquetas laterais.
        return switch (zone) {
            case INTERNET -> new Color(70, 165, 235);
            case DMZ -> new Color(55, 195, 120);
            case REDE_INTERNA -> new Color(225, 185, 65);
            case DATACENTER -> new Color(235, 135, 55);
            case NUCLEO -> new Color(230, 75, 75);
        };
    }

    private Color getDefaultPlayerColor(int index) {
        // Paleta de recurso para cores fora do catálogo.
        Color[] palette = {
                new Color(0, 255, 140),
                new Color(0, 195, 255),
                new Color(255, 210, 50),
                new Color(255, 90, 200)
        };
        return palette[Math.floorMod(index, palette.length)];
    }

    private Color getPlayerColor(String colorName, int index) {
        // Nome em falta: usa a paleta por posição.
        if (colorName == null) {
            return getDefaultPlayerColor(index);
        }
        // Os nomes vêm do modelo em português.
        switch (colorName.toLowerCase(Locale.ROOT)) {
            case "verde":
                return new Color(0, 255, 140);
            case "azul":
                return new Color(0, 195, 255);
            case "amarelo":
                return new Color(255, 210, 50);
            case "rosa":
                return new Color(255, 90, 200);
            default:
                return getDefaultPlayerColor(index);
        }
    }
}
