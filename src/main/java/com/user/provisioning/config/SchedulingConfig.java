package com.user.provisioning.config;

import com.user.provisioning.service.DynamicGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final DynamicGroupService dynamicGroupService;

    @Scheduled(cron = "0 0 * * * ?")
    public void scheduleDynamicGroupEvaluation() {
        dynamicGroupService.evaluateAndUpdateDynamicGroups();
    }
}

