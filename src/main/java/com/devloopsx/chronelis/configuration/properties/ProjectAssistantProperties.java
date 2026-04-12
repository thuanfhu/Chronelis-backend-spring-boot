package com.devloopsx.chronelis.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ConfigurationProperties(prefix = "chronelis.project-assistant")
public class ProjectAssistantProperties {
    private boolean enabled = false;

    private int maxPreviewActions = 12;

    private int contextGoalLimit = 50;

    private int contextTaskLimit = 200;

    private int contextScheduleLimit = 150;

    private int validationRetryAttempts = 2;

    private Google google = new Google();

    public boolean hasReadyGoogleConfiguration() {
        if (!this.enabled) {
            return false;
        }

        if (this.google.isVertexAi()) {
            return StringUtils.hasText(this.google.getProjectId()) && StringUtils.hasText(this.google.getLocation());
        }

        return StringUtils.hasText(this.google.getApiKey());
    }

    @Getter
    @Setter
    public static class Google {
        private boolean vertexAi = false;

        private String apiKey;

        private String projectId;

        private String location;

        private Resource credentialsUri;

        private String model = "gemini-2.5-flash";

        private double temperature = 0.15d;

        private int maxOutputTokens = 4096;
    }
}