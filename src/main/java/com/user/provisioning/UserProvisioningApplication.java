package com.user.provisioning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class UserProvisioningApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserProvisioningApplication.class, args);
	}
}
