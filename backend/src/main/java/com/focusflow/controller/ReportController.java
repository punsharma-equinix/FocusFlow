package com.focusflow.controller;

import com.focusflow.dto.Dto;
import com.focusflow.entity.User;
import com.focusflow.repository.UserRepository;
import com.focusflow.service.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ── Weekly Report ─────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
class ReportController {

    private final WeeklyReportService reportService;

    /** GET /api/reports/current — generate or fetch this week's report */
    @GetMapping("/current")
    public ResponseEntity<Dto.WeeklyReportResponse> getCurrentReport(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reportService.getCurrentWeekReport(user.getUsername()));
    }

    /** GET /api/reports — all past reports */
    @GetMapping
    public ResponseEntity<List<Dto.WeeklyReportResponse>> getHistory(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reportService.getReportHistory(user.getUsername()));
    }
}

// ── User / Profile ────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
class UserController {

    private final UserRepository userRepository;

    /** GET /api/users/me */
    @GetMapping("/me")
    public ResponseEntity<Dto.UserResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(Dto.UserResponse.from(user));
    }

    /** PATCH /api/users/me/plan — upgrade plan (hook Stripe here in prod) */
    @PatchMapping("/me/plan")
    public ResponseEntity<Dto.UserResponse> upgradePlan(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody Dto.UpgradePlanRequest req
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPlan(req.getPlan());
        // TODO: verify Stripe payment token before upgrading
        userRepository.save(user);
        return ResponseEntity.ok(Dto.UserResponse.from(user));
    }
}
