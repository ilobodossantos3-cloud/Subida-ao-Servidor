// Módulo: rootaccess.model.QuestionBank
// Propósito: guarda as perguntas de múltipla escolha de cada firewall e
//            entrega a próxima pergunta sempre que o peão cai numa delas.
// Aluno: B - Pedro Moroni (30016458)
// Revisor: [a preencher]
// Uso de IA: Sim - ver AI_USE.md
package rootaccess.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class QuestionBank {

    // Uma firewall pode ter mais do que uma pergunta; a lista preserva a ordem.
    private final Map<Integer, List<FirewallQuestion>> questions = new LinkedHashMap<>();
    // Posição seguinte a usar em cada firewall, para não repetir sempre a mesma.
    private final Map<Integer, Integer> nextIndex = new LinkedHashMap<>();

    public QuestionBank() {
        // As perguntas são as publicadas no README, secção "Perguntas das firewalls".
        // Firewall 11: conceito de DMZ, coerente com a primeira barreira da rede.
        add(new FirewallQuestion(
                11,
                "Qual é a função principal de uma DMZ numa rede empresarial?",
                List.of(
                        "Guardar as cópias de segurança da empresa.",
                        "Isolar os serviços expostos à Internet do resto da rede interna.",
                        "Aumentar a velocidade das ligações internas.",
                        "Substituir por completo o firewall perimetral."
                ),
                1,
                "A DMZ é uma zona intermédia onde ficam os serviços públicos; "
                        + "se um deles for comprometido, a rede interna continua separada."
        ));

        // Firewall 25: movimento lateral dentro da rede interna.
        add(new FirewallQuestion(
                25,
                "Que técnica permite autenticar num servidor com o hash NTLM de outra conta, "
                        + "sem descobrir a palavra-passe?",
                List.of(
                        "Pass-the-hash",
                        "SQL injection",
                        "ARP spoofing",
                        "DNS tunneling"
                ),
                0,
                "O Pass-the-hash reutiliza diretamente o hash da palavra-passe, "
                        + "por isso roubar o hash é tão perigoso como roubar a password."
        ));

        // Firewall 35: firewall de aplicação perante injeção de SQL.
        add(new FirewallQuestion(
                35,
                "Um firewall de aplicação bloqueia pedidos com o texto ' OR '1'='1. "
                        + "Que ataque está a mitigar?",
                List.of(
                        "Phishing por email",
                        "Cross-site scripting (XSS)",
                        "SQL injection",
                        "Negação de serviço distribuída (DDoS)"
                ),
                2,
                "O fragmento ' OR '1'='1 tenta transformar uma condição em sempre verdadeira; "
                        + "é a assinatura clássica de uma injeção de SQL."
        ));

        // Firewall 44: última barreira antes do Root, com um conceito de Kerberos.
        add(new FirewallQuestion(
                44,
                "No Kerberos, o que é um Golden Ticket?",
                List.of(
                        "Um ticket de serviço legítimo emitido pelo KDC.",
                        "Um TGT forjado com o hash da conta krbtgt, que dá acesso persistente ao domínio.",
                        "Um certificado digital emitido para o controlador de domínio.",
                        "Um registo de auditoria assinado pelo servidor Root."
                ),
                1,
                "Com o hash do krbtgt é possível forjar um TGT válido para qualquer utilizador; "
                        + "por isso essa conta deve ser protegida e a sua palavra-passe trocada duas vezes."
        ));
    }

    // Regista uma pergunta na lista da respetiva firewall.
    private void add(FirewallQuestion question) {
        // computeIfAbsent cria a lista apenas na primeira pergunta de cada casa.
        questions.computeIfAbsent(question.getFirewallCell(), cell -> new ArrayList<>())
                .add(question);
        // O contador começa em zero para a primeira pergunta ser a primeira da lista.
        nextIndex.putIfAbsent(question.getFirewallCell(), 0);
    }

    // Devolve a próxima pergunta da firewall, alternando se houver várias.
    public FirewallQuestion nextFor(int firewallCell) {
        // A lista pode não existir se a casa não for uma firewall configurada.
        List<FirewallQuestion> bank = questions.get(firewallCell);
        if (bank == null || bank.isEmpty()) {
            throw new IllegalStateException("Sem pergunta para a firewall " + firewallCell);
        }
        // Lê a posição atual e avança-a de forma circular.
        int index = nextIndex.getOrDefault(firewallCell, 0);
        FirewallQuestion question = bank.get(index % bank.size());
        nextIndex.put(firewallCell, (index + 1) % bank.size());
        return question;
    }

    // Permite à GUI e aos testes verificarem se a casa tem perguntas.
    public boolean hasQuestion(int firewallCell) {
        // A lista tem de existir e não estar vazia.
        List<FirewallQuestion> bank = questions.get(firewallCell);
        return bank != null && !bank.isEmpty();
    }

    // Lista imutável das perguntas de uma firewall, útil para testes.
    public List<FirewallQuestion> getQuestionsFor(int firewallCell) {
        // Devolve lista vazia em vez de null quando a casa não tem perguntas.
        return Collections.unmodifiableList(
                questions.getOrDefault(firewallCell, Collections.emptyList()));
    }

    // Número total de perguntas carregadas.
    public int size() {
        // Soma o tamanho de todas as listas.
        int total = 0;
        for (List<FirewallQuestion> bank : questions.values()) {
            total += bank.size();
        }
        return total;
    }
}
