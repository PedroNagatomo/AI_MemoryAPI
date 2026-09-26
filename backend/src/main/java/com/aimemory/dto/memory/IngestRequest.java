package com.aimemory.dto.memory;

import com.aimemory.ai.model.ChatMessage;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.List;
import java.util.UUID;

public record IngestRequest(
        @Schema(description = "ID do end-user (retornado ao criar)", example = "9e6ba9a7-cae6-4503-ac44-8ab673dcf8da")
        @NotNull UUID endUserId,

        @Schema(description = "Conversa a ingerir (max 100 mensagens)")
        @NotEmpty @Size(max = 100)  // limite de 100 mensagens por ingest
        List<Message> messages,

        // opcional: ID externo da conversa pra rastreabilidade
        @Schema(description = "ID externo da conversa (rastreabilidade)", example = "conv_001")
        @Size(max = 255)
        String sourceId
) {
    public record Message(
            @Schema(description = "Role", example = "user", allowableValues = {"user", "assistant", "system"})
            @NotNull @Size(max = 20) String role,
            @Schema(description = "Conteúdo da mensagem", example = "Oi, sou Pedro")
            @NotNull @Size(max = 10000) String content
    ) {}

    public List<ChatMessage> toChatMessages() {
        return messages.stream()
                .map(m -> new ChatMessage(m.role(), m.content()))
                .toList();
    }
}