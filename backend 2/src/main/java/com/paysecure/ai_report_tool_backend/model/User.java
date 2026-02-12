package com.paysecure.ai_report_tool_backend.model;

import com.paysecure.ai_report_tool_backend.model.enums.Plan;
import com.paysecure.ai_report_tool_backend.model.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password; // Nullable for OAuth users

    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    private int credits = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Plan plan = Plan.Free;

    // OAuth fields
    private String googleId;
    private String profileImageUrl;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}