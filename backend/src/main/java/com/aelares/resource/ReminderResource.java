package com.aelares.resource;

import com.aelares.dto.response.ErrorResponse;
import com.aelares.entity.Home;
import com.aelares.entity.User;
import com.aelares.exception.ResourceNotFoundException;
import com.aelares.exception.UnauthorizedException;
import com.aelares.service.AuthService;
import com.aelares.service.ReminderService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/reminders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Lembretes", description = "Envio do planejamento semanal por e-mail")
public class ReminderResource {

    @Inject
    ReminderService reminderService;

    @Inject
    AuthService authService;

    /**
     * POST /api/v1/reminders/send-now
     * Dispara o e-mail de planejamento semanal imediatamente para o usuário autenticado.
     * Útil para testar / envio manual antes de qualquer configuração de agenda.
     */
    @POST
    @Path("/send-now")
    public Response sendNow(@HeaderParam("Authorization") String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            List<Home> homes = Home.findByUserId(user.id);

            if (homes.isEmpty()) {
                Map<String, String> body = new HashMap<>();
                body.put("message", "Nenhum lar cadastrado. Crie um lar primeiro para receber o planejamento.");
                return Response.ok(body).build();
            }

            reminderService.sendReminderToUser(user, homes);

            Map<String, String> body = new HashMap<>();
            body.put("message", "E-mail com planejamento semanal enviado para " + user.email);
            return Response.ok(body).build();

        } catch (UnauthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(e.getMessage(), Response.Status.UNAUTHORIZED.getStatusCode()))
                .build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage(), Response.Status.NOT_FOUND.getStatusCode()))
                .build();
        } catch (Exception e) {
            Log.errorf("Erro ao disparar lembrete manual: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Erro ao enviar e-mail. Tente novamente.", 500))
                .build();
        }
    }

    /**
     * GET /api/v1/reminders/info
     * Informa ao usuário autenticado qual o horário do próximo lembrete automático.
     */
    @GET
    @Path("/info")
    public Response info(@HeaderParam("Authorization") String authHeader) {
        try {
            getUserFromToken(authHeader); // apenas valida o token

            Map<String, Object> body = new HashMap<>();
            body.put("schedule", "Segunda-feira às 07:00 (horário do servidor)");
            body.put("description", "Um e-mail com o planejamento da semana é enviado automaticamente toda segunda-feira.");
            body.put("manualSendEndpoint", "POST /api/v1/reminders/send-now");
            return Response.ok(body).build();

        } catch (UnauthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(e.getMessage(), Response.Status.UNAUTHORIZED.getStatusCode()))
                .build();
        }
    }

    // --- helper ---

    private User getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token não fornecido");
        }
        return authService.getUserFromToken(authHeader.substring(7));
    }
}
