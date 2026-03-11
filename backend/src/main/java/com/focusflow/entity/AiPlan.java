package com.focusflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String goal;

    @Column(name = "generated_plan", columnDefinition = "TEXT")
    private String generatedPlan;

    // Full chat history serialized as JSON
    @Column(name = "chat_history", columnDefinition = "TEXT")
    private String chatHistory;

    @Column(name = "tasks_added")
    @Builder.Default
    private Integer tasksAdded = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
