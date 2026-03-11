package com.focusflow.controller;

import com.focusflow.dto.Dto;
import com.focusflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /** GET /api/tasks — all tasks for user */
    @GetMapping
    public ResponseEntity<List<Dto.TaskResponse>> getAllTasks(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(taskService.getAllTasks(user.getUsername()));
    }

    /** GET /api/tasks?date=2025-01-01 — tasks for a specific date */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<Dto.TaskResponse>> getByDate(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(taskService.getTasksByDate(user.getUsername(), date));
    }

    /** POST /api/tasks — create task */
    @PostMapping
    public ResponseEntity<Dto.TaskResponse> create(
        @AuthenticationPrincipal UserDetails user,
        @Valid @RequestBody Dto.TaskRequest req
    ) {
        return ResponseEntity.ok(taskService.createTask(user.getUsername(), req));
    }

    /** PUT /api/tasks/{id} — update task */
    @PutMapping("/{id}")
    public ResponseEntity<Dto.TaskResponse> update(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable Long id,
        @Valid @RequestBody Dto.TaskRequest req
    ) {
        return ResponseEntity.ok(taskService.updateTask(user.getUsername(), id, req));
    }

    /** PATCH /api/tasks/{id}/toggle — toggle done */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Dto.TaskResponse> toggleDone(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(taskService.toggleDone(user.getUsername(), id));
    }

    /** DELETE /api/tasks/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable Long id
    ) {
        taskService.deleteTask(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/tasks/{id}/end-time — update end time (max 2 times) */
    @PatchMapping("/{id}/end-time")
    public ResponseEntity<Dto.TaskResponse> updateEndTime(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable Long id,
        @Valid @RequestBody Dto.TaskRequest req
    ) {
        return ResponseEntity.ok(taskService.updateEndTime(user.getUsername(), id, req));
    }

    /** PATCH /api/tasks/{id}/start-reminder — mark start time reminder as shown */
    @PatchMapping("/{id}/start-reminder")
    public ResponseEntity<Dto.TaskResponse> markStartReminderShown(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(taskService.markStartTimeReminderShown(user.getUsername(), id));
    }

    /** PATCH /api/tasks/{id}/photo — upload completion photo */
    @PatchMapping("/{id}/photo")
    public ResponseEntity<Dto.TaskResponse> uploadPhoto(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable Long id,
        @Valid @RequestBody Dto.PhotoRequest req
    ) {
        return ResponseEntity.ok(taskService.uploadPhoto(user.getUsername(), id, req));
    }
}

