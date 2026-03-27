package com.devloopsx.chronelis.domain;

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
@Table(name = "task_statuses", uniqueConstraints = {
        @UniqueConstraint(name = "uk_task_statuses_project_code", columnNames = { "project_id", "code" })
})
public class TaskStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @Column(nullable = false, length = 100)
    String name;

    @Column(nullable = false, length = 50)
    String code;

    @Column(nullable = false)
    Integer position;

    @Column(name = "is_closed", nullable = false)
    Boolean isClosed;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "status", fetch = FetchType.LAZY)
    List<Task> tasks = new ArrayList<>();
}
