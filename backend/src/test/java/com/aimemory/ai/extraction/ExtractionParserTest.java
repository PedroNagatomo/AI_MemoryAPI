package com.aimemory.ai.extraction;

import com.aimemory.ai.model.ExtractedMemory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExtractionParserTest {

    private ExtractionParser parser;

    @BeforeEach
    void setup() {
        parser = new ExtractionParser(new ObjectMapper());
    }

    @Test
    void parse_shouldHandlePerfectJson() {
        String json = """
            [
              {"content": "User is named Pedro", "category": "FACT", "importance": 10},
              {"content": "User loves coffee", "category": "PREFERENCE", "importance": 7}
            ]
            """;

        List<ExtractedMemory> result = parser.parse(json);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).content()).isEqualTo("User is named Pedro");
        assertThat(result.get(0).category()).isEqualTo("FACT");
        assertThat(result.get(0).importance()).isEqualTo(10);
    }

    @Test
    void parse_shouldStripMarkdownCodeFence() {
        String json = """
            ```json
            [
              {"content": "Test", "category": "FACT", "importance": 5}
            ]
""";

        List<ExtractedMemory> result = parser.parse(json);

        assertThat(result).hasSize(1);
    }

    @Test
    void parse_shouldExtractJsonFromSurroundingText() {
        String json = """
Aqui está o resultado:
[{"content": "Test", "category": "FACT", "importance": 5}]
Fim.
""";

        List<ExtractedMemory> result = parser.parse(json);

        assertThat(result).hasSize(1);
    }

    @Test
    void parse_shouldNormalizeUnknownCategory() {
        String json = """
[{"content": "Test", "category": "UNKNOWN_CATEGORY", "importance": 5}]
""";

        List<ExtractedMemory> result = parser.parse(json);

        assertThat(result.get(0).category()).isEqualTo("FACT");
    }

    @Test
    void parse_shouldClampImportance() {
        String json = """
[
{"content": "Test1", "category": "FACT", "importance": 15},
{"content": "Test2", "category": "FACT", "importance": -3}
]
""";

        List<ExtractedMemory> result = parser.parse(json);

        assertThat(result.get(0).importance()).isEqualTo(10);
        assertThat(result.get(1).importance()).isEqualTo(1);
    }

    @Test
    void parse_shouldReturnEmptyListForEmptyInput() {
        assertThat(parser.parse("")).isEmpty();
        assertThat(parser.parse(" ")).isEmpty();
        assertThat(parser.parse(null)).isEmpty();
    }

    @Test
    void parse_shouldThrowOnInvalidJson() {
        assertThatThrownBy(() -> parser.parse("not json at all"))
                .isInstanceOf(ExtractionException.class);
    }

    @Test
    void parse_shouldFilterOutBlankContent() {
        String json = """
[
{"content": "Valid", "category": "FACT", "importance": 5},
{"content": " ", "category": "FACT", "importance": 5}
]
""";

        List<ExtractedMemory> result = parser.parse(json);

        assertThat(result).hasSize(1);
    }
}