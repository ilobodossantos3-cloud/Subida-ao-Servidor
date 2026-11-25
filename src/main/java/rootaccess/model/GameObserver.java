// Módulo: rootaccess.model.GameObserver
// Propósito: recebe atualizações de turno e de fim do jogo sem acoplar o modelo à GUI.
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

public interface GameObserver {

    void onTurn(TurnEvent event);

    void onGameOver(GameResult result);
}
