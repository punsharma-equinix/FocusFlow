package com.focusflow.controller;

import com.focusflow.dto.Dto;
import com.focusflow.service.AiAgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAgentController {

    private final AiAgentService aiAgentService;

    /** POST /api/ai/plan — generate initial plan from goal */
    @PostMapping("/plan")
    public ResponseEntity<Dto.AiPlanResponse> generatePlan(
        @AuthenticationPrincipal UserDetails user,
        @Valid @RequestBody Dto.AiPlanRequest req
    ) {
        return ResponseEntity.ok(aiAgentService.generatePlan(user.getUsername(), req.getGoal()));
    }

    /** POST /api/ai/chat — chat to refine the plan */
    @PostMapping("/chat")
    public ResponseEntity<Dto.AiChatResponse> chat(
        @AuthenticationPrincipal UserDetails user,
        @Valid @RequestBody Dto.AiChatRequest req
    ) {
        return ResponseEntity.ok(aiAgentService.chat(user.getUsername(), req));
    }

    /** GET /api/ai/plans — all saved plans */
    @GetMapping("/plans")
    public ResponseEntity<List<Dto.AiPlanResponse>> getPlans(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(aiAgentService.getPlans(user.getUsername()));
    }
}
