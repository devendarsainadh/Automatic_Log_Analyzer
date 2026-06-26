package com.example.ai_log_analyzer.service;

import com.example.ai_log_analyzer.model.LogEntry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LogParserService {

    private static final Pattern LOG_PATTERN = Pattern.compile(".*\\b(WARN|ERROR)\\b(.*)$");

    public List<LogEntry> parse(String content) {
        List<LogEntry> entries = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return entries;
        }

        for (String line : content.split("\\R")) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }

            Matcher matcher = LOG_PATTERN.matcher(trimmedLine);
            if (!matcher.matches()) {
                continue;
            }

            String level = matcher.group(1);
            String message = matcher.group(2).trim();

            entries.add(LogEntry.builder()
                    .timestamp("")
                    .level(level)
                    .message(message)
                    .build());
        }

        return entries;
    }
}
