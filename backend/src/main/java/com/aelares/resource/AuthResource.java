package com.aelares.resource;

import com.aelares.dto.request.LoginRequest;
import com.aelares.dto.request.VerifyCodeRequest;
import com.aelares.dto.response.ErrorResponse;
import com.aelares.dto.response.LoginResponse;
import com.aelares.dto.response.UserResponse;
import com.aelares.entity.User;
import com.aelares.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/request-code")
    public Response requestCode(@Valid LoginRequest request,
                                @HeaderParam("X-Forwarded-For") String forwardedFor) {
        try {
            authService.requestCode(request.email, forwardedFor);

            return Response
                .ok(new LoginResponse(
                    "Código de acesso enviado para seu email",
                    request.email
                ))
                .build();
        } catch (Exception e) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(
                    "Erro ao enviar código: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
                ))
                .build();
        }
    }

    @POST
    @Path("/verify-code")
    public Response verifyCode(@Valid VerifyCodeRequest request) {
        try {
            AuthService.AuthResult authResult = authService.verifyCode(request.email, request.code);
            String token = authResult.token();
            User user = authResult.user();

            Map<String, Object> body = new HashMap<>();
            body.put("token", token);
            body.put("tokenType", "Bearer");
            body.put("message", "Login realizado com sucesso");
            body.put("user", new UserResponse(user.id, user.email, user.name, user.createdAt, user.lastLogin, null));

            return Response
                .ok()
                .header("Authorization", "Bearer " + token)
                .entity(body)
                .build();
        } catch (com.aelares.exception.UnauthorizedException e) {
            return Response
                .status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(
                    e.getMessage(),
                    Response.Status.UNAUTHORIZED.getStatusCode()
                ))
                .build();
        } catch (Exception e) {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(
                    "Erro ao verificar código",
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
                ))
                .build();
        }
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request,
                          @HeaderParam("X-Forwarded-For") String forwardedFor) {
        return requestCode(request, forwardedFor);
    }

    @POST
    @Path("/verify")
    public Response verify(@Valid VerifyCodeRequest request) {
        return verifyCode(request);
    }

    /**
     * POST /api/v1/auth/validate - Valida se o token é válido
     */
    @POST
    @Path("/validate")
    public Response validateToken(@HeaderParam("Authorization") String authHeader) {
        try {
            String token = extractBearerToken(authHeader);
            if (token == null) {
                return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Token não fornecido", 401))
                    .build();
            }

            boolean isValid = authService.isTokenValid(token);

            if (isValid) {
                Map<String, Object> body = new HashMap<>();
                body.put("valid", true);
                body.put("message", "Token válido");
                return Response.ok(body).build();
            } else {
                return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Token inválido", 401))
                    .build();
            }
        } catch (Exception e) {
            return Response
                .status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(e.getMessage(), 401))
                .build();
        }
    }

    @GET
    @Path("/me")
    public Response me(@HeaderParam("Authorization") String authHeader) {
        try {
            String token = extractBearerToken(authHeader);
            if (token == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Token não fornecido", 401))
                    .build();
            }

            User user = authService.getUserFromToken(token);
            UserResponse response = new UserResponse(user.id, user.email, user.name, user.createdAt, user.lastLogin, null);
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse("Token inválido", 401))
                .build();
        }
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}

