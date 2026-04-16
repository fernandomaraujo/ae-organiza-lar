package com.aelares.resource;

import com.aelares.dto.response.UserResponse;
import com.aelares.service.AuthService;
import com.aelares.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @Inject
    AuthService authService;

    /**
     * GET /api/v1/users/me - Retorna dados do usuário autenticado
     */
    @GET
    @Path("/me")
    public Response getCurrentUser(@HeaderParam("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .build();
            }

            String token = authHeader.substring(7);
            UUID userId = authService.extractUserIdFromToken(token);

            UserResponse user = userService.getUserById(userId);
            return Response.ok(user).build();
        } catch (Exception e) {
            return Response
                .status(Response.Status.UNAUTHORIZED)
                .build();
        }
    }

    /**
     * GET /api/v1/users/{id} - Busca usuário por ID
     */
    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") String id) {
        try {
            UUID userId = UUID.fromString(id);
            UserResponse user = userService.getUserById(userId);
            return Response.ok(user).build();
        } catch (IllegalArgumentException e) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new java.util.HashMap<String, String>() {{
                    put("message", "ID inválido");
                }})
                .build();
        } catch (com.aelares.exception.ResourceNotFoundException e) {
            return Response
                .status(Response.Status.NOT_FOUND)
                .entity(new java.util.HashMap<String, String>() {{
                    put("message", e.getMessage());
                }})
                .build();
        }
    }
}

