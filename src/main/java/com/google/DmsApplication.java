package com.google;

import com.google.entity.User;
import com.google.entity.Workspace;
import com.google.enums.JoinMode;
import com.google.repository.UserRepository;
import com.google.repository.WorkspaceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@SpringBootApplication
public class DmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DmsApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, WorkspaceRepository workspaceRepository) {
        return args -> {
            System.out.println("--- Bootstrapping Database Tables ---");
            
            // 1. Create a mock owner first (Workspaces require an owner)
            User owner = new User();
            owner.setEmail("admin-" + UUID.randomUUID() + "@docsy.local"); // Randomize to prevent unique constraint errors on restarts
            owner.setFullName("System Admin");
            owner.setPasswordHash("dummy_hash");
            userRepository.save(owner);

            System.out.println("--- User Created! ID: " + owner.getId() + " ---");

            // 2. Create the Workspace linked to the owner
            Workspace workspace = new Workspace();
            workspace.setName("Docsy Initial Workspace");
            workspace.setOwner(owner);
            workspace.setJoinCode("DOCSY-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
            workspace.setJoinMode(JoinMode.INVITE_ONLY);
            workspaceRepository.save(workspace);

            System.out.println("--- Workspace Created! ID: " + workspace.getId() + " ---");
            System.out.println("--- All Tables Generated Successfully ---");
        };
    }
}