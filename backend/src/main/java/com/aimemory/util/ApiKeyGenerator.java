package com.aimemory.util;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

@UtilityClass
public class ApiKeyGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int KEY_LENGTH = 32;

    /**
     * Gera API key no formato: amk_{env}_{32 chars}
     */
    public static String generate(String environment) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < KEY_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return "amk_" + environment.toLowerCase() + "_" + sb;
    }

    /**
     * Extrai prefixo identificável (primeiros 12 chars) para lookup rápido.
     * Ex: "amk_live_abc"
     */
    public static String extractPrefix(String apiKey) {
        if (apiKey == null || apiKey.length() < 12) {
            throw new IllegalArgumentException("API key inválida");
        }
        return apiKey.substring(0, 12);
    }

    /**
     * SHA-256 hash da API key. Nunca armazenar plain.
     */
    public static String hash(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }
}