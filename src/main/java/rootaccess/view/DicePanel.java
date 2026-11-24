// Módulo: rootaccess.view.DicePanel
// Propósito: dado único e interativo. Só roda depois de um clique e mostra
//            claramente a face sorteada antes de o turno avançar.
// Aluno: C - Cleiton Muangala (30016337)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.view;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntConsumer;

public class DicePanel extends JPanel {

    // Quantas faces falsas passam antes de o resultado ficar parado.
    private static final int SPIN_TICKS = 16;
    // Ritmo do giro: 65 ms dá uma leitura clara sem tornar a espera longa.
    private static final int SPIN_TICK_MS = 65;
    // Pausa final para todos verem a face antes de o avatar arrancar.
    private static final int REVEAL_PAUSE_MS = 480;

    private static final Color DIE_DARK = new Color(9, 17, 24);
    private static final Color DIE_LIGHT = new Color(22, 47, 61);
    private static final Color DIE_BORDER = new Color(0, 210, 170);
    private static final Color DIE_BORDER_IDLE = new Color(28, 92, 110);
    private static final Color PIP_COLOR = new Color(226, 245, 240);
    private static final Color TEXT_GREEN = new Color(0, 230, 140);
    private static final Color TEXT_CYAN = new Color(90, 200, 220);

    // Recebe a face final; o painel de controlo transforma-a num turno.
    private final IntConsumer onRolled;
    // Timer que troca as faces durante o giro.
    private final Timer spinTimer;
    // Timer curto que segura a face final antes de avisar o controlo.
    private final Timer revealTimer;

    // 0 significa "ainda não houve lançamento nesta partida".
    private int face;
    // Contador de ticks do giro, usado também na inclinação visual.
    private int spinTick;
    // Evita dois cliques seguidos enquanto o dado roda.
    private boolean spinning;
    // A GUI desliga o dado durante animações e no fim do jogo.
    private boolean interactionEnabled = true;

    public DicePanel(IntConsumer onRolled) {
        // O painel não está completo sem quem trate do resultado.
        if (onRolled == null) {
            throw new IllegalArgumentException("O dado precisa de um recetor para o resultado");
        }
        this.onRolled = onRolled;
        // O fundo do cartão é desenhado pelo painel pai.
        setOpaque(false);
        // Espaço suficiente para o cubo, o brilho e a legenda.
        setPreferredSize(new Dimension(210, 205));
        // O cursor indica que o objeto é clicável.
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Permite lançar com a barra de espaços ou Enter.
        setFocusable(true);
        // Texto de ajuda nativo do Swing.
        setToolTipText("Clica no dado para o lançar");
        // Nome acessível para leitores de ecrã.
        getAccessibleContext().setAccessibleName("Dado de seis faces");

        // Só mousePressed inicia o giro: não existe reação a hover.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                // O botão esquerdo é o gesto normal de lançamento.
                if (SwingUtilities.isLeftMouseButton(event)) {
                    // Dar foco torna o teclado imediatamente utilizável.
                    requestFocusInWindow();
                    // O clique é o único gatilho do giro.
                    startSpin();
                }
            }
        });

        // Alternativa sem rato, útil em portáteis e para acessibilidade.
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                // Espaço e Enter lançam o dado, tal como o clique.
                if (event.getKeyCode() == KeyEvent.VK_SPACE
                        || event.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Reutiliza o mesmo caminho do clique.
                    startSpin();
                }
            }
        });

        // Timer do giro; o trabalho pesado é apenas trocar a face.
        this.spinTimer = new Timer(SPIN_TICK_MS, event -> onSpinTick());
        // O timer final não se repete: dispara uma vez e para.
        this.revealTimer = new Timer(REVEAL_PAUSE_MS, event -> finishReveal());
        this.revealTimer.setRepeats(false);
    }

    // Liga ou desliga o dado sem esconder a última face.
    public void setInteractionEnabled(boolean enabled) {
        // Guarda o estado para o próximo clique ser aceite ou recusado.
        this.interactionEnabled = enabled;
        // O cursor volta ao normal quando o dado está bloqueado.
        setCursor(Cursor.getPredefinedCursor(
                enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        // Repinta para a borda refletir o estado.
        repaint();
    }

    // Prepara o dado para uma nova partida.
    public void reset() {
        // Para qualquer animação que tenha ficado a meio.
        spinTimer.stop();
        revealTimer.stop();
        // Limpa face e contadores para o próximo jogo.
        spinning = false;
        spinTick = 0;
        face = 0;
        // Mostra novamente o estado "clica no dado".
        repaint();
    }

    // Inicia o giro apenas quando o painel está disponível.
    private void startSpin() {
        // Ignora cliques repetidos, teclas durante o giro e estados bloqueados.
        if (!interactionEnabled || spinning) {
            return;
        }
        // Marca o início antes de arrancar o timer.
        spinning = true;
        spinTick = 0;
        // A primeira face falsa evita um "salto" visual no primeiro tick.
        face = randomFace(face);
        // Som curto confirma que o clique foi aceite.
        SoundEffects.playClick();
        // Começa a sequência de faces.
        spinTimer.start();
        // Redesenha já para mostrar a primeira face.
        repaint();
    }

    // Cada tick troca a face, inclusive a última antes da pausa.
    private void onSpinTick() {
        // Avança o contador que controla a inclinação.
        spinTick++;
        // Uma face diferente da anterior dá a sensação de rotação.
        face = randomFace(face);
        // Atualiza o cubo no ecrã.
        repaint();
        // Ao atingir o número de ticks, termina o giro.
        if (spinTick >= SPIN_TICKS) {
            // O giro termina no tick atual; a face já está escolhida.
            spinTimer.stop();
            // A face final fica visível durante a pausa.
            revealTimer.restart();
        }
    }

    // Fecha o lançamento e entrega a face ao controlador do turno.
    private void finishReveal() {
        // O dado deixa de estar em movimento.
        spinning = false;
        // Som de chegada da face, distinto do clique.
        SoundEffects.playDiceReveal();
        // O número fica parado no ecrã.
        repaint();
        // Só depois da pausa o turno avança, para todos verem o resultado.
        onRolled.accept(face);
    }

    // Sorteia uma face diferente da anterior, para o giro nunca parecer parado.
    private int randomFace(int previous) {
        // O intervalo é inclusivo em ambos os extremos.
        int next = ThreadLocalRandom.current().nextInt(1, 7);
        // Uma repetição isolada é aceitável, mas evitamos a imediata.
        while (next == previous) {
            // Nova tentativa; termina sempre porque há cinco faces diferentes.
            next = ThreadLocalRandom.current().nextInt(1, 7);
        }
        // Devolve a face escolhida.
        return next;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        // Pinta o fundo transparente do painel.
        super.paintComponent(graphics);
        // Copia o contexto para não contaminar o desenho do painel pai.
        Graphics2D g2 = (Graphics2D) graphics.create();
        // Suaviza pips e cantos arredondados.
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Lado do cubo: deixa margem para o brilho e para a legenda inferior.
        int size = Math.min(getWidth() - 46, getHeight() - 62);
        // Nunca deixa o cubo desaparecer em janelas muito pequenas.
        size = Math.max(96, size);
        // Coordenadas que centram o cubo horizontalmente.
        int x = (getWidth() - size) / 2;
        // A legenda ocupa os 30 px de baixo.
        int y = (getHeight() - 30 - size) / 2;

        // Durante o giro inclina ligeiramente o cubo; parado fica direito.
        AffineTransform original = g2.getTransform();
        if (spinning) {
            // Centro do cubo para rodar em torno dele próprio.
            double centerX = x + size / 2.0;
            double centerY = y + size / 2.0;
            // Oscilação suave entre ticks.
            double angle = Math.sin(spinTick * 0.75) * 0.10;
            // Translada, roda e volta a transladar.
            g2.rotate(angle, centerX, centerY);
        }

        // Sombra deslocada dá profundidade ao cubo.
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRoundRect(x + 5, y + 7, size, size, 26, 26);

        // Corpo com gradiente escuro, coerente com o terminal do jogo.
        g2.setPaint(new GradientPaint(x, y, DIE_LIGHT, x + size, y + size, DIE_DARK));
        g2.fillRoundRect(x, y, size, size, 26, 26);

        // Borda viva durante o giro e no resultado; discreta antes do primeiro lançamento.
        Color border = (spinning || face > 0) ? DIE_BORDER : DIE_BORDER_IDLE;
        g2.setColor(border);
        g2.setStroke(new BasicStroke(2.4f));
        g2.drawRoundRect(x, y, size, size, 26, 26);

        // Pips ou interrogação, conforme exista face.
        if (face == 0) {
            drawQuestionMark(g2, x, y, size);
        } else {
            drawPips(g2, x, y, size, face);
        }

        // Termina a rotação antes de escrever a legenda.
        g2.setTransform(original);
        drawCaption(g2);
        // Liberta a cópia do contexto gráfico.
        g2.dispose();
    }

    // Desenha os pips da face atual na grelha 3x3 do cubo.
    private void drawPips(Graphics2D g2, int x, int y, int size, int value) {
        // Raio dos pontos proporcional ao cubo.
        int radius = Math.max(6, size / 11);
        // Coordenadas dos três alinhamentos possíveis.
        int left = x + size / 4;
        int middle = x + size / 2;
        int right = x + 3 * size / 4;
        int top = y + size / 4;
        int center = y + size / 2;
        int bottom = y + 3 * size / 4;

        // Cada face usa os pontos convencionais de um dado de seis lados.
        switch (value) {
            case 1 -> {
                // Um ponto ao centro.
                g2.setColor(PIP_COLOR);
                g2.fillOval(middle - radius, center - radius, radius * 2, radius * 2);
            }
            case 2 -> {
                // Diagonal principal.
                paintPip(g2, left, top, radius);
                paintPip(g2, right, bottom, radius);
            }
            case 3 -> {
                // Diagonal principal mais o centro.
                paintPip(g2, left, top, radius);
                paintPip(g2, middle, center, radius);
                paintPip(g2, right, bottom, radius);
            }
            case 4 -> {
                // Quatro cantos.
                paintPip(g2, left, top, radius);
                paintPip(g2, right, top, radius);
                paintPip(g2, left, bottom, radius);
                paintPip(g2, right, bottom, radius);
            }
            case 5 -> {
                // Quatro cantos e o centro.
                paintPip(g2, left, top, radius);
                paintPip(g2, right, top, radius);
                paintPip(g2, middle, center, radius);
                paintPip(g2, left, bottom, radius);
                paintPip(g2, right, bottom, radius);
            }
            case 6 -> {
                // Duas colunas de três pontos.
                paintPip(g2, left, top, radius);
                paintPip(g2, right, top, radius);
                paintPip(g2, left, center, radius);
                paintPip(g2, right, center, radius);
                paintPip(g2, left, bottom, radius);
                paintPip(g2, right, bottom, radius);
            }
            default -> {
                // Valor impossível: não desenha nada em vez de rebentar.
            }
        }
    }

    // Atalho para não repetir a cor e o preenchimento de cada pip.
    private void paintPip(Graphics2D g2, int centerX, int centerY, int radius) {
        // Cor clara, com bom contraste no fundo escuro.
        g2.setColor(PIP_COLOR);
        // Círculo centrado na coordenada recebida.
        g2.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    // Estado inicial: um "?" grande até haver o primeiro lançamento.
    private void drawQuestionMark(Graphics2D g2, int x, int y, int size) {
        // Fonte proporcional ao cubo para o símbolo não ficar perdido.
        g2.setFont(new Font("Monospaced", Font.BOLD, Math.max(34, size / 3)));
        // Cor neutra enquanto não existe resultado.
        g2.setColor(TEXT_CYAN);
        // Medição para centrar o texto com precisão.
        FontMetrics metrics = g2.getFontMetrics();
        // Texto "?" centrado horizontal e verticalmente.
        g2.drawString("?", x + (size - metrics.stringWidth("?")) / 2,
                y + (size - metrics.getHeight()) / 2 + metrics.getAscent());
    }

    // Legenda com instrução ou resultado, sempre fora do cubo.
    private void drawCaption(Graphics2D g2) {
        // Texto e cor mudam com o estado do dado.
        String caption;
        if (spinning) {
            // Durante o giro a legenda explica o que está a acontecer.
            caption = "A RODAR...";
            g2.setColor(TEXT_CYAN);
        } else if (face == 0) {
            // Antes do primeiro clique, o convite ao gesto.
            caption = "CLICA NO DADO";
            g2.setColor(TEXT_CYAN);
        } else {
            // Resultado explícito, além dos pips.
            caption = "SAIU " + face;
            g2.setColor(TEXT_GREEN);
        }
        // Fonte monoespaçada para combinar com o terminal.
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        // Medição para centrar a legenda na largura do painel.
        FontMetrics metrics = g2.getFontMetrics();
        // Linha de base a 22 px do fundo.
        g2.drawString(caption, (getWidth() - metrics.stringWidth(caption)) / 2,
                getHeight() - 16);
    }
}
