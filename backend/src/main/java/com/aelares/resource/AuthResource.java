package com.aelares.resource;

import com.aelares.dto.request.LoginRequest;
import com.aelares.dto.request.VerifyCodeRequest;
import com.aelares.dto.response.LoginResponse;
import com.aelares.dto.response.ErrorResponse;
import com.aelares.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    /**
     * POST /api/v1/auth/login - Envia código OTP por email
     */
    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        try {
            authService.sendOtpCode(request.email);

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

    /**
     * POST /api/v1/auth/verify - Verifica código OTP e retorna JWT token
     */
    @POST
    @Path("/verify")
    public Response verifyCode(@Valid VerifyCodeRequest request) {
        try {
            String token = authService.verifyOtpAndGetToken(request.email, request.code);

            return Response
                .ok()
                .header("Authorization", "Bearer " + token)
                .entity(new java.util.HashMap<String, String>() {{
                    put("token", token);
                    put("message", "Login realizado com sucesso");
                }})
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

    /**
     * POST /api/v1/auth/validate - Valida se o token é válido
     */
    @POST
    @Path("/validate")
    public Response validateToken(@HeaderParam("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Token não fornecido", 401))
                    .build();
            }

            String token = authHeader.substring(7);
            boolean isValid = authService.isTokenValid(token);

            if (isValid) {
                return Response.ok(new java.util.HashMap<String, Object>() {{
                    put("valid", true);
                    put("message", "Token válido");
                }}).build();
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
}

