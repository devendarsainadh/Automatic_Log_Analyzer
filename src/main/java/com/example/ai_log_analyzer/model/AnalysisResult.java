package com.example.ai_log_analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private String errorType;
    private String severity;
    private String rootCause;
    private String suggestedFix;
    private String summary;
    private String originalLog;
}
