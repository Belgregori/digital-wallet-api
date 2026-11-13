package com.digitalmoneyhouse.users.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AccountIdentifierGenerator {

    private static final int CVU_LENGTH = 22;
    private static final int ALIAS_WORD_COUNT = 3;

    private final List<String> words;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public AccountIdentifierGenerator(ResourceLoader resourceLoader) throws IOException {
        this(loadWords(resourceLoader));
    }

    AccountIdentifierGenerator(List<String> words) {
        this.words = List.copyOf(words);
        if (this.words.size() < ALIAS_WORD_COUNT) {
            throw new IllegalStateException("palabras.txt debe contener al menos 3 palabras");
        }
    }

    public String generateCvu() {
        StringBuilder cvu = new StringBuilder(CVU_LENGTH);
        for (int i = 0; i < CVU_LENGTH; i++) {
            cvu.append(random.nextInt(10));
        }
        return cvu.toString();
    }

    public String generateAlias() {
        List<String> shuffled = new ArrayList<>(words);
        Collections.shuffle(shuffled, random);
        return String.join(".", shuffled.subList(0, ALIAS_WORD_COUNT));
    }

    private static List<String> loadWords(ResourceLoader resourceLoader) throws IOException {
        var resource = resourceLoader.getResource("classpath:palabras.txt");
        try (var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> words = reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .distinct()
                    .toList();
            if (words.size() < ALIAS_WORD_COUNT) {
                throw new IllegalStateException("palabras.txt debe contener al menos 3 palabras");
            }
            return words;
        }
    }
}
