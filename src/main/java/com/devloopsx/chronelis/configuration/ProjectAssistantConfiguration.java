package com.devloopsx.chronelis.configuration;

import com.devloopsx.chronelis.configuration.condition.ProjectAssistantGeminiReadyCondition;
import com.devloopsx.chronelis.configuration.properties.ProjectAssistantProperties;
import com.devloopsx.chronelis.service.projectassistant.GeminiProjectAssistantAiGateway;
import com.devloopsx.chronelis.service.projectassistant.ProjectAssistantAiGateway;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import java.io.IOException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProjectAssistantProperties.class)
public class ProjectAssistantConfiguration {
    @Bean(name = "projectAssistantGenAiClient")
    @org.springframework.context.annotation.Conditional(ProjectAssistantGeminiReadyCondition.class)
    Client projectAssistantGenAiClient(ProjectAssistantProperties properties) throws IOException {
        var google = properties.getGoogle();
        Client.Builder builder = Client.builder();

        if (google.isVertexAi()) {
            builder.project(google.getProjectId()).location(google.getLocation()).vertexAI(true);
            if (google.getCredentialsUri() != null) {
                try (var inputStream = google.getCredentialsUri().getInputStream()) {
                    builder.credentials(GoogleCredentials.fromStream(inputStream));
                }
            }
        } else {
            builder.apiKey(google.getApiKey());
        }

        return builder.build();
    }

    @Bean(name = "projectAssistantChatModel")
    @ConditionalOnBean(name = "projectAssistantGenAiClient")
    GoogleGenAiChatModel projectAssistantChatModel(
            @Qualifier("projectAssistantGenAiClient") Client projectAssistantGenAiClient,
            ProjectAssistantProperties properties) {
        var google = properties.getGoogle();
        GoogleGenAiChatOptions defaultOptions = GoogleGenAiChatOptions.builder()
                .model(google.getModel())
                .temperature(google.getTemperature())
                .maxOutputTokens(google.getMaxOutputTokens())
                .build();

        return GoogleGenAiChatModel.builder()
                .genAiClient(projectAssistantGenAiClient)
                .defaultOptions(defaultOptions)
                .build();
    }

    @Bean(name = "projectAssistantChatClient")
    @ConditionalOnBean(name = "projectAssistantChatModel")
    ChatClient projectAssistantChatClient(
            @Qualifier("projectAssistantChatModel") GoogleGenAiChatModel projectAssistantChatModel) {
        return ChatClient.builder(projectAssistantChatModel).build();
    }

    @Bean
    @ConditionalOnBean(name = "projectAssistantChatClient")
    ProjectAssistantAiGateway projectAssistantAiGateway(
            @Qualifier("projectAssistantChatClient") ChatClient projectAssistantChatClient,
            ProjectAssistantProperties properties) {
        return new GeminiProjectAssistantAiGateway(projectAssistantChatClient, properties);
    }
}