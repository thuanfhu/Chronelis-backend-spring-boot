package com.devloopsx.chronelis.service.projectassistant;

import com.devloopsx.chronelis.configuration.properties.ProjectAssistantProperties;
import com.devloopsx.chronelis.dto.projectassistant.ProjectAssistantPlan;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeminiProjectAssistantAiGateway implements ProjectAssistantAiGateway {
    ChatClient chatClient;
    ProjectAssistantProperties properties;

    @Override
    public ProjectAssistantPlan generatePlan(String systemPrompt, String userPrompt) {
        Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(userPrompt)));

        ProjectAssistantPlan plan = this.chatClient.prompt(prompt)
                .advisors(StructuredOutputValidationAdvisor.builder()
                        .outputType(ProjectAssistantPlan.class)
                        .maxRepeatAttempts(this.properties.getValidationRetryAttempts())
                        .build())
                .call()
                .entity(ProjectAssistantPlan.class);

        if (plan == null) {
            throw new ApplicationException(ErrorCode.PROJECT_ASSISTANT_UNAVAILABLE,
                    "Không thể tạo kế hoạch AI hợp lệ từ Gemini.");
        }

        return plan;
    }
}