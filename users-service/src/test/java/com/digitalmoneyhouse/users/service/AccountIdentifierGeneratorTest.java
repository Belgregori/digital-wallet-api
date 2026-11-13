package com.digitalmoneyhouse.users.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountIdentifierGeneratorTest {

    @Test
    void generateCvuHasTwentyTwoDigits() {
        AccountIdentifierGenerator generator = new AccountIdentifierGenerator(List.of("sol", "luna", "rio"));

        String cvu = generator.generateCvu();

        assertEquals(22, cvu.length());
        assertTrue(cvu.chars().allMatch(Character::isDigit));
    }

    @Test
    void generateAliasUsesThreeDistinctWords() {
        AccountIdentifierGenerator generator = new AccountIdentifierGenerator(List.of("sol", "luna", "rio", "mar"));

        String[] parts = generator.generateAlias().split("\\.");

        assertEquals(3, parts.length);
        assertTrue(List.of("sol", "luna", "rio", "mar").containsAll(List.of(parts)));
        assertEquals(3, List.of(parts).stream().distinct().count());
    }

    @Test
    void loadsWordsFromClasspathFile() throws IOException {
        AccountIdentifierGenerator generator = new AccountIdentifierGenerator(new DefaultResourceLoader());

        String[] parts = generator.generateAlias().split("\\.");

        assertEquals(3, parts.length);
        assertTrue(parts[0].length() > 0);
    }
}
