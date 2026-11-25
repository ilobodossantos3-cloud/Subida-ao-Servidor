// Módulo: rootaccess.model.Player
// Propósito: jogador (pentester) com nome, cor do peão, avatar e posição atual.
//            A posição 0 significa "fora do tabuleiro".
// Aluno: A - Igor Santos (30016495)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

public class Player {

    private final String name;
    private final String color;
    // Avatar escolhido no ecrã de configuração; a vista usa o glifo.
    private final Avatar avatar;
    private int position;

    // Construtor antigo: mantém os testes e chamadas existentes a funcionar.
    public Player(String name, String color) {
        // Sem avatar explícito, usa o primeiro do catálogo como valor seguro.
        this(name, color, Avatar.at(0));
    }

    // Construtor novo: recebe também o avatar escolhido pelo jogador.
    public Player(String name, String color, Avatar avatar) {
        // Um nome vazio tornaria o registo de eventos ilegível.
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O jogador precisa de um nome");
        }
        // A cor identifica o peão quando dois avatares são parecidos.
        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("O jogador precisa de uma cor");
        }
        // O avatar é obrigatório porque a GUI o desenha no tabuleiro.
        if (avatar == null) {
            throw new IllegalArgumentException("O jogador precisa de um avatar");
        }
        // trim remove espaços acidentais escritos na configuração.
        this.name = name.trim();
        this.color = color;
        this.avatar = avatar;
        this.position = 0;
    }

    public String getName() { return name; }

    public String getColor() { return color; }

    // Devolve o avatar escolhido para este jogador.
    public Avatar getAvatar() { return avatar; }

    public int getPosition() { return position; }

    // Move o peão para a casa indicada (0 a 50).
    public void moveTo(int newPosition) {
        // Recusa posições que o tabuleiro não tem.
        if (newPosition < 0 || newPosition > Board.SIZE) {
            throw new IllegalArgumentException("Posição inválida: " + newPosition);
        }
        this.position = newPosition;
    }

    // Volta a pôr o peão fora do tabuleiro (reinício).
    public void reset() { this.position = 0; }

    public boolean isAtRoot() { return position == Board.SIZE; }

    @Override
    public String toString() {
        // Inclui o avatar para o log identificar o jogador de forma inequívoca.
        return avatar.getGlyph() + " " + name + " (" + color + ") na casa " + position;
    }
}
