package com.devloopsx.chronelis.domain;

import com.devloopsx.chronelis.constant.ImportanceLevel;
import com.devloopsx.chronelis.constant.SourceViewType;
import com.devloopsx.chronelis.constant.TaskPriorityType;
import com.devloopsx.chronelis.constant.UrgencyLevel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    Goal goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    TaskStatus status;

    @Column(nullable = false, length = 200)
    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    TaskPriorityType priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_type_id")
    TaskType taskType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    User createdBy;

    @Column(name = "due_date")
    LocalDateTime dueDate;

    @Column(name = "estimated_minutes", nullable = false)
    Integer estimatedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "importance_level", length = 10)
    ImportanceLevel importanceLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level", length = 10)
    UrgencyLevel urgencyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_view", nullable = false, length = 20)
    @Builder.Default
    SourceViewType sourceView = SourceViewType.KANBAN;

    @Column(name = "board_position", nullable = false)
    Integer boardPosition;

    @Column(name = "is_completed", nullable = false)
    Boolean isCompleted;

    @Column(name = "completed_at")
    LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    List<TaskSchedule> schedules = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    List<TaskComment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    List<TaskCheckItem> checkItems = new ArrayList<>();
}
