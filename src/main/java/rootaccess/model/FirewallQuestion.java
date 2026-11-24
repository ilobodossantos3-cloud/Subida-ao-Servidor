// Módulo: rootaccess.model.FirewallQuestion
// Propósito: representa uma pergunta de múltipla escolha associada a uma firewall.
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

import java.util.List;

public final class FirewallQuestion {

    // Casa do tabuleiro onde a pergunta é apresentada.
    private final int firewallCell;
    // Enunciado mostrado ao jogador.
    private final String prompt;
    // Opções, por ordem de apresentação; a primeira é a opção A.
    private final List<String> options;
    // Índice da opção correta dentro da lista.
    private final int correctIndex;
    // Justificação curta, mostrada depois da resposta.
    private final String explanation;

    public FirewallQuestion(int firewallCell, String prompt, List<String> options,
                            int correctIndex, String explanation) {
        // A pergunta só faz sentido numa casa válida do tabuleiro.
        if (firewallCell < 1 || firewallCell > Board.SIZE) {
            throw new IllegalArgumentException("Firewall inválida: " + firewallCell);
        }
        // Um enunciado vazio não dá informação suficiente ao jogador.
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("A pergunta precisa de enunciado");
        }
        // São precisas pelo menos duas alternativas para haver escolha.
        if (options == null || options.size() < 2) {
            throw new IllegalArgumentException("A pergunta precisa de pelo menos duas opções");
        }
        // O índice tem de apontar para uma das opções fornecidas.
        if (correctIndex < 0 || correctIndex >= options.size()) {
            throw new IllegalArgumentException("Índice de resposta correta inválido");
        }
        // Nenhuma alternativa pode estar vazia.
        for (String option : options) {
            if (option == null || option.isBlank()) {
                throw new IllegalArgumentException("As opções não podem estar vazias");
            }
        }
        // Cópia imutável para a pergunta não mudar depois de criada.
        this.firewallCell = firewallCell;
        this.prompt = prompt;
        this.options = List.copyOf(options);
        this.correctIndex = correctIndex;
        // A explicação é opcional; null é convertido em texto vazio.
        this.explanation = explanation == null ? "" : explanation;
    }

    public int getFirewallCell() { return firewallCell; }

    public String getPrompt() { return prompt; }

    public List<String> getOptions() { return options; }

    public int getCorrectIndex() { return correctIndex; }

    public String getExplanation() { return explanation; }

    // Texto completo da opção, com a letra correspondente.
    public String getOptionLabel(int index) {
        // Recusa índices fora da lista para evitar mensagens erradas.
        if (index < 0 || index >= options.size()) {
            throw new IllegalArgumentException("Opção inexistente: " + index);
        }
        // 'A' + índice dá A, B, C ou D.
        return (char) ('A' + index) + ") " + options.get(index);
    }
}
