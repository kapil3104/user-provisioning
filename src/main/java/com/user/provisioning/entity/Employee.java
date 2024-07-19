package com.user.provisioning.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "employees")
@Data
public class Employee {
    @Id
    private String id;
    private String name;
    private String email;
    private String role;
    private String location;
    private String department;
}

