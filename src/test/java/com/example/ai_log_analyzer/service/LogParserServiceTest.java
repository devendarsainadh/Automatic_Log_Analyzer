package com.example.ai_log_analyzer.service;

import com.example.ai_log_analyzer.model.LogEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LogParserServiceTest {

    private final LogParserService parserService = new LogParserService();

    @Test
    void parsesWarnAndErrorEntriesAndSkipsInfoLines() {
        String content = """
                2026-06-27 INFO Application Started
                2026-06-27 WARN Database Connection Slow
                2026-06-27 ERROR java.lang.NullPointerException at UserService.java:45
                2026-06-27 INFO Request Completed
                """;

        List<LogEntry> entries = parserService.parse(content);

        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).getLevel()).isEqualTo("WARN");
        assertThat(entries.get(0).getMessage()).contains("Database Connection Slow");
        assertThat(entries.get(1).getLevel()).isEqualTo("ERROR");
        assertThat(entries.get(1).getMessage()).contains("NullPointerException");
    }

    @Test
    void parsesCommonLogFormatsWithTimestampAndSeverity() {
        String content = """
                2026-06-27 14:23:11 WARN Database connection is slow
                2026-06-27 14:23:12 ERROR java.net.SocketTimeoutException
                """;

        List<LogEntry> entries = parserService.parse(content);

        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).getLevel()).isEqualTo("WARN");
        assertThat(entries.get(1).getLevel()).isEqualTo("ERROR");
    }

    @Test
    void parsesYourSampleLogFormatWithBracketsAndStackTraces() {
        String content = """
                2026-06-27 09:00:01 INFO  [Application] Starting AI Log Analyzer...
                2026-06-27 09:02:15 WARN  [Database] Connection pool utilization is above 85%.
                2026-06-27 09:03:42 ERROR [UserService] java.lang.NullPointerException: Cannot invoke "User.getName()" because "user" is null
                    at com.example.service.UserService.getUser(UserService.java:45)
                    at com.example.controller.UserController.getUser(UserController.java:28)
                2026-06-27 09:05:33 ERROR [PaymentService] java.net.SocketTimeoutException: Read timed out while calling Payment API
                """;

        List<LogEntry> entries = parserService.parse(content);

        assertThat(entries).hasSize(3);
        assertThat(entries.get(0).getLevel()).isEqualTo("WARN");
        assertThat(entries.get(1).getLevel()).isEqualTo("ERROR");
        assertThat(entries.get(1).getMessage()).contains("NullPointerException");
    }
}
