/* Module and purpose: rootaccess.gui.MainWindow - Janela principal da aplicação Swing e implementação de GameObserver.
- Student name/number and implemented contributions: Cleiton Muangala (30016337) - Implementação de MainWindow, ecrã de configuração inicial (2-4 jogadores e modo de dado) e métodos onTurn/onGameOver.
- Collaborator/reviewer and contribution: Pedro Moroni (30016458) & Igor Santos (30016495) - Revisão da ligação do padrão Observer com a classe Game.
- AI assistance: Auxílio na configuração do diálogo inicial de jogadores (ver AI_USE.md).
 */
package rootaccess.gui;

import rootaccess.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainWindow extends JFrame implements GameObserver {

    private static final Color BG_MAIN = new Color(6, 10, 16);

    private Game game;
    private BoardPanel boardPanel;
    private ControlPanel controlPanel;
    private LogPanel logPanel;

    public MainWindow() {
        super("Root Access: Subida ao Servidor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout(12, 12));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        this.game = new Game();
        this.boardPanel = new BoardPanel(game);
        this.logPanel = new LogPanel();
        this.controlPanel = new ControlPanel(game, this);

        // Painel lateral direito ( Estado + Log + Controlos )
        JPanel rightColumn = new JPanel(new BorderLayout(0, 8));
        rightColumn.setBackground(BG_MAIN);
        rightColumn.setPreferredSize(new Dimension(330, 620));
        rightColumn.add(controlPanel, BorderLayout.NORTH);
        rightColumn.add(logPanel, BorderLayout.CENTER);

        add(boardPanel, BorderLayout.CENTER);
        add(rightColumn, BorderLayout.EAST);

        pack();
        setMinimumSize(new Dimension(1120, 680));
        setLocationRelativeTo(null);

        promptNewGameConfiguration();
    }

    public void promptNewGameConfiguration() {
        JPanel configPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Integer> countCombo = new JComboBox<>(new Integer[]{2, 3, 4});
        JRadioButton randomRadio = new JRadioButton("Dado Aleatório (1-6)", true);
        JRadioButton manualRadio = new JRadioButton("Dado Manual (1-6)");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(randomRadio);
        modeGroup.add(manualRadio);

        JTextField[] nameFields = {
                new JTextField("Jogador 1", 14),
                new JTextField("Jogador 2", 14),
                new JTextField("Jogador 3", 14),
                new JTextField("Jogador 4", 14)
        };
        nameFields[2].setEnabled(false);
        nameFields[3].setEnabled(false);

        countCombo.addActionListener(e -> {
            int count = (Integer) countCombo.getSelectedItem();
            for (int i = 0; i < 4; i++) {
                nameFields[i].setEnabled(i < count);
            }
        });

        gbc.gridx = 0; gbc.gridy = 0;
        configPanel.add(new JLabel("Número de jogadores (2-4):"), gbc);
        gbc.gridx = 1;
        configPanel.add(countCombo, gbc);

        for (int i = 0; i < 4; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1;
            configPanel.add(new JLabel("Nome do Jogador " + (i + 1) + ":"), gbc);
            gbc.gridx = 1;
            configPanel.add(nameFields[i], gbc);
        }

        gbc.gridx = 0; gbc.gridy = 5;
        configPanel.add(new JLabel("Modo de Dado:"), gbc);
        gbc.gridx = 1;
        configPanel.add(randomRadio, gbc);
        gbc.gridx = 1; gbc.gridy = 6;
        configPanel.add(manualRadio, gbc);

        int option = JOptionPane.showConfirmDialog(
                this,
                configPanel,
                "Configuração Inicial — Root Access",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION && game.getPlayers() != null && !game.getPlayers().isEmpty()) {
            return;
        }

        int playerCount = (Integer) countCombo.getSelectedItem();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            String rawName = nameFields[i].getText().trim();
            names.add(rawName.isEmpty() ? "Jogador " + (i + 1) : rawName);
        }

        Dice selectedDice = manualRadio.isSelected() ? new ManualDice() : new RandomDice();
        this.game = new Game(selectedDice);
        this.game.addObserver(this);
        this.game.start(names);

        boardPanel.setGame(game);
        controlPanel.setGame(game);
        logPanel.clear();
        logPanel.append("Jogo iniciado! Modo: " + (manualRadio.isSelected() ? "Manual" : "Aleatório"));
        logPanel.append(game.getCurrentPlayer().getName() + " começa o turno na posição 0.");
    }

    @Override
    public void onTurn(TurnEvent event) {
        if (event != null) {
            if (event.getMessage() != null && !event.getMessage().isEmpty()) {
                logPanel.append(event.getMessage());
            } else {
                String playerName = event.getPlayer().getName();
                logPanel.append(playerName + " lançou o dado: " + event.getDice());
                if (event.getLanded() != event.getFinalPos()) {
                    logPanel.append(playerName + ": casa " + event.getFrom() + " -> "
                            + event.getLanded() + " (efeito) -> casa " + event.getFinalPos() + ".");
                } else {
                    logPanel.append(playerName + " avançou da casa " + event.getFrom()
                            + " para a casa " + event.getFinalPos() + ".");
                }
            }
        }
        controlPanel.refreshState(event);
        boardPanel.repaint();
    }

    @Override
    public void onGameOver(GameResult result) {
        controlPanel.refreshState(null);
        boardPanel.repaint();

        if (result == null) return;

        String endMessage;
        if (result.isDraw()) {
            String tiedNames = result.getWinners().stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", "));
            endMessage = "Fim às 40 rondas! Empate na frente entre: " + tiedNames + ".";
        } else {
            Player winner = result.getWinners().get(0);
            endMessage = "🏆 ROOT MASTER! " + winner.getName() + " conquistou o servidor Root!";
        }

        logPanel.append(endMessage);
        JOptionPane.showMessageDialog(
                this,
                endMessage,
                "Fim do Jogo — Root Access",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public LogPanel getLogPanel() {
        return logPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}