# AI Log Analyzer

A simple Spring Boot 3.5+ application that uploads a log file, extracts WARN and ERROR entries, and uses Spring AI with OpenAI to produce concise troubleshooting insights.

## Features

- Upload .log and .txt files
- Parse WARN and ERROR lines only
- Send each relevant log entry to Spring AI for analysis
- Display results in a modern Bootstrap UI
- Works locally with a simple Maven setup

## Requirements

- Java 17
- Maven
- An OpenAI API key

## How to Run

1. Create a `.env` file in the project root with your Groq API key:
   ```env
   GROQ_API_KEY=your-groq-api-key
   GROQ_MODEL=llama-3.1-8b-instant
   ```
   You can copy the sample file:
   ```bash
   copy .env.example .env
   ```
2. Run:
   - `mvn spring-boot:run`
3. Open: http://localhost:8080

The app now uses Groq instead of a local model.

## Sample curl Request

```bash
curl -X POST http://localhost:8080/api/logs/analyze -F "file=@sample.log"
```

## Sample Log File

```text
2026-06-27 INFO Application Started
2026-06-27 INFO User Login Success
2026-06-27 WARN Database Connection Slow
2026-06-27 ERROR java.lang.NullPointerException at UserService.java:45
2026-06-27 INFO Request Completed
2026-06-27 ERROR java.net.SocketTimeoutException while calling Payment API
```
