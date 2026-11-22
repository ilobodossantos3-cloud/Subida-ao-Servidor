/* Module and purpose: rootaccess.gui.ControlPanel - Painel de estado do turno e controlos de dados (manual/aleatório), reinício e saída.
- Student name/number and implemented contributions: Cleiton Muangala (30016337) - Implementação de ControlPanel, manualField, onRandomClick, onManualOk e onRestart.
- Collaborator/reviewer and contribution: Pedro Moroni (30016458) - Integração com ManualDice, RandomDice, playTurn e InvalidDiceValueException.
- AI assistance: Auxílio na disposição dos cartões de estado em BoxLayout (ver AI_USE.md).
 */
package rootaccess.gui;

import rootaccess.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class ControlPanel extends JPanel {

    private static final Color BG_DARK = new Color(10, 15, 22);
    private static final Color BG_CARD = new Color(13, 21, 30);
    private static final Color BORDER_CYAN = new Color(25, 95, 110);
    private static final Color TEXT_GREEN = new Color(0, 230, 140);
    private static final Color TEXT_CYAN = new Color(90, 200, 220);
    private static final Color TEXT_WHITE = new Color(230, 240, 245);

    private Game game;
    private final MainWindow mainWindow;
    private final JTextField manualField;

    private final JLabel currentPlayerLabel;
    private final JLabel lastRollLabel;
    private final JTextArea positionsArea;
    private final JButton randomRollButton;
    private final JButton manualOkButton;

    public ControlPanel(Game game, MainWindow mainWindow) {
        this.game = game;
        this.mainWindow = mainWindow;
        this.manualField = new JTextField(4);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG_DARK);

        // Cartão: Jogador Atual
        currentPlayerLabel = new JLabel("> -");
        currentPlayerLabel.setFont(new Font("Monospaced", Font.BOLD, 15));
        currentPlayerLabel.setForeground(TEXT_GREEN);
        add(createInfoCard("Jogador Atual", currentPlayerLabel));
        add(Box.createVerticalStrut(8));

        // Cartão: Último Lançamento
        lastRollLabel = new JLabel("🎲 -");
        lastRollLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        lastRollLabel.setForeground(TEXT_WHITE);
        add(createInfoCard("Último Lançamento", lastRollLabel));
        add(Box.createVerticalStrut(8));

        // Cartão: Posição Atual de cada jogador
        positionsArea = new JTextArea(4, 20);
        positionsArea.setEditable(false);
        positionsArea.setBackground(BG_CARD);
        positionsArea.setForeground(TEXT_CYAN);
        positionsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(createInfoCard("Posição Atual (Casas 0-50)", positionsArea));
        add(Box.createVerticalStrut(8));

        // Cartão: Controlos
        JPanel controlsBox = new JPanel();
        controlsBox.setLayout(new BoxLayout(controlsBox, BoxLayout.Y_AXIS));
        controlsBox.setBackground(BG_CARD);
        controlsBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CYAN, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel controlsTitle = new JLabel("Controles");
        controlsTitle.setFont(new Font("Monospaced", Font.BOLD, 13));
        controlsTitle.setForeground(TEXT_CYAN);
        controlsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlsBox.add(controlsTitle);
        controlsBox.add(Box.createVerticalStrut(8));

        // Botão de lançamento aleatório
        randomRollButton = createStyledButton("🎲 Lançar Dado (Aleatório)");
        randomRollButton.addActionListener(e -> onRandomClick());
        controlsBox.add(randomRollButton);
        controlsBox.add(Box.createVerticalStrut(8));

        // Entrada manual de dado (1 a 6)
        JPanel manualRow = new JPanel(new BorderLayout(6, 0));
        manualRow.setBackground(BG_CARD);
        manualRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        manualRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel manualLabel = new JLabel("Dado (1-6):");
        manualLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        manualLabel.setForeground(TEXT_WHITE);

        manualField.setBackground(new Color(6, 10, 15));
        manualField.setForeground(TEXT_GREEN);
        manualField.setCaretColor(TEXT_GREEN);
        manualField.setFont(new Font("Monospaced", Font.BOLD, 14));
        manualField.setHorizontalAlignment(JTextField.CENTER);
        manualField.setBorder(new LineBorder(BORDER_CYAN, 1));
        manualField.addActionListener(e -> onManualOk());

        manualOkButton = createStyledButton("OK Manual");
        manualOkButton.addActionListener(e -> onManualOk());

        manualRow.add(manualLabel, BorderLayout.WEST);
        manualRow.add(manualField, BorderLayout.CENTER);
        manualRow.add(manualOkButton, BorderLayout.EAST);

        controlsBox.add(manualRow);
        controlsBox.add(Box.createVerticalStrut(10));

        // Botão Reiniciar
        JButton restartButton = createStyledButton("↻ Reiniciar Jogo");
        restartButton.addActionListener(e -> onRestart());
        controlsBox.add(restartButton);
        controlsBox.add(Box.createVerticalStrut(6));

        // Botão Sair
        JButton quitButton = createStyledButton("⏻ Sair");
        quitButton.addActionListener(e -> System.exit(0));
        controlsBox.add(quitButton);

        add(controlsBox);
        refreshState(null);
    }

    public void setGame(Game game) {
        this.game = game;
        lastRollLabel.setText("🎲 -");
        manualField.setText("");
        refreshState(null);
    }

    private void onRandomClick() {
        if (game == null) return;
        try {
            game.playTurn();
        } catch (InvalidDiceValueException ex) {
            mainWindow.getLogPanel().append("ERRO: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            mainWindow.getLogPanel().append("AVISO: " + ex.getMessage());
        }
    }

    private void onManualOk() {
        if (game == null) return;
        String input = manualField.getText();
        try {
            if (game.getDice() instanceof ManualDice) {
                ((ManualDice) game.getDice()).setInput(input);
            }
            game.playTurn();
            manualField.setText("");
            manualField.requestFocusInWindow();
        } catch (InvalidDiceValueException ex) {
            mainWindow.getLogPanel().append(
                    "Valor inválido (\"" + input + "\"). Introduza um número de 1 a 6. O estado não mudou."
            );
            JOptionPane.showMessageDialog(
                    this,
                    "Valor de dado inválido: \"" + input + "\".\nIntroduza um valor inteiro entre 1 e 6.",
                    "Dado Manual Inválido",
                    JOptionPane.WARNING_MESSAGE
            );
            manualField.selectAll();
            manualField.requestFocusInWindow();
        } catch (IllegalStateException ex) {
            mainWindow.getLogPanel().append("AVISO: " + ex.getMessage());
        }
    }

    private void onRestart() {
        if (game != null) {
            game.restart();
        }
        mainWindow.promptNewGameConfiguration();
    }

    public void refreshState(TurnEvent lastEvent) {
        if (game == null) return;

        boolean finished = (game.getResult() != null);
        boolean isManual = (game.getDice() instanceof ManualDice);

        randomRollButton.setEnabled(!finished && !isManual);
        manualField.setEnabled(!finished && isManual);
        manualOkButton.setEnabled(!finished && isManual);

        Player current = game.getCurrentPlayer();
        if (current != null && !finished) {
            currentPlayerLabel.setText("> " + current.getName() + " (Ronda " + game.getRound() + "/40)");
        } else if (finished) {
            currentPlayerLabel.setText("> Jogo Terminado");
        }

        if (lastEvent != null) {
            lastRollLabel.setText("🎲 " + lastEvent.getDice());
        }

        StringBuilder sb = new StringBuilder();
        List<Player> players = game.getPlayers();
        if (players != null) {
            for (Player p : players) {
                sb.append("> ").append(p.getName())
                  .append(": Casa ").append(p.getPosition()).append(" / ")
                  .append(Board.SIZE).append("\n");
            }
        }
        positionsArea.setText(sb.toString().trim());
    }

    private JPanel createInfoCard(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CYAN, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        titleLbl.setForeground(TEXT_CYAN);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setBackground(new Color(16, 34, 48));
        btn.setForeground(TEXT_CYAN);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CYAN, 1, true),
                new EmptyBorder(7, 12, 7, 12)
        ));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}