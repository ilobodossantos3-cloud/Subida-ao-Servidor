/* Module and purpose: rootaccess.gui.LogPanel - Painel de registo de eventos (terminal hacker).
- Student name/number and implemented contributions: Cleiton Muangala (30016337) - Implementação completa do LogPanel, formatação temporal e estilo visual de terminal.
- Collaborator/reviewer and contribution: Pedro Moroni (30016458) - Revisão da integração com as mensagens de TurnEvent.
- AI assistance: Auxílio na estruturação visual dos componentes Swing (ver AI_USE.md).
 */
package rootaccess.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LogPanel extends JPanel {

    private static final Color BG_DARK = new Color(10, 15, 22);
    private static final Color BORDER_CYAN = new Color(25, 95, 110);
    private static final Color TEXT_GREEN = new Color(0, 230, 140);
    private static final Color HEADER_CYAN = new Color(90, 200, 220);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final JTextArea logArea;

    public LogPanel() {
        setLayout(new BorderLayout(0, 6));
        setBackground(BG_DARK);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CYAN, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel titleLabel = new JLabel("Log de Eventos");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        titleLabel.setForeground(HEADER_CYAN);
        add(titleLabel, BorderLayout.NORTH);

        logArea = new JTextArea(8, 26);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(new Color(6, 10, 15));
        logArea.setForeground(TEXT_GREEN);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBorder(new EmptyBorder(4, 4, 4, 4));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(6, 10, 15));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void append(String message) {
        String timestamp = LocalTime.now().format(TIME_FMT);
        logArea.append("[" + timestamp + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void clear() {
        logArea.setText("");
    }
}