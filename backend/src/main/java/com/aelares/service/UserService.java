package com.aelares.service;

import com.aelares.entity.User;
import com.aelares.dto.response.UserResponse;
import com.aelares.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    /**
     * Busca usuário por ID
     */
    public UserResponse getUserById(UUID userId) {
        User user = User.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }
        return toResponse(user);
    }

    /**
     * Busca usuário por email
     */
    public UserResponse getUserByEmail(String email) {
        User user = User.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }
        return toResponse(user);
    }

    /**
     * Atualiza informações do usuário
     */
    @Transactional
    public UserResponse updateUser(UUID userId, String name, String email) {
        User user = User.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        if (name != null && !name.isBlank()) {
            user.name = name;
        }

        // TODO: Check if email already exists before updating
        if (email != null && !email.isBlank() && !email.equals(user.email)) {
            user.email = email;
        }

        user.persist();
        return toResponse(user);
    }

    /**
     * Converte entidade User para UserResponse
     */
    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.id,
            user.email,
            user.name,
            user.createdAt,
            user.lastLogin,
            null // preferences será processada em fase 2
        );
    }
}

