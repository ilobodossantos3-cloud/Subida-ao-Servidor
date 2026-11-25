// Módulo: rootaccess.view.ControlPanel
// Propósito: estado do turno, dado único (modo aleatório ou manual), controlos
//            de som, reinício e saída.
// Aluno: C - Cleiton Muangala (30016337)
// Revisor: [a preencher]
// Uso de IA: Sim - Claude Code: modos de dado aleatório/manual com campo de entrada,
//            validação sem alterar o estado e desacoplamento do MainWindow (ver AI_USE.md, secção 3)
package rootaccess.view;

import rootaccess.model.Board;
import rootaccess.model.Game;
import rootaccess.model.InvalidDiceValueException;
import rootaccess.model.ManualDice;
import rootaccess.model.Player;
import rootaccess.model.TurnEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

public class ControlPanel extends JPanel {

    private static final Color BG_DARK = new Color(10, 15, 22);
    private static final Color BG_CARD = new Color(13, 21, 30);
    private static final Color BORDER_CYAN = new Color(25, 95, 110);
    private static final Color TEXT_GREEN = new Color(0, 230, 140);
    private static final Color TEXT_CYAN = new Color(90, 200, 220);
    private static final Color TEXT_ERROR = new Color(255, 110, 110);
    private static final Color TEXT_HINT = new Color(120, 145, 160);

    // Nomes dos cartões do CardLayout com os controlos de cada modo.
    private static final String CARD_RANDOM = "aleatorio";
    private static final String CARD_MANUAL = "manual";

    private Game game;
    private final LogPanel logPanel;
    private final Runnable onRestart;
    private final DicePanel dicePanel;
    private final JLabel currentPlayerLabel;
    private final JTextArea positionsArea;
    private final JButton restartButton;
    private final JCheckBox soundToggle;
    // Controlos do modo aleatório e do modo manual (só um está visível).
    private final JPanel rollControls;
    private final CardLayout rollCards;
    private final JButton randomRollButton;
    private final JTextField manualField;
    private final JButton manualRollButton;
    private final JLabel diceTitle;
    private final JLabel feedbackLabel;
    private boolean animating;
    // true quando o dado do jogo é um ManualDice (o jogador escreve o valor).
    private boolean manualMode;

    public ControlPanel(Game game, LogPanel logPanel, Runnable onRestart) {
        // O painel escreve no registo e pede o reinício a quem o criou.
        if (logPanel == null || onRestart == null) {
            throw new IllegalArgumentException("O painel precisa do registo e da ação de reinício");
        }
        this.game = game;
        this.logPanel = logPanel;
        this.onRestart = onRestart;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG_DARK);

        // Cartão com o jogador que tem a vez.
        currentPlayerLabel = new JLabel("> -");
        currentPlayerLabel.setFont(new Font("Monospaced", Font.BOLD, 15));
        currentPlayerLabel.setForeground(TEXT_GREEN);
        add(createInfoCard(cardTitle("Jogador Atual"), currentPlayerLabel));
        add(Box.createVerticalStrut(8));

        // Dado único: o cubo mostra sempre o valor que o modelo usou.
        dicePanel = new DicePanel(this::onDieClicked);
        dicePanel.setPreferredSize(new Dimension(210, 150));

        // Modo aleatório: botão explícito (o clique no cubo faz o mesmo).
        randomRollButton = createStyledButton("Lançar dado (aleatório)");
        randomRollButton.addActionListener(event -> rollRandom());
        JPanel randomCard = new JPanel(new BorderLayout());
        randomCard.setBackground(BG_CARD);
        randomCard.add(randomRollButton, BorderLayout.CENTER);

        // Modo manual: campo de texto validado pelo ManualDice do modelo.
        manualField = new JTextField(3);
        manualField.setFont(new Font("Monospaced", Font.BOLD, 14));
        manualField.setToolTipText("Escreve um inteiro de 1 a 6 e carrega em Enter");
        manualField.getAccessibleContext().setAccessibleName("Valor do dado (1 a 6)");
        // Enter no campo é o mesmo que carregar no botão.
        manualField.addActionListener(event -> rollManual());
        manualRollButton = createStyledButton("Lançar");
        manualRollButton.addActionListener(event -> rollManual());
        manualRollButton.setMaximumSize(null);
        JLabel manualLabel = new JLabel("Valor (1-6):");
        manualLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        manualLabel.setForeground(TEXT_CYAN);
        JPanel manualCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        manualCard.setBackground(BG_CARD);
        manualCard.add(manualLabel);
        manualCard.add(manualField);
        manualCard.add(manualRollButton);

        rollCards = new CardLayout();
        rollControls = new JPanel(rollCards);
        rollControls.setBackground(BG_CARD);
        rollControls.add(randomCard, CARD_RANDOM);
        rollControls.add(manualCard, CARD_MANUAL);

        // Linha de mensagens: dicas do modo e erros de entrada.
        feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(new Font("SansSerif", Font.BOLD, 11));

        JPanel diceBox = new JPanel();
        diceBox.setLayout(new BoxLayout(diceBox, BoxLayout.Y_AXIS));
        diceBox.setBackground(BG_CARD);
        for (JComponent part : new JComponent[]{dicePanel, rollControls, feedbackLabel}) {
            part.setAlignmentX(Component.LEFT_ALIGNMENT);
            diceBox.add(part);
        }
        // O título do cartão indica o modo escolhido na configuração.
        diceTitle = cardTitle("Dado");
        add(createInfoCard(diceTitle, diceBox));
        add(Box.createVerticalStrut(8));

        // Lista das posições, agora com o avatar de cada jogador.
        positionsArea = new JTextArea(4, 20);
        positionsArea.setEditable(false);
        positionsArea.setBackground(BG_CARD);
        positionsArea.setForeground(TEXT_CYAN);
        positionsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(createInfoCard(cardTitle("Posição Atual (Casas 0-50)"), positionsArea));
        add(Box.createVerticalStrut(8));

        // Cartão de controlos gerais.
        JPanel controlsBox = new JPanel();
        controlsBox.setLayout(new BoxLayout(controlsBox, BoxLayout.Y_AXIS));
        controlsBox.setBackground(BG_CARD);
        controlsBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CYAN, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel controlsTitle = new JLabel("Controlos");
        controlsTitle.setFont(new Font("Monospaced", Font.BOLD, 13));
        controlsTitle.setForeground(TEXT_CYAN);
        controlsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlsBox.add(controlsTitle);
        controlsBox.add(Box.createVerticalStrut(8));

        // O som pode ser desligado sem perder o resto da interação.
        soundToggle = new JCheckBox("Sons ligados", true);
        soundToggle.setFont(new Font("Monospaced", Font.PLAIN, 12));
        soundToggle.setForeground(TEXT_CYAN);
        soundToggle.setBackground(BG_CARD);
        soundToggle.setFocusPainted(false);
        soundToggle.addActionListener(event -> {
            // O estado global dos efeitos sonoros acompanha a caixa.
            SoundEffects.setEnabled(soundToggle.isSelected());
            if (soundToggle.isSelected()) {
                // Um clique confirma que o som voltou.
                SoundEffects.playClick();
            }
        });
        controlsBox.add(soundToggle);
        controlsBox.add(Box.createVerticalStrut(8));

        // Reiniciar abre novamente a configuração de jogadores.
        restartButton = createStyledButton("↻ Reiniciar");
        restartButton.addActionListener(event -> {
            // O som confirma a ação antes de abrir o diálogo.
            SoundEffects.playClick();
            this.onRestart.run();
        });
        // Sair encerra a aplicação.
        JButton quitButton = createStyledButton("Sair");
        quitButton.addActionListener(event -> System.exit(0));
        // Os dois botões lado a lado poupam altura para o registo de eventos.
        JPanel buttonsRow = new JPanel(new GridLayout(1, 2, 6, 0));
        buttonsRow.setBackground(BG_CARD);
        buttonsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        buttonsRow.add(restartButton);
        buttonsRow.add(quitButton);
        controlsBox.add(buttonsRow);

        add(controlsBox);
        // Estado inicial coerente com o dado do jogo recebido.
        configureDiceMode();
        refreshState();
    }

    public void setGame(Game game) {
        // Troca o modelo por uma partida nova e limpa o dado.
        this.game = game;
        dicePanel.reset();
        animating = false;
        configureDiceMode();
        clearFeedback();
        manualField.setText("");
        refreshState();
        focusRollControl();
    }

    // Mostra os controlos do modo do dado escolhido na configuração.
    private void configureDiceMode() {
        // O modo é uma propriedade do modelo: o tipo do dado decide-o.
        manualMode = game != null && game.getDice() instanceof ManualDice;
        rollCards.show(rollControls, manualMode ? CARD_MANUAL : CARD_RANDOM);
        dicePanel.setIdleCaption(manualMode ? "ESCREVE 1-6" : "CLICA NO DADO");
        diceTitle.setText(manualMode ? "Dado · modo manual" : "Dado · modo aleatório");
    }

    public void setAnimating(boolean animating, TurnEvent event) {
        // A flag impede lançamentos enquanto o dado e o avatar se mexem.
        this.animating = animating;
        refreshState();
        if (animating && event != null) {
            // Sem revelar o destino antes de o dado e o avatar o mostrarem.
            currentPlayerLabel.setText("> " + event.getPlayer().getAvatar().getGlyph() + " "
                    + event.getPlayer().getName() + " a jogar...");
        } else if (!animating) {
            focusRollControl();
        }
    }

    // Revela no cubo o valor usado pelo modelo e depois corre "then".
    public void showRoll(TurnEvent event, Runnable then) {
        // No modo aleatório o cubo roda; no manual o valor já é conhecido.
        dicePanel.showResult(event.getDice(), !manualMode, then);
    }

    // Clique (ou Espaço/Enter) no cubo.
    private void onDieClicked() {
        if (manualMode) {
            // No modo manual o cubo não sorteia nada: indica o campo de texto.
            showHint("Escreve um valor de 1 a 6 no campo e carrega em Lançar.");
            manualField.requestFocusInWindow();
        } else {
            rollRandom();
        }
    }

    private void rollRandom() {
        // O RandomDice do modelo decide o valor.
        playTurn(null);
    }

    private void rollManual() {
        // O texto vai tal como foi escrito; quem valida é o ManualDice.
        playTurn(manualField.getText());
    }

    // Caminho único para os dois modos: só muda a origem do valor do dado.
    private void playTurn(String manualInput) {
        // Sem jogo ou com animação em curso não há turno para processar.
        if (game == null || animating) {
            return;
        }
        Player current = game.getCurrentPlayer();
        try {
            if (manualInput != null && game.getDice() instanceof ManualDice manual) {
                manual.setInput(manualInput);
            }
            // O turno corre uma única vez por pedido; o GameObserver anima-o.
            game.playTurn();
            clearFeedback();
            manualField.setText("");
        } catch (InvalidDiceValueException ex) {
            // Entrada inválida: o modelo recusou antes de mudar qualquer estado.
            showError(ex.getMessage());
            logPanel.append("ENTRADA INVÁLIDA: " + ex.getMessage()
                    + " O estado do jogo não mudou; continua a vez de "
                    + (current == null ? "-" : current.getName()) + ".");
            SoundEffects.playDenied();
            manualField.selectAll();
            manualField.requestFocusInWindow();
        } catch (IllegalStateException ex) {
            // Jogo terminado, ainda não iniciado ou firewall pendente.
            logPanel.append("AVISO: " + ex.getMessage());
        }
    }

    // Atualiza botões, rótulo do jogador e posições a partir do modelo.
    public void refreshState() {
        // Ainda não há partida: nada para atualizar além do estado base.
        if (game == null) {
            return;
        }

        boolean finished = game.getResult() != null;
        boolean hasPlayers = !game.getPlayers().isEmpty();
        // O dado só responde quando é a vez do jogador, não há animação
        // e nenhuma pergunta de firewall está pendente.
        boolean canRoll = hasPlayers && !finished && !animating && !game.isAwaitingFirewall();
        dicePanel.setInteractionEnabled(canRoll);
        randomRollButton.setEnabled(canRoll);
        manualField.setEnabled(canRoll);
        manualRollButton.setEnabled(canRoll);
        // Reiniciar fica bloqueado apenas durante uma animação.
        restartButton.setEnabled(!animating);

        Player current = game.getCurrentPlayer();
        if (finished) {
            // No fim, o rótulo deixa claro que já não há turnos.
            currentPlayerLabel.setText("> Jogo Terminado");
        } else if (current != null) {
            // Mostra o avatar e a ronda atual.
            currentPlayerLabel.setText(current.getAvatar().getGlyph() + " "
                    + current.getName() + " (Ronda " + game.getRound()
                    + "/" + Game.MAX_ROUNDS + ")");
        } else {
            // Antes de iniciar, o painel fica num estado neutro.
            currentPlayerLabel.setText("> -");
        }

        // Durante a animação as posições só mudam quando o avatar chega.
        if (!animating) {
            refreshPositions();
        }
    }

    // Lista das posições com o avatar de cada jogador (dados do modelo).
    public void refreshPositions() {
        if (game == null) {
            return;
        }
        StringBuilder positions = new StringBuilder();
        List<Player> players = game.getPlayers();
        for (Player player : players) {
            positions.append("> ").append(player.getAvatar().getGlyph()).append(' ')
                    .append(player.getName())
                    .append(": Casa ").append(player.getPosition()).append(" / ")
                    .append(Board.SIZE).append('\n');
        }
        // trim evita uma linha vazia no fim.
        positionsArea.setText(positions.toString().trim());
    }

    private void focusRollControl() {
        // O teclado fica pronto para o próximo lançamento.
        if (manualMode) {
            manualField.requestFocusInWindow();
        } else {
            dicePanel.requestFocusInWindow();
        }
    }

    private void showError(String message) {
        feedbackLabel.setForeground(TEXT_ERROR);
        feedbackLabel.setText("<html>" + escapeHtml(message) + "</html>");
    }

    private void showHint(String message) {
        feedbackLabel.setForeground(TEXT_HINT);
        feedbackLabel.setText("<html>" + escapeHtml(message) + "</html>");
    }

    private void clearFeedback() {
        // No modo manual a linha recorda o que fazer; no aleatório fica vazia.
        if (manualMode) {
            showHint("Escreve 1-6 e carrega em Lançar (ou Enter).");
        } else {
            feedbackLabel.setText(" ");
        }
    }

    private static String escapeHtml(String text) {
        // A entrada do jogador aparece na mensagem; não pode ser lida como HTML.
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // Título pequeno em ciano, igual em todos os cartões.
    private JLabel cardTitle(String text) {
        JLabel titleLabel = new JLabel(text);
        titleLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        titleLabel.setForeground(TEXT_CYAN);
        return titleLabel;
    }

    private JPanel createInfoCard(JLabel titleLabel, JComponent content) {
        // Cartão com título e conteúdo, reutilizado pelos três blocos.
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CYAN, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JButton createStyledButton(String text) {
        // Botão coerente com o terminal: fundo escuro e borda ciano.
        JButton button = new JButton(text);
        button.setFont(new Font("Monospaced", Font.BOLD, 12));
        button.setBackground(new Color(16, 34, 48));
        button.setForeground(TEXT_CYAN);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CYAN, 1, true),
                new EmptyBorder(7, 12, 7, 12)
        ));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}
