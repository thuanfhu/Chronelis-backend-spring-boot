package com.devloopsx.chronelis.domain;

import com.devloopsx.chronelis.constant.GoalStatusType;
import com.devloopsx.chronelis.constant.GoalType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
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
@Table(name = "goals")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @Column(nullable = false, length = 200)
    String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 30)
    GoalType goalType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    GoalStatusType status;

    @Column(name = "progress_percent", nullable = false, precision = 5, scale = 2)
    BigDecimal progressPercent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    User createdBy;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "goal", fetch = FetchType.LAZY)
    List<Task> tasks = new ArrayList<>();
}
