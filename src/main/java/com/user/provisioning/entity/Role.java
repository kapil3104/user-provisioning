package com.user.provisioning.entity;

import com.user.provisioning.enums.ERole;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Role extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ERole name;
}