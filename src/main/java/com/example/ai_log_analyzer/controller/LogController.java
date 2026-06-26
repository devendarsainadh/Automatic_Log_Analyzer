package com.example.ai_log_analyzer.controller;

import com.example.ai_log_analyzer.model.AnalysisResult;
import com.example.ai_log_analyzer.model.LogEntry;
import com.example.ai_log_analyzer.service.AIAnalysisService;
import com.example.ai_log_analyzer.service.LogParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class LogController {

    private final LogParserService logParserService;
    private final AIAnalysisService aiAnalysisService;

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    @PostMapping("/api/logs/analyze")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> analyzeLogs(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please upload a log file."));
        }

        try {
            String content = new String(file.getBytes());
            List<LogEntry> parsedLogs = logParserService.parse(content);

            if (parsedLogs.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "totalLogs", 0,
                        "analyzedLogs", 0,
                        "results", List.of(),
                        "message", "No analyzable logs found."
                ));
            }

            List<AnalysisResult> results = aiAnalysisService.analyze(parsedLogs);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("totalLogs", content.split("\\R").length);
            response.put("analyzedLogs", parsedLogs.size());
            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("error", "AI analysis failed: " + ex.getMessage()));
        }
    }
}
