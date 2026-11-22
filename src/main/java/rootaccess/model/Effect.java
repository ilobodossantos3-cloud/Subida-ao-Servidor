/*
 * Módulo: rootaccess.model.Effect
 * Propósito: contrato das casas com efeito (avanço ou recuo). Qualquer casa
 *            que redirecione o peão implementa esta interface.
 * Aluno: A - Igor Santos (30016495)
 * Revisor: [a preencher]
 * Uso de IA: Sim - ver AI_USE.md
 */
package rootaccess.model;

public interface Effect {

    /** Número da casa para onde o peão é enviado (1 a 50). */
    int getDestination();

    /** Descrição curta do efeito, para o registo de eventos da GUI. */
    String describeEffect();
}
