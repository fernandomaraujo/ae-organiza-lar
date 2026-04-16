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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Autenticação", description = "Login sem senha via código enviado ao e-mail")
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/request-code")
    @Operation(summary = "Solicitar código de acesso",
               description = "Envia um código OTP de 6 dígitos para o e-mail informado. Expira em 15 minutos.")
    @APIResponse(responseCode = "200", description = "Código enviado com sucesso")
    @APIResponse(responseCode = "429", description = "Muitas tentativas — aguarde alguns minutos")
    public Response requestCode(@Valid LoginRequest request,
                                @HeaderParam("X-Forwarded-For") String forwardedFor) {
        try {
            authService.requestCode(request.email, extractClientIp(forwardedFor));

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
    @Operation(summary = "Verificar código e obter token",
               description = "Valida o código OTP e retorna um JWT Bearer token. Cria o usuário automaticamente no primeiro acesso.")
    @APIResponse(responseCode = "200", description = "Login realizado — token retornado no corpo e no header Authorization")
    @APIResponse(responseCode = "401", description = "Código inválido, expirado ou já utilizado")
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
    @Operation(summary = "Alias para /request-code", description = "Mantido por compatibilidade.")
    public Response login(@Valid LoginRequest request,
                          @HeaderParam("X-Forwarded-For") String forwardedFor) {
        return requestCode(request, forwardedFor);
    }

    @POST
    @Path("/verify")
    @Operation(summary = "Alias para /verify-code", description = "Mantido por compatibilidade.")
    public Response verify(@Valid VerifyCodeRequest request) {
        return verifyCode(request);
    }

    /**
     * POST /api/v1/auth/validate - Valida se o token é válido
     */
    @POST
    @Path("/validate")
    @Operation(summary = "Validar token JWT", description = "Verifica se o Bearer token ainda é válido.")
    @APIResponse(responseCode = "200", description = "Token válido")
    @APIResponse(responseCode = "401", description = "Token inválido ou ausente")
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
    @Operation(summary = "Dados do usuário autenticado",
               description = "Retorna o perfil do usuário dono do token Bearer fornecido.")
    @APIResponse(responseCode = "200", description = "Perfil do usuário")
    @APIResponse(responseCode = "401", description = "Token ausente ou inválido")
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

    private String extractClientIp(String forwardedFor) {
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return null;
        }

        String[] parts = forwardedFor.split(",");
        return parts[0].trim();
    }
}

