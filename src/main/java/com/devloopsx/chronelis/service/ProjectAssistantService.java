package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.projectassistant.ProjectAssistantApplyRequest;
import com.devloopsx.chronelis.dto.request.projectassistant.ProjectAssistantPreviewRequest;
import com.devloopsx.chronelis.dto.response.projectassistant.ProjectAssistantApplyResponse;
import com.devloopsx.chronelis.dto.response.projectassistant.ProjectAssistantPreviewResponse;
import com.devloopsx.chronelis.dto.response.projectassistant.ProjectAssistantStatusResponse;

public interface ProjectAssistantService {
    ProjectAssistantStatusResponse getStatus();

    ProjectAssistantPreviewResponse previewProject(Long projectId, ProjectAssistantPreviewRequest request);

    ProjectAssistantApplyResponse applyPlan(Long projectId, ProjectAssistantApplyRequest request);
}