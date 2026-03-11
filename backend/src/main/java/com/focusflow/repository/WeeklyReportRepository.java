package com.focusflow.repository;

import com.focusflow.entity.WeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {
    List<WeeklyReport> findByUserIdOrderByWeekStartDesc(Long userId);
    Optional<WeeklyReport> findByUserIdAndWeekStart(Long userId, LocalDate weekStart);
}
