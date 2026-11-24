// Módulo: rootaccess.view.ControlPanel
// Propósito: estado do turno, dado único interativo, controlos de som,
//            reinício e saída.
// Aluno: C - Cleiton Muangala (30016337)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.view;

import rootaccess.model.Board;
import rootaccess.model.Dice;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

public class ControlPanel extends JPanel {

    private static final Color BG_DARK = new Color(10, 15, 22);
    private static final Color BG_CARD = new Color(13, 21, 30);
    private static final Color BORDER_CYAN = new Color(25, 95, 110);
    private static final Color TEXT_GREEN = new Color(0, 230, 140);
    private static final Color TEXT_CYAN = new Color(90, 200, 220);

    private Game game;
    private final MainWindow mainWindow;
    private final DicePanel dicePanel;
    private final JLabel currentPlayerLabel;
    private final JTextArea positionsArea;
    private final JButton restartButton;
    private final JCheckBox soundToggle;
    private boolean animating;

    public ControlPanel(Game game, MainWindow mainWindow) {
        // O painel consulta o jogo e o log da janela principal.
        this.game = game;
        this.mainWindow = mainWindow;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG_DARK);

        // Cartão com o jogador que tem a vez.
        currentPlayerLabel = new JLabel("> -");
        currentPlayerLabel.setFont(new Font("Monospaced", Font.BOLD, 15));
        currentPlayerLabel.setForeground(TEXT_GREEN);
        add(createInfoCard("Jogador Atual", currentPlayerLabel));
        add(Box.createVerticalStrut(8));

        // Dado único: o mesmo objeto serve todos os turnos.
        dicePanel = new DicePanel(this::onDieRolled);
        add(createInfoCard("Dado", dicePanel));
        add(Box.createVerticalStrut(8));

        // Lista das posições, agora com o avatar de cada jogador.
        positionsArea = new JTextArea(4, 20);
        positionsArea.setEditable(false);
        positionsArea.setBackground(BG_CARD);
        positionsArea.setForeground(TEXT_CYAN);
        positionsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(createInfoCard("Posição Atual (Casas 0-50)", positionsArea));
        add(Box.createVerticalStrut(8));

        // Cartão de controlos gerais.
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
        controlsBox.add(Box.createVerticalStrut(10));

        // Reiniciar abre novamente a configuração de jogadores.
        restartButton = createStyledButton("↻ Reiniciar Jogo");
        restartButton.addActionListener(event -> onRestart());
        controlsBox.add(restartButton);
        controlsBox.add(Box.createVerticalStrut(6));

        // Sair encerra a aplicação.
        JButton quitButton = createStyledButton("⏻ Sair");
        quitButton.addActionListener(event -> System.exit(0));
        controlsBox.add(quitButton);

        add(controlsBox);
        // Estado inicial coerente com um jogo ainda sem jogadores.
        refreshState(null);
    }

    public void setGame(Game game) {
        // Troca o modelo por uma partida nova e limpa o dado.
        this.game = game;
        dicePanel.reset();
        animating = false;
        refreshState(null);
    }

    public void setAnimating(boolean animating, TurnEvent event) {
        // A flag impede lançamentos enquanto o avatar se desloca.
        this.animating = animating;
        refreshState(event);
        if (animating && event != null) {
            // Mensagem temporária com o destino que está a ser percorrido.
            currentPlayerLabel.setText(
                    "> " + event.getPlayer().getName() + " a mover-se para a casa "
                            + event.getFinalPos() + "..."
            );
        }
    }

    private void onDieRolled(int value) {
        // Sem jogo não há turno para processar.
        if (game == null) {
            return;
        }
        try {
            Dice dice = game.getDice();
            // O dado visual fixa o valor que o modelo vai consumir.
            if (dice instanceof ManualDice manual) {
                manual.setInput(Integer.toString(value));
            }
            // O turno corre uma única vez por clique.
            game.playTurn();
        } catch (InvalidDiceValueException ex) {
            // Só acontece se a ligação entre dado visual e modelo falhar.
            mainWindow.getLogPanel().append("ERRO: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            // Jogo terminado ou ainda não iniciado.
            mainWindow.getLogPanel().append("AVISO: " + ex.getMessage());
        }
    }

    private void onRestart() {
        // O som confirma a ação antes de abrir o diálogo.
        SoundEffects.playClick();
        // O diálogo trata de criar um Game novo se for confirmado.
        mainWindow.promptNewGameConfiguration();
    }

    public void refreshState(TurnEvent lastEvent) {
        // Ainda não há partida: nada para atualizar além do estado base.
        if (game == null) {
            return;
        }

        boolean finished = game.getResult() != null;
        boolean hasPlayers = !game.getPlayers().isEmpty();
        // O dado só responde quando é a vez do jogador, não há animação
        // e nenhuma pergunta de firewall está pendente.
        dicePanel.setInteractionEnabled(hasPlayers && !finished && !animating
                && !game.isAwaitingFirewall());
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

        // Constrói a lista de posições com o avatar de cada jogador.
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

    private JPanel createInfoCard(String title, JComponent content) {
        // Cartão com título e conteúdo, reutilizado pelos três blocos.
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CYAN, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Título pequeno em ciano.
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        titleLabel.setForeground(TEXT_CYAN);

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
