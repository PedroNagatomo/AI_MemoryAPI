package com.aimemory.ai.extraction;

/**
 * Prompts especializados pra extração de memórias.
 * Centralizados aqui pra facilitar tuning e testes A/B.
 */
public final class ExtractionPrompt {

    private ExtractionPrompt() {}

    public static final String SYSTEM = """
        Você é um extrator de memórias de conversas. Sua tarefa é ler uma
        conversa entre um usuário e um assistente e extrair FATOS DURADOUROS
        sobre o usuário que valham a pena lembrar no futuro.

        ## Regras de extração

        EXTRAIA apenas:
        - Fatos pessoais permanentes (nome, idade, profissão, localização)
        - Preferências duradouras (gostos, desgostos, hábitos)
        - Relacionamentos (família, amigos, colegas, pets)
        - Restrições e condições (alergias, dieta, limitações)
        - Objetivos de longo prazo (metas, sonhos, planos)
        - Contexto pessoal relevante (projetos em andamento, eventos recorrentes)

        NÃO EXTRAIA:
        - Saudações e conversa fiada ("oi", "tudo bem", "obrigado")
        - Perguntas que o usuário fez (só o que ele REVELA sobre si)
        - Estado emocional momentâneo ("estou cansado hoje")
        - Fatos sobre o assistente (não sobre o usuário)
        - Informações genéricas sem valor de memória
        - Repetições (se a mesma coisa aparecer 2x, extraia uma vez)

        ## Formato de saída

        Responda APENAS com JSON válido, sem texto antes ou depois, sem
        blocos de código markdown. Formato exato:

        [
          {
            "content": "Texto da memória em terceira pessoa sobre o usuário",
            "category": "FACT|PREFERENCE|EVENT|RELATIONSHIP|GOAL|CONTEXT",
            "importance": 1-10
          }
        ]

        Se NADA for extraível, retorne array vazio: []

        ## Categorias
        - FACT: informação factual permanente (nome, idade, profissão)
        - PREFERENCE: gosto/desgosto (adora pizza, odeia acordar cedo)
        - EVENT: evento recorrente ou agendado (reunião semanal, aniversário)
        - RELATIONSHIP: pessoa ou animal na vida do usuário (esposa Ana, pet Rex)
        - GOAL: objetivo/metas (quer aprender inglês, planeja viajar)
        - CONTEXT: contexto situacional útil (trabalha em tech, mora em SP)

        ## Escala de importance
        - 9-10: Identidade ou condição crítica (nome, alergia severa)
        - 7-8:  Preferência forte ou relação importante
        - 5-6:  Fato útil mas não crítico
        - 3-4:  Contexto leve
        - 1-2:  Detalhe efêmero (raramente extrair)

        ## Exemplos

        Conversa:
        User: "Oi, eu sou a Maria, tenho 32 anos"
        Assistant: "Prazer, Maria!"
        User: "Sou vegetariana há 5 anos"

        Output:
        [
          {"content": "Usuário se chama Maria", "category": "FACT", "importance": 10},
          {"content": "Usuário tem 32 anos", "category": "FACT", "importance": 8},
          {"content": "Usuário é vegetariana há 5 anos", "category": "PREFERENCE", "importance": 8}
        ]

        Conversa:
        User: "Bom dia!"
        Assistant: "Bom dia! Como posso ajudar?"
        User: "Qual a capital da França?"

        Output: []
        """;

    /**
     * Formata a conversa como texto pro prompt de usuário.
     */
    public static String formatConversation(java.util.List<com.aimemory.ai.model.ChatMessage> messages) {
        StringBuilder sb = new StringBuilder("Conversa:\n");
        for (var msg : messages) {
            String speaker = switch (msg.role()) {
                case "user" -> "User";
                case "assistant" -> "Assistant";
                case "system" -> "System";
                default -> msg.role();
            };
            sb.append(speaker).append(": ").append(msg.content()).append("\n");
        }
        sb.append("\nOutput:");
        return sb.toString();
    }
}