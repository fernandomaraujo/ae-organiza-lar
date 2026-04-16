package com.aelares.service;

import com.aelares.entity.Home;
import com.aelares.dto.request.CreateHomeRequest;
import com.aelares.dto.response.HomeResponse;
import com.aelares.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class HomeService {

    /**
     * Cria uma nova residência
     */
    @Transactional
    public HomeResponse createHome(UUID userId, CreateHomeRequest request) {
        Home home = new Home();
        home.id = UUID.randomUUID();
        home.userId = userId;
        home.name = request.name;
        home.description = request.description;
        home.createdAt = LocalDateTime.now();
        home.updatedAt = LocalDateTime.now();

        home.persist();
        return toResponse(home);
    }

    /**
     * Busca residência por ID
     */
    public HomeResponse getHomeById(UUID homeId) {
        Home home = Home.findById(homeId);
        if (home == null) {
            throw new ResourceNotFoundException("Residência não encontrada");
        }
        return toResponse(home);
    }

    /**
     * Lista todas as residências do usuário
     */
    public List<HomeResponse> getHomesByUserId(UUID userId) {
        List<Home> homes = Home.findByUserId(userId);
        return homes.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Atualiza uma residência
     */
    @Transactional
    public HomeResponse updateHome(UUID homeId, CreateHomeRequest request) {
        Home home = Home.findById(homeId);
        if (home == null) {
            throw new ResourceNotFoundException("Residência não encontrada");
        }

        home.name = request.name;
        home.description = request.description;
        home.updatedAt = LocalDateTime.now();
        home.persist();

        return toResponse(home);
    }

    /**
     * Deleta uma residência
     */
    @Transactional
    public void deleteHome(UUID homeId) {
        Home home = Home.findById(homeId);
        if (home == null) {
            throw new ResourceNotFoundException("Residência não encontrada");
        }
        home.delete();
    }

    /**
     * Verifica se a residência pertence ao usuário
     */
    public void validateHomeOwnership(UUID homeId, UUID userId) {
        Home home = Home.findById(homeId);
        if (home == null) {
            throw new ResourceNotFoundException("Residência não encontrada");
        }
        if (!home.userId.equals(userId)) {
            throw new ResourceNotFoundException("Acesso negado");
        }
    }

    /**
     * Converte entidade Home para HomeResponse
     */
    private HomeResponse toResponse(Home home) {
        return new HomeResponse(
            home.id,
            home.userId,
            home.name,
            home.description,
            home.createdAt,
            home.updatedAt
        );
    }
}

