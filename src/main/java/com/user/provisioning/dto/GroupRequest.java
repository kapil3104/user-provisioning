package com.user.provisioning.dto;


import com.user.provisioning.entity.DynamicGroupRule;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class GroupRequest {
    @NonNull
    private String name;
    @NonNull
    private String type; // "Static" or "Dynamic"
    private String description;
    private List<DynamicGroupRule> dynamicGroupRules;
}
