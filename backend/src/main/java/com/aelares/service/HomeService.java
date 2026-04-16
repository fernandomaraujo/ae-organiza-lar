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

    public HomeResponse getHomeById(UUID homeId, UUID userId) {
        return toResponse(findOwnedHome(homeId, userId));
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

    @Transactional
    public HomeResponse updateHome(UUID homeId, UUID userId, CreateHomeRequest request) {
        Home home = findOwnedHome(homeId, userId);

        home.name = request.name;
        home.description = request.description;
        home.updatedAt = LocalDateTime.now();
        home.persist();

        return toResponse(home);
    }

    @Transactional
    public void deleteHome(UUID homeId, UUID userId) {
        Home home = findOwnedHome(homeId, userId);
        home.delete();
    }

    public void validateHomeOwnership(UUID homeId, UUID userId) {
        Home home = Home.findByIdAndUserId(homeId, userId);
        if (home == null) {
            throw new ResourceNotFoundException("Residência não encontrada");
        }
    }

    private Home findOwnedHome(UUID homeId, UUID userId) {
        Home home = Home.findByIdAndUserId(homeId, userId);
        if (home == null) {
            throw new ResourceNotFoundException("Residência não encontrada");
        }
        return home;
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

