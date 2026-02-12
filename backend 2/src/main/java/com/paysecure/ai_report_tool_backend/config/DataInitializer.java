package com.paysecure.ai_report_tool_backend.config;

import com.paysecure.ai_report_tool_backend.service.SubscriptionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeData(SubscriptionService subscriptionService) {
        return args -> {
            subscriptionService.initializeDefaultPlans();
        };
    }
}
