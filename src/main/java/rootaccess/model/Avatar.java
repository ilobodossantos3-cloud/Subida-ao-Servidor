// Módulo: rootaccess.model.Avatar
// Propósito: catálogo dos avatares temáticos que cada jogador escolhe antes da partida.
// Aluno: A - Igor Santos (30016495)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

// Enumeração fechada: a GUI só pode mostrar avatares que existam aqui.
public enum Avatar {

    // Cada constante junta um nome legível e o glifo desenhado no tabuleiro.
    GHOST("Fantasma", "👻"),
    BOT("Bot de intrusão", "🤖"),
    BUG("Bug explorado", "🐞"),
    SKULL("Caveira", "💀"),
    BRAIN("Cérebro", "🧠"),
    KEY("Chave de acesso", "🔑"),
    SATELLITE("Satélite", "📡"),
    SHADES("Óculos de sombra", "🕶");

    // Texto que aparece nas listas de escolha e no registo de eventos.
    private final String displayName;
    // Carácter Unicode usado como avatar; o desenho fica a cargo da vista.
    private final String glyph;

    // Guarda o par nome/glifo de cada avatar.
    Avatar(String displayName, String glyph) {
        // O nome nunca pode faltar numa lista de escolha.
        this.displayName = displayName;
        // O glifo é texto simples, para não obrigar a ficheiros de imagem.
        this.glyph = glyph;
    }

    // Nome apresentado ao jogador.
    public String getDisplayName() {
        // Devolve o nome sem expor o campo.
        return displayName;
    }

    // Glifo que a vista usa para desenhar o avatar.
    public String getGlyph() {
        // Devolve o carácter escolhido.
        return glyph;
    }

    // Permite pedir o avatar de uma posição sem rebentar nos extremos.
    public static Avatar at(int index) {
        // floorMod evita índices negativos e roda suavemente pelo catálogo.
        return values()[Math.floorMod(index, values().length)];
    }

    // Texto completo para as combo boxes: "👻 Fantasma".
    @Override
    public String toString() {
        // Junta glifo e nome para a GUI não precisar de outro renderer.
        return glyph + " " + displayName;
    }
}
