package com.user.provisioning.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "dynamic_group_rules")
@Data
public class DynamicGroupRule {
    @Id
    private String id;
    private String groupId;
    private String attribute;
    private String operation;
    private String value;
}

