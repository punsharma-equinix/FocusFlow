package com.focusflow.repository;

import com.focusflow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserIdOrderByTaskDateAscTaskTimeAsc(Long userId);

    List<Task> findByUserIdAndTaskDateOrderByTaskTimeAsc(Long userId, LocalDate date);

    List<Task> findByUserIdAndTaskDateBetweenOrderByTaskDateAscTaskTimeAsc(
        Long userId, LocalDate from, LocalDate to
    );

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.taskDate = :date AND t.done = true")
    long countCompletedByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.taskDate = :date")
    long countTotalByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.done = false AND t.alertedAt IS NULL AND t.taskDate = CURRENT_DATE")
    List<Task> findDueUnalerter();
}
