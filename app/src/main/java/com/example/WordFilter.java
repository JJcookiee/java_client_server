package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class WordFilter {
    private Set<String> bannedWords = new HashSet<>();

    public WordFilter() {
        try (BufferedReader reader = new BufferedReader(new FileReader("files/banned_words.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                bannedWords.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            FileHandler.Debug("Could not load banned words: " + e.getMessage());
        }
    }

    public String censorMessage(String m) {
        String cm = m;
        for (String bannedWord : bannedWords) {
            if (m.toLowerCase().contains(bannedWord)) {
                String regex = "(?i)\\b" + Pattern.quote(bannedWord) + "\\b";
                String censor = "*".repeat(bannedWord.length());
                cm = cm.replaceAll(regex, censor);
            }
        }
        return cm;
    }
}
