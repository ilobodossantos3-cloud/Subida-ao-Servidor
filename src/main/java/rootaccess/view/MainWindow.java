// Módulo: rootaccess.view.MainWindow
// Propósito: janela principal, configuração inicial (jogadores, avatares e
//            modo do dado), ligação ao modelo e apresentação do fim do jogo.
// Aluno: C - Cleiton Muangala (30016337)
// Revisor: [a preencher]
// Uso de IA: Sim - Claude Code: escolha do modo do dado, sequência dado -> movimento,
//            resposta à firewall enviada ao modelo (ver AI_USE.md, secção 3)
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
import rootaccess.model.RandomDice;
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
<<<<<<< HEAD
import javax.swing.border.LineBorder;
=======
>>>>>>> master
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
import java.util.OptionalInt;
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

        // Jogo vazio até a configuração ser confirmada.
        this.game = new Game(new RandomDice());
        this.boardPanel = new BoardPanel(game);
        this.logPanel = new LogPanel();
        // O painel de controlo só conhece o registo e a ação de reinício.
        this.controlPanel = new ControlPanel(game, logPanel, () -> promptNewGameConfiguration());

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
        // Lista genérica de combos: evita o array de tipo genérico (raw type).
        List<JComboBox<Avatar>> avatarCombos = new ArrayList<>();
<<<<<<< HEAD
        JLabel[] previews = new JLabel[4];
=======
>>>>>>> master

        // Cada jogador arranca com um avatar diferente do catálogo.
        for (int i = 0; i < 4; i++) {
            avatarCombos.add(new JComboBox<>(Avatar.values()));
            avatarCombos.get(i).setSelectedItem(Avatar.at(i));
<<<<<<< HEAD
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
            avatarCombos.get(i).addActionListener(event -> refreshAvatarPreview(
                    previews[index], avatarCombos.get(index), index));
            refreshAvatarPreview(previews[i], avatarCombos.get(i), i);
=======
>>>>>>> master
        }

        // Cabeçalho da grelha de jogadores.
        gbc.gridx = 0;
        gbc.gridy = 0;
        configPanel.add(new JLabel("Número de jogadores (2-4):"), gbc);
        gbc.gridx = 1;
<<<<<<< HEAD
        gbc.gridwidth = 3;
=======
        gbc.gridwidth = 2;
>>>>>>> master
        configPanel.add(countCombo, gbc);
        gbc.gridwidth = 1;

        // gridx volta a 0: sem isto "Jogador" e "Nome" ficavam na mesma célula.
        gbc.gridy = 1;
        gbc.gridx = 0;
        configPanel.add(headerLabel("Jogador"), gbc);
        gbc.gridx = 1;
        configPanel.add(headerLabel("Nome"), gbc);
        gbc.gridx = 2;
        configPanel.add(headerLabel("Avatar"), gbc);
<<<<<<< HEAD
        gbc.gridx = 3;
        configPanel.add(headerLabel("Ativo"), gbc);

        // Uma linha por jogador: número, nome, combo e pré-visualização.
=======

        // Uma linha por jogador: número, nome e avatar.
>>>>>>> master
        for (int i = 0; i < 4; i++) {
            gbc.gridy = i + 2;
            gbc.gridx = 0;
            configPanel.add(new JLabel("Jogador " + (i + 1) + ":"), gbc);
            gbc.gridx = 1;
            configPanel.add(nameFields[i], gbc);
            gbc.gridx = 2;
            configPanel.add(avatarCombos.get(i), gbc);
<<<<<<< HEAD
            gbc.gridx = 3;
            configPanel.add(previews[i], gbc);
=======
>>>>>>> master
        }

        // Nota que explica a exigência de avatares distintos.
        gbc.gridy = 6;
        gbc.gridx = 0;
<<<<<<< HEAD
        gbc.gridwidth = 4;
=======
        gbc.gridwidth = 3;
>>>>>>> master
        JLabel note = new JLabel("Cada jogador escolhe um avatar diferente dos restantes.");
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        note.setForeground(new Color(120, 145, 160));
        configPanel.add(note, gbc);
        gbc.gridwidth = 1;

        // Modo do dado, escolhido no início e válido para toda a partida.
        JRadioButton randomMode = new JRadioButton("Aleatório (o jogo sorteia 1-6)", true);
        JRadioButton manualMode = new JRadioButton("Manual (o jogador escreve 1-6)");
        ButtonGroup diceModes = new ButtonGroup();
        diceModes.add(randomMode);
        diceModes.add(manualMode);
        JPanel diceModeRow = new JPanel(new GridLayout(1, 2, 8, 0));
        diceModeRow.add(randomMode);
        diceModeRow.add(manualMode);
        gbc.gridy = 7;
        gbc.gridx = 0;
        configPanel.add(headerLabel("Modo do dado"), gbc);
        gbc.gridx = 1;
<<<<<<< HEAD
        gbc.gridwidth = 3;
=======
        gbc.gridwidth = 2;
>>>>>>> master
        configPanel.add(diceModeRow, gbc);
        gbc.gridwidth = 1;

        // Os campos dos jogadores inativos começam desligados.
<<<<<<< HEAD
        updatePlayerRows(countCombo, nameFields, avatarCombos, previews);
        countCombo.addActionListener(event ->
                updatePlayerRows(countCombo, nameFields, avatarCombos, previews));
=======
        updatePlayerRows(countCombo, nameFields, avatarCombos);
        countCombo.addActionListener(event ->
                updatePlayerRows(countCombo, nameFields, avatarCombos));
>>>>>>> master

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
                Avatar avatar = (Avatar) avatarCombos.get(i).getSelectedItem();
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

            // Os dois modos usam a mesma lógica de turno; só muda o Dice do modelo.
            Dice selectedDice = manualMode.isSelected() ? new ManualDice() : new RandomDice();
            this.game = new Game(selectedDice);
            this.game.addObserver(this);
            this.game.start(names, avatars);
            pendingGameOver = null;

            // Reaponta as vistas para a partida nova.
            boardPanel.setGame(game);
            controlPanel.setGame(game);
            logPanel.clear();
            logPanel.append("Jogo iniciado no modo de dado " + selectedDice.getModeName() + ". "
                    + (manualMode.isSelected()
                    ? "Escreve um valor de 1 a 6 e carrega em Lançar."
                    : "Clica no dado ou no botão para lançar."));
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
<<<<<<< HEAD
                                  List<JComboBox<Avatar>> avatarCombos,
                                  JLabel[] previews) {
=======
                                  List<JComboBox<Avatar>> avatarCombos) {
>>>>>>> master
        // Os quatro arrays têm sempre o mesmo tamanho.
        int count = (Integer) countCombo.getSelectedItem();
        for (int i = 0; i < 4; i++) {
            // Só as linhas dos jogadores ativos aceitam edição.
            boolean active = i < count;
            nameFields[i].setEnabled(active);
            avatarCombos.get(i).setEnabled(active);
<<<<<<< HEAD
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

=======
        }
    }

>>>>>>> master
    private JLabel headerLabel(String text) {
        // Cabeçalhos discretos, alinhados com o tema.
        JLabel label = new JLabel(text);
        label.setFont(new Font("Monospaced", Font.BOLD, 11));
        label.setForeground(new Color(90, 200, 220));
        return label;
    }

<<<<<<< HEAD
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

=======
>>>>>>> master
    @Override
    public void onTurn(TurnEvent event) {
        // Evento nulo não deve provocar uma exceção na animação.
        if (event == null) {
            return;
        }
        // A animação bloqueia o dado até o avatar parar.
        controlPanel.setAnimating(true, event);
        if (event.getFirewallCorrect() == null) {
            // Lançamento novo: o modelo já moveu o peão, mas a vista mostra
            // primeiro a face do dado e só depois o percurso.
            boardPanel.holdAtStart(event);
            controlPanel.showRoll(event, () -> presentTurn(event));
        } else {
            // Resposta à firewall: não há dado novo para mostrar.
            presentTurn(event);
        }
    }

    // Escreve o turno no registo e anima o avatar; no fim, pergunta a firewall
    // (se houver) ou liberta os controlos.
    private void presentTurn(TurnEvent event) {
        // O modelo já produziu a mensagem completa do turno.
        if (!event.getMessage().isEmpty()) {
            logPanel.append(event.getMessage());
        }
        // Resposta errada leva um som grave; a certa é tratada como efeito.
        if (Boolean.FALSE.equals(event.getFirewallCorrect())) {
            SoundEffects.playDenied();
        }
        // Ao passar a firewall com sucesso, o som é de passagem, não de efeito normal.
        Runnable effectSound = event.getFirewallCorrect() != null
                ? SoundEffects::playFirewallPass
                : SoundEffects::playEvent;
        boardPanel.animateTurn(event, SoundEffects::playStep, effectSound, () -> {
            // Primeiro evento: o avatar chegou à firewall e falta responder.
            if (event.isAwaitingFirewall()) {
                controlPanel.refreshPositions();
                // O diálogo é modal; os controlos continuam bloqueados.
                OptionalInt choice = askFirewall(event.getQuestion());
                // O modelo decide se a resposta está certa; a vista só envia a escolha.
                // A resposta gera um novo TurnEvent, que relança a animação.
                if (choice.isPresent()) {
                    game.answerFirewall(choice.getAsInt());
                } else {
                    game.giveUpFirewall();
                }
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

    // Mostra a pergunta e devolve o índice escolhido (0 = A), ou vazio se o
    // jogador desistir. A correção da resposta é feita pelo Game.
    private OptionalInt askFirewall(FirewallQuestion question) {
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
        JLabel hint = new JLabel("Acertar: avança 1 casa. Errar ou desistir: ficas na firewall.");
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
        // Botões explícitos: a desistência é uma escolha visível, não um efeito escondido.
        Object[] actions = {"Responder", "Desistir (conta como errada)"};

        while (true) {
            int choice = JOptionPane.showOptionDialog(
                    this,
                    panel,
                    title,
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    actions,
                    actions[0]
            );
            // Desistir ou fechar a janela conta como resposta errada (regra do grupo).
            if (choice != 0) {
                return OptionalInt.empty();
            }
            // Procura a opção marcada; sem escolha, insiste sem mudar o jogo.
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i].isSelected()) {
                    return OptionalInt.of(i);
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
        controlPanel.refreshState();
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
