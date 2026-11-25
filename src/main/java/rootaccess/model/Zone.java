// Módulo: rootaccess.model.Zone
// Propósito: enumeração das cinco zonas da rede da NexaCorp (cada zona
// corresponde a uma linha do tabuleiro, com 10 casas).
// Aluno: A - Igor Santos (30016495)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

public enum Zone {
    INTERNET("Internet", 1, 10, "azul-frio"),
    DMZ("DMZ", 11, 20, "verde"),
    REDE_INTERNA("Rede Interna", 21, 30, "âmbar"),
    DATACENTER("Datacenter", 31, 40, "laranja"),
    NUCLEO("Núcleo", 41, 50, "vermelho");

    private final String displayName;
    private final int firstCell;
    private final int lastCell;
    private final String colorName;

    Zone(String displayName, int firstCell, int lastCell, String colorName) {
        this.displayName = displayName;
        this.firstCell = firstCell;
        this.lastCell = lastCell;
        this.colorName = colorName;
    }

    public String getDisplayName() { return displayName; }
    public int getFirstCell() { return firstCell; }
    public int getLastCell() { return lastCell; }
    public String getColorName() { return colorName; }

    // Devolve a zona a que pertence a casa indicada (1 a 50).
    public static Zone of(int cellNumber) {
        for (Zone z : values()) {
            if (cellNumber >= z.firstCell && cellNumber <= z.lastCell) {
                return z;
            }
        }
        throw new IllegalArgumentException("Casa inexistente: " + cellNumber);
    }

    @Override
    public String toString() { return displayName; }
}
