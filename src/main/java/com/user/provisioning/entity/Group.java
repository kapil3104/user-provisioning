package com.user.provisioning.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "groups")
public class Group {
    @Id
    private String id;
    private String name;
    private String type;
    private String description;
}

