package com.focusflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.focusflow.dto.Dto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnthropicService {

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.url}")
    private String apiUrl;

    @Value("${anthropic.api.model}")
    private String model;

    @Value("${anthropic.api.max-tokens}")
    private int maxTokens;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String PLAN_SYSTEM = """
        You are FocusFlow AI, an expert life coach and productivity planner.
        Generate clear, actionable task plans tailored to the user's goal.
        Format plans as numbered daily tasks with suggested times.
        Be motivational, realistic, and specific.
        """;

    private static final String CHAT_SYSTEM = """
        You are FocusFlow AI, an expert life coach and productivity planner.
        Help users refine their action plans based on their goals.
        When the user wants to add a task to their todo list, respond with:
        TASK_ADD: {"title":"...", "description":"...", "category":"FITNESS|WORK|LEARNING|NUTRITION|WELLNESS", "priority":"LOW|MEDIUM|HIGH"}
        You can emit multiple TASK_ADD lines.
        Keep responses concise, warm, and practical.
        """;

    private static final String REPORT_SYSTEM = """
        You are FocusFlow AI. Write an engaging, data-driven weekly productivity report.
        Include: key insights, patterns, wins to celebrate, and 3 specific recommendations for next week.
        Keep it warm, motivational, and under 300 words.
        """;

    // ── Generate initial plan ─────────────────────────────────────────────
    public String generatePlan(String goal) {
        String prompt = "My goal: " + goal + "\n\nCreate a practical 7-day task plan to achieve this goal. Format as numbered daily tasks with suggested times. Be specific and motivating.";
        return callClaude(PLAN_SYSTEM, List.of(new Dto.ChatMessage() {{
            setRole("user"); setContent(prompt);
        }}));
    }

    // ── Chat to refine plan ───────────────────────────────────────────────
    public String chatWithAgent(String goal, List<Dto.ChatMessage> history) {
        String systemWithGoal = CHAT_SYSTEM + "\nUser's goal: " + goal;
        return callClaude(systemWithGoal, history);
    }

    // ── Generate weekly AI report ─────────────────────────────────────────
    public String generateWeeklyReport(String statsSummary) {
        String prompt = "Weekly task data:\n" + statsSummary + "\n\nGenerate a motivational weekly productivity report with insights and recommendations.";
        return callClaude(REPORT_SYSTEM, List.of(new Dto.ChatMessage() {{
            setRole("user"); setContent(prompt);
        }}));
    }

    // ── Core HTTP call ────────────────────────────────────────────────────
    private String callClaude(String system, List<Dto.ChatMessage> messages) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", maxTokens);
            body.put("system", system);

            ArrayNode msgs = mapper.createArrayNode();
            for (Dto.ChatMessage m : messages) {
                ObjectNode msg = mapper.createObjectNode();
                msg.put("role", m.getRole());
                msg.put("content", m.getContent());
                msgs.add(msg);
            }
            body.set("messages", msgs);

            RequestBody requestBody = RequestBody.create(
                mapper.writeValueAsString(body),
                MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("Content-Type", "application/json")
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Anthropic API error: {} {}", response.code(), response.message());
                    return "Sorry, AI is temporarily unavailable.";
                }
                String responseBody = response.body().string();
                JsonNode root = mapper.readTree(responseBody);
                return root.path("content").get(0).path("text").asText();
            }
        } catch (Exception e) {
            log.error("Error calling Anthropic API", e);
            return "Sorry, something went wrong with the AI service.";
        }
    }
}
