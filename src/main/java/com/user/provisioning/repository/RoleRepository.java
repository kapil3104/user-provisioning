package com.user.provisioning.repository;

import java.util.Optional;

import com.user.provisioning.entity.ERole;
import com.user.provisioning.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(ERole name);
}
