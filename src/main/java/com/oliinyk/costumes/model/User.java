package com.oliinyk.costumes.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private UUID id;
    private String email;
    private String passwordHash;
    private String role;
    private String verificationCode;
    private boolean isVerified;
    private boolean isBlocked;
    private LocalDateTime createdAt;
}
