package com.devloopsx.chronelis.configuration.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

public class ProjectAssistantGeminiReadyCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var environment = context.getEnvironment();

        boolean enabled = environment.getProperty("chronelis.project-assistant.enabled", Boolean.class, false);
        if (!enabled) {
            return false;
        }

        boolean vertexAi = environment.getProperty("chronelis.project-assistant.google.vertex-ai", Boolean.class,
                false);
        String apiKey = trimToNull(environment.getProperty("chronelis.project-assistant.google.api-key"));
        String projectId = trimToNull(environment.getProperty("chronelis.project-assistant.google.project-id"));
        String location = trimToNull(environment.getProperty("chronelis.project-assistant.google.location"));

        if (vertexAi) {
            return StringUtils.hasText(projectId) && StringUtils.hasText(location);
        }

        return StringUtils.hasText(apiKey);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}