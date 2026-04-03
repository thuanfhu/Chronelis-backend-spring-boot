package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.goal.CreateGoalRequest;
import com.devloopsx.chronelis.dto.request.goal.UpdateGoalRequest;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.goal.GoalResponse;
import org.springframework.data.domain.Pageable;

public interface GoalService {
    GoalResponse createGoal(CreateGoalRequest request);

    GoalResponse updateGoal(Long goalId, UpdateGoalRequest request);

    GoalResponse getGoal(Long goalId);

    PaginationResponse listGoalsByProject(Long projectId, Pageable pageable);

    void deleteGoal(Long goalId);

    void recalculateGoalProgress(Long goalId);
}
