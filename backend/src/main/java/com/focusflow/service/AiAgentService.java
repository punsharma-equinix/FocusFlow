package com.focusflow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.focusflow.dto.Dto;
import com.focusflow.entity.AiPlan;
import com.focusflow.entity.Task;
import com.focusflow.entity.User;
import com.focusflow.repository.AiPlanRepository;
import com.focusflow.repository.TaskRepository;
import com.focusflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAgentService {

    private final AnthropicService anthropicService;
    private final AiPlanRepository aiPlanRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    // ── Generate initial plan ──────────────────────────────────────────────
    public Dto.AiPlanResponse generatePlan(String email, String goal) {
        User user = getUser(email);
        requireProPlan(user);

        String plan = anthropicService.generatePlan(goal);

        AiPlan aiPlan = AiPlan.builder()
            .user(user)
            .goal(goal)
            .generatedPlan(plan)
            .chatHistory("[]")
            .build();
        aiPlan = aiPlanRepository.save(aiPlan);

        return toResponse(aiPlan);
    }

    // ── AI chat refinement ─────────────────────────────────────────────────
    public Dto.AiChatResponse chat(String email, Dto.AiChatRequest req) {
        User user = getUser(email);
        requireProPlan(user);

        // Fetch plan to get goal
        AiPlan aiPlan = aiPlanRepository.findById(req.getPlanId())
            .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        // Build full history for context
        List<Dto.ChatMessage> history = req.getHistory() != null ? req.getHistory() : new ArrayList<>();
        history.add(new Dto.ChatMessage() {{ setRole("user"); setContent(req.getMessage()); }});

        String reply = anthropicService.chatWithAgent(aiPlan.getGoal(), history);

        // Add assistant reply to history
        history.add(new Dto.ChatMessage() {{ setRole("assistant"); setContent(reply); }});

        // Extract tasks to add (TASK_ADD: {...})
        List<Task> addedTasks = extractAndSaveTasks(reply, user, aiPlan.getGoal());

        // Persist updated chat history
        try {
            aiPlan.setChatHistory(mapper.writeValueAsString(history));
            aiPlan.setTasksAdded(aiPlan.getTasksAdded() + addedTasks.size());
            aiPlanRepository.save(aiPlan);
        } catch (Exception e) {
            log.warn("Could not save chat history", e);
        }

        Dto.AiChatResponse response = new Dto.AiChatResponse();
        response.setReply(reply);
        response.setPlanId(aiPlan.getId());
        response.setTasksAdded(addedTasks.stream().map(Dto.TaskResponse::from).collect(Collectors.toList()));
        return response;
    }

    // ── Get all plans for user ─────────────────────────────────────────────
    public List<Dto.AiPlanResponse> getPlans(String email) {
        return aiPlanRepository.findByUserIdOrderByCreatedAtDesc(getUser(email).getId())
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Extract TASK_ADD: {json} blocks from AI reply ─────────────────────
    private List<Task> extractAndSaveTasks(String reply, User user, String goal) {
        List<Task> tasks = new ArrayList<>();
        Pattern pattern = Pattern.compile("TASK_ADD:\\s*(\\{[^}]+\\})");
        Matcher matcher = pattern.matcher(reply);
        while (matcher.find()) {
            try {
                String json = matcher.group(1);
                var node = mapper.readTree(json);
                Task task = Task.builder()
                    .user(user)
                    .title(node.path("title").asText("AI Task"))
                    .description(node.path("description").asText(""))
                    .taskDate(LocalDate.now().plusDays(1))
                    .taskTime(LocalTime.of(9, 0))
                    .priority(parseEnum(Task.Priority.class, node.path("priority").asText(), Task.Priority.MEDIUM))
                    .category(parseEnum(Task.Category.class, node.path("category").asText(), Task.Category.AI_PLAN))
                    .aiGenerated(true)
                    .goalContext(goal)
                    .build();
                tasks.add(taskRepository.save(task));
            } catch (Exception e) {
                log.warn("Could not parse TASK_ADD block: {}", e.getMessage());
            }
        }
        return tasks;
    }

    private <T extends Enum<T>> T parseEnum(Class<T> cls, String val, T fallback) {
        try { return Enum.valueOf(cls, val.toUpperCase()); }
        catch (Exception e) { return fallback; }
    }

    private Dto.AiPlanResponse toResponse(AiPlan p) {
        Dto.AiPlanResponse r = new Dto.AiPlanResponse();
        r.setId(p.getId());
        r.setGoal(p.getGoal());
        r.setGeneratedPlan(p.getGeneratedPlan());
        r.setTasksAdded(p.getTasksAdded());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void requireProPlan(User user) {
        if (user.getPlan() == User.Plan.FREE) {
            throw new IllegalStateException("AI Agent requires Pro or Premium plan");
        }
    }
}
