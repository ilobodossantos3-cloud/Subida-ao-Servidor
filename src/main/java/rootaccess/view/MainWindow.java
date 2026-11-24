// Módulo: rootaccess.view.MainWindow
// Propósito: janela principal, configuração inicial com nomes e avatares,
//            ligação ao modelo e apresentação do fim do jogo.
// Aluno: C - Cleiton Muangala (30016337)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.view;

import rootaccess.model.Avatar;
import rootaccess.model.Board;
import rootaccess.model.Dice;
import rootaccess.model.FirewallQuestion;
import rootaccess.model.Game;
import rootaccess.model.GameObserver;
import rootaccess.model.GameResult;
import rootaccess.model.ManualDice;
import rootaccess.model.Player;
import rootaccess.model.TurnEvent;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainWindow extends JFrame implements GameObserver {

    private static final Color BG_MAIN = new Color(6, 10, 16);

    private Game game;
    private final BoardPanel boardPanel;
    private final ControlPanel controlPanel;
    private final LogPanel logPanel;
    private GameResult pendingGameOver;

    public MainWindow() {
        super("Root Access: Subida ao Servidor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout(12, 12));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        // Dado manual interior: o valor vem sempre do dado clicável da GUI.
        this.game = new Game(new ManualDice());
        this.boardPanel = new BoardPanel(game);
        this.logPanel = new LogPanel();
        this.controlPanel = new ControlPanel(game, this);

        // Coluna direita: estado, dado e registo.
        JPanel rightColumn = new JPanel(new BorderLayout(0, 8));
        rightColumn.setBackground(BG_MAIN);
        rightColumn.setPreferredSize(new Dimension(340, 640));
        rightColumn.add(controlPanel, BorderLayout.NORTH);
        rightColumn.add(logPanel, BorderLayout.CENTER);

        add(boardPanel, BorderLayout.CENTER);
        add(rightColumn, BorderLayout.EAST);

        pack();
        setMinimumSize(new Dimension(1140, 700));
        setLocationRelativeTo(null);

        // A configuração é obrigatória antes de mostrar o tabuleiro.
        if (!promptNewGameConfiguration()) {
            dispose();
        }
    }

    public boolean promptNewGameConfiguration() {
        // Ecrã de configuração: contagem, nomes e avatares.
        JPanel configPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Quantidade de jogadores permitida pelo modelo.
        JComboBox<Integer> countCombo = new JComboBox<>(new Integer[]{2, 3, 4});
        JTextField[] nameFields = {
                new JTextField("Jogador 1", 12),
                new JTextField("Jogador 2", 12),
                new JTextField("Jogador 3", 12),
                new JTextField("Jogador 4", 12)
        };
        // Array de combos; o Swing não permite criar arrays genéricos com tipo concreto.
        @SuppressWarnings("unchecked")
        JComboBox<Avatar>[] avatarCombos = new JComboBox[4];
        JLabel[] previews = new JLabel[4];

        // Cada jogador arranca com um avatar diferente do catálogo.
        for (int i = 0; i < 4; i++) {
            avatarCombos[i] = new JComboBox<>(Avatar.values());
            avatarCombos[i].setSelectedItem(Avatar.at(i));
            previews[i] = new JLabel();
            previews[i].setFont(new Font("SansSerif", Font.PLAIN, 24));
            previews[i].setHorizontalAlignment(JLabel.CENTER);
            previews[i].setPreferredSize(new Dimension(48, 42));
            previews[i].setOpaque(true);
            previews[i].setBackground(new Color(12, 22, 30));
            previews[i].setBorder(new LineBorder(playerAccent(i), 2, true));
        }

        // Atualiza a pré-visualização assim que o avatar muda.
        for (int i = 0; i < 4; i++) {
            final int index = i;
            avatarCombos[i].addActionListener(event -> refreshAvatarPreview(
                    previews[index], avatarCombos[index], index));
            refreshAvatarPreview(previews[i], avatarCombos[i], i);
        }

        // Cabeçalho da grelha de jogadores.
        gbc.gridx = 0;
        gbc.gridy = 0;
        configPanel.add(new JLabel("Número de jogadores (2-4):"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        configPanel.add(countCombo, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 1;
        configPanel.add(headerLabel("Jogador"), gbc);
        gbc.gridx = 1;
        configPanel.add(headerLabel("Nome"), gbc);
        gbc.gridx = 2;
        configPanel.add(headerLabel("Avatar"), gbc);
        gbc.gridx = 3;
        configPanel.add(headerLabel("Ativo"), gbc);

        // Uma linha por jogador: número, nome, combo e pré-visualização.
        for (int i = 0; i < 4; i++) {
            gbc.gridy = i + 2;
            gbc.gridx = 0;
            configPanel.add(new JLabel("Jogador " + (i + 1) + ":"), gbc);
            gbc.gridx = 1;
            configPanel.add(nameFields[i], gbc);
            gbc.gridx = 2;
            configPanel.add(avatarCombos[i], gbc);
            gbc.gridx = 3;
            configPanel.add(previews[i], gbc);
        }

        // Nota que explica a exigência de avatares distintos.
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        JLabel note = new JLabel("Cada jogador escolhe um avatar diferente dos restantes.");
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        note.setForeground(new Color(120, 145, 160));
        configPanel.add(note, gbc);
        gbc.gridwidth = 1;

        // Os campos dos jogadores inativos começam desligados.
        updatePlayerRows(countCombo, nameFields, avatarCombos, previews);
        countCombo.addActionListener(event ->
                updatePlayerRows(countCombo, nameFields, avatarCombos, previews));

        // Repete o diálogo se houver avatares repetidos.
        while (true) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    configPanel,
                    "Configuração Inicial — Root Access",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            // Cancelar mantém o jogo anterior, se existir.
            if (option != JOptionPane.OK_OPTION) {
                return false;
            }

            int playerCount = (Integer) countCombo.getSelectedItem();
            List<String> names = new ArrayList<>();
            List<Avatar> avatars = new ArrayList<>();
            Set<Avatar> used = EnumSet.noneOf(Avatar.class);
            int duplicate = -1;

            // Lê apenas as linhas ativas.
            for (int i = 0; i < playerCount; i++) {
                String rawName = nameFields[i].getText().trim();
                names.add(rawName.isEmpty() ? "Jogador " + (i + 1) : rawName);
                Avatar avatar = (Avatar) avatarCombos[i].getSelectedItem();
                avatars.add(avatar);
                if (!used.add(avatar)) {
                    duplicate = i + 1;
                }
            }

            // Dois avatares iguais tornam os peões confusos; pede nova escolha.
            if (duplicate >= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "O Jogador " + duplicate + " escolheu um avatar que já está em uso.\n"
                                + "Escolhe um avatar diferente para cada jogador.",
                        "Avatares repetidos",
                        JOptionPane.WARNING_MESSAGE
                );
                continue;
            }

            // O dado do jogo é alimentado pelo DicePanel, nunca por aleatoriedade oculta.
            Dice selectedDice = new ManualDice();
            this.game = new Game(selectedDice);
            this.game.addObserver(this);
            this.game.start(names, avatars);
            pendingGameOver = null;

            // Reaponta as vistas para a partida nova.
            boardPanel.setGame(game);
            controlPanel.setGame(game);
            logPanel.clear();
            logPanel.append("Jogo iniciado. Clica no dado para lançar.");
            // Lista os avatares escolhidos para o registo inicial ficar completo.
            String roster = IntStream.range(0, names.size())
                    .mapToObj(i -> avatars.get(i).getGlyph() + " " + names.get(i))
                    .collect(Collectors.joining(", "));
            logPanel.append("Avatares: " + roster + ". Objetivo: chegar à casa "
                    + Board.SIZE + ".");
            return true;
        }
    }

    private void updatePlayerRows(JComboBox<Integer> countCombo,
                                  JTextField[] nameFields,
                                  JComboBox<Avatar>[] avatarCombos,
                                  JLabel[] previews) {
        // Os quatro arrays têm sempre o mesmo tamanho.
        int count = (Integer) countCombo.getSelectedItem();
        for (int i = 0; i < 4; i++) {
            // Só as linhas dos jogadores ativos aceitam edição.
            boolean active = i < count;
            nameFields[i].setEnabled(active);
            avatarCombos[i].setEnabled(active);
            previews[i].setEnabled(active);
        }
    }

    private void refreshAvatarPreview(JLabel preview, JComboBox<Avatar> combo, int index) {
        // O valor selecionado nunca é nulo com o catálogo fechado.
        Avatar avatar = (Avatar) combo.getSelectedItem();
        if (avatar == null) {
            return;
        }
        // Glifo grande para o jogador confirmar a escolha antes de começar.
        preview.setText(avatar.getGlyph());
        preview.setToolTipText(avatar.getDisplayName());
        // A borda mantém a cor do jogador, não a do avatar.
        preview.setBorder(new LineBorder(playerAccent(index), 2, true));
    }

    private JLabel headerLabel(String text) {
        // Cabeçalhos discretos, alinhados com o tema.
        JLabel label = new JLabel(text);
        label.setFont(new Font("Monospaced", Font.BOLD, 11));
        label.setForeground(new Color(90, 200, 220));
        return label;
    }

    private Color playerAccent(int index) {
        // Cores iguais às usadas no tabuleiro, por ordem de jogador.
        Color[] accents = {
                new Color(0, 220, 130),
                new Color(0, 185, 255),
                new Color(240, 200, 50),
                new Color(255, 90, 200)
        };
        return accents[Math.floorMod(index, accents.length)];
    }

    @Override
    public void onTurn(TurnEvent event) {
        // Evento nulo não deve provocar uma exceção na animação.
        if (event == null) {
            return;
        }
        // O modelo já produziu a mensagem completa do turno.
        if (event.getMessage() != null && !event.getMessage().isEmpty()) {
            logPanel.append(event.getMessage());
        }
        // Resposta errada leva um som grave; a certa é tratada como efeito.
        if (Boolean.FALSE.equals(event.getFirewallCorrect())) {
            SoundEffects.playDenied();
        }
        // A animação bloqueia o dado até o avatar parar.
        controlPanel.setAnimating(true, event);
        // Ao passar a firewall com sucesso, o som é de passagem, não de efeito normal.
        Runnable effectSound = event.getFirewallCorrect() != null
                ? SoundEffects::playFirewallPass
                : SoundEffects::playEvent;
        boardPanel.animateTurn(event, SoundEffects::playStep, effectSound, () -> {
            // Primeiro evento: o avatar chegou à firewall e falta responder.
            if (event.isAwaitingFirewall()) {
                // O diálogo é modal; os controlos continuam bloqueados.
                boolean correct = askFirewall(event.getQuestion());
                // A resposta gera um novo TurnEvent, que relança a animação.
                game.answerFirewall(correct);
                return;
            }
            // Evento final: liberta os controlos e mostra o fim, se existir.
            controlPanel.setAnimating(false, null);
            if (pendingGameOver != null) {
                GameResult result = pendingGameOver;
                pendingGameOver = null;
                showGameOver(result);
            }
        });
    }

    private boolean askFirewall(FirewallQuestion question) {
        // O enunciado vem do banco e é escapado para o HTML do JLabel.
        JLabel prompt = new JLabel("<html><body style='width:360px'>"
                + escapeHtml(question.getPrompt()) + "</body></html>");
        prompt.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Uma opção por linha, com letras A-D vindas do modelo.
        JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        ButtonGroup group = new ButtonGroup();
        JRadioButton[] buttons = new JRadioButton[question.getOptions().size()];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JRadioButton(question.getOptionLabel(i));
            buttons[i].setFont(new Font("SansSerif", Font.PLAIN, 12));
            group.add(buttons[i]);
            optionsPanel.add(buttons[i]);
        }

        // Recorda a regra sem revelar a resposta.
        JLabel hint = new JLabel("Acertar: avança 1 casa. Errar: ficas na firewall.");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(new Color(120, 145, 160));

        // Composição final do diálogo.
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.add(prompt, BorderLayout.NORTH);
        panel.add(optionsPanel, BorderLayout.CENTER);
        panel.add(hint, BorderLayout.SOUTH);

        // Nome da casa para o título (ex.: "Firewall perimetral").
        String cellName = game.getBoard().getCell(question.getFirewallCell()).getName();
        String title = "Firewall — " + question.getFirewallCell() + " " + cellName;

        while (true) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    title,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            // Fechar ou cancelar conta como resposta errada.
            if (choice != JOptionPane.OK_OPTION) {
                return false;
            }
            // Procura a opção marcada; sem escolha, insiste.
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i].isSelected()) {
                    return i == question.getCorrectIndex();
                }
            }
            JOptionPane.showMessageDialog(
                    this,
                    "Escolhe uma das opções antes de responder.",
                    "Resposta em falta",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private String escapeHtml(String text) {
        // Evita que caracteres especiais quebrem o HTML do enunciado.
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    public void onGameOver(GameResult result) {
        // Guarda o resultado para o callback da animação o apresentar.
        pendingGameOver = result;
    }

    private void showGameOver(GameResult result) {
        // Atualiza os painéis antes de abrir a mensagem final.
        controlPanel.refreshState(null);
        boardPanel.repaint();

        // Sem resultado não há mensagem para mostrar.
        if (result == null) {
            return;
        }

        String endMessage;
        if (result.isDraw()) {
            // Empate: lista todos os líderes empatados.
            String tiedNames = result.getWinners().stream()
                    .map(player -> player.getAvatar().getGlyph() + " " + player.getName())
                    .collect(Collectors.joining(", "));
            endMessage = "Fim às " + Game.MAX_ROUNDS + " rondas! Empate na frente entre: "
                    + tiedNames + ".";
        } else if (result.isRoundLimit()) {
            // Limite de rondas com um único líder.
            Player leader = result.getWinners().get(0);
            endMessage = "Fim às " + Game.MAX_ROUNDS + " rondas! "
                    + leader.getAvatar().getGlyph() + " " + leader.getName()
                    + " ficou em primeiro lugar.";
        } else {
            // Vitória normal na casa Root.
            Player winner = result.getWinners().get(0);
            endMessage = "🏆 ROOT MASTER! " + winner.getAvatar().getGlyph() + " "
                    + winner.getName() + " conquistou o servidor Root!";
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
        // A GUI Swing corre sempre na Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            if (window.isDisplayable()) {
                window.setVisible(true);
            }
        });
    }
}
