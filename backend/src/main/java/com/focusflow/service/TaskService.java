package com.focusflow.service;

import com.focusflow.dto.Dto;
import com.focusflow.entity.Task;
import com.focusflow.entity.User;
import com.focusflow.repository.TaskRepository;
import com.focusflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public List<Dto.TaskResponse> getAllTasks(String email) {
        return taskRepository.findByUserIdOrderByTaskDateAscTaskTimeAsc(getUser(email).getId())
            .stream().map(Dto.TaskResponse::from).collect(Collectors.toList());
    }

    public List<Dto.TaskResponse> getTasksByDate(String email, LocalDate date) {
        return taskRepository.findByUserIdAndTaskDateOrderByTaskTimeAsc(getUser(email).getId(), date)
            .stream().map(Dto.TaskResponse::from).collect(Collectors.toList());
    }

    public Dto.TaskResponse createTask(String email, Dto.TaskRequest req) {
        User user = getUser(email);
        Task task = Task.builder()
            .user(user)
            .title(req.getTitle())
            .description(req.getDescription())
            .taskDate(req.getTaskDate())
            .taskTime(req.getTaskTime())
            .startTime(req.getStartTime())
            .endTime(req.getEndTime())
            .alarmTune(req.getAlarmTune())
            .priority(req.getPriority())
            .category(req.getCategory())
            .goalContext(req.getGoalContext())
            .endTimeChanges(0)
            .startTimeReminderShown(false)
            .build();
        return Dto.TaskResponse.from(taskRepository.save(task));
    }

    public Dto.TaskResponse updateTask(String email, Long id, Dto.TaskRequest req) {
        Task task = getOwnedTask(email, id);
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setTaskDate(req.getTaskDate());
        task.setTaskTime(req.getTaskTime());
        task.setStartTime(req.getStartTime());
        task.setEndTime(req.getEndTime());
        task.setAlarmTune(req.getAlarmTune());
        task.setPriority(req.getPriority());
        task.setCategory(req.getCategory());
        task.setGoalContext(req.getGoalContext());
        return Dto.TaskResponse.from(taskRepository.save(task));
    }

    public Dto.TaskResponse toggleDone(String email, Long id) {
        Task task = getOwnedTask(email, id);
        task.setDone(!task.getDone());
        return Dto.TaskResponse.from(taskRepository.save(task));
    }

    public void deleteTask(String email, Long id) {
        taskRepository.delete(getOwnedTask(email, id));
    }

    public Dto.TaskResponse updateEndTime(String email, Long id, Dto.TaskRequest req) {
        Task task = getOwnedTask(email, id);
        if (task.getEndTimeChanges() >= 2) {
            throw new IllegalArgumentException("Cannot change end time - maximum 2 changes allowed");
        }
        task.setEndTime(req.getEndTime());
        task.setEndTimeChanges(task.getEndTimeChanges() + 1);
        return Dto.TaskResponse.from(taskRepository.save(task));
    }

    public Dto.TaskResponse markStartTimeReminderShown(String email, Long id) {
        Task task = getOwnedTask(email, id);
        task.setStartTimeReminderShown(true);
        return Dto.TaskResponse.from(taskRepository.save(task));
    }

    public Dto.TaskResponse uploadPhoto(String email, Long id, Dto.PhotoRequest req) {
        Task task = getOwnedTask(email, id);
        task.setCompletionPhotoUrl(req.getPhotoData());
        return Dto.TaskResponse.from(taskRepository.save(task));
    }

    private Task getOwnedTask(String email, Long id) {
        User user = getUser(email);
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (!task.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Access denied");
        }
        return task;
    }
}
