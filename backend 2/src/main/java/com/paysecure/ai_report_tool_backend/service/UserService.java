package com.paysecure.ai_report_tool_backend.service;

import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );
    }
}