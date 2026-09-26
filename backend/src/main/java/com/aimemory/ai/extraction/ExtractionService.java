package com.aimemory.ai.extraction;

import com.aimemory.ai.groq.GroqClient;
import com.aimemory.ai.model.AIResponse;
import com.aimemory.ai.model.ChatMessage;
import com.aimemory.ai.model.ExtractedMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionService {

    private final GroqClient groqClient;
    private final ExtractionParser parser;

    /**
     * Extrai memórias de uma conversa.
     */
    public ExtractionResult extract(List<ChatMessage> conversation) {
        if (conversation == null || conversation.isEmpty()) {
            log.debug("Conversa vazia — nada a extrair");
            return new ExtractionResult(List.of(), null);
        }

        long start = System.currentTimeMillis();

        String userPrompt = ExtractionPrompt.formatConversation(conversation);

        AIResponse response = groqClient.chat(
                ExtractionPrompt.SYSTEM,
                userPrompt
        );

        List<ExtractedMemory> memories = parser.parse(response.content());

        long elapsed = System.currentTimeMillis() - start;
        log.info("🧠 Extração concluída: {} memórias em {}ms (tokens: {})",
                memories.size(), elapsed, response.totalTokens());

        return new ExtractionResult(memories, response.totalTokens());
    }

    /**
     * Resultado da extração com metadata.
     */
    public record ExtractionResult(
            List<ExtractedMemory> memories,
            Integer tokensUsed
    ) {}
}