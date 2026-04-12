package com.devloopsx.chronelis.service.projectassistant;

import com.devloopsx.chronelis.dto.projectassistant.ProjectAssistantPlan;

public interface ProjectAssistantAiGateway {
    ProjectAssistantPlan generatePlan(String systemPrompt, String userPrompt);
}