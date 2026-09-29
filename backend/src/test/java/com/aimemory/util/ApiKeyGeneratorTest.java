package com.aimemory.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiKeyGeneratorTest {

    @Test
    void generate_shouldCreateKeyWithCorrectFormat() {
        String key = ApiKeyGenerator.generate("TEST");

        assertThat(key).startsWith("amk_test_");
        assertThat(key).hasSize(41);
    }

    @Test
    void generate_shouldCreateUniqueKeys() {
        String key1 = ApiKeyGenerator.generate("TEST");
        String key2 = ApiKeyGenerator.generate("TEST");

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void extractPrefix_shouldReturnFirst12Chars() {
        String key = "amk_live_abc123xyz456";

        String prefix = ApiKeyGenerator.extractPrefix(key);

        assertThat(prefix).isEqualTo("amk_live_abc");
    }

    @Test
    void extractPrefix_shouldThrowOnShortKey() {
        assertThatThrownBy(() -> ApiKeyGenerator.extractPrefix("short"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hash_shouldBeDeterministic() {
        String key = "amk_test_samekey";

        String hash1 = ApiKeyGenerator.hash(key);
        String hash2 = ApiKeyGenerator.hash(key);

        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64);
    }

    @Test
    void hash_shouldDifferForDifferentKeys() {
        String hash1 = ApiKeyGenerator.hash("key1");
        String hash2 = ApiKeyGenerator.hash("key2");

        assertThat(hash1).isNotEqualTo(hash2);
    }
}