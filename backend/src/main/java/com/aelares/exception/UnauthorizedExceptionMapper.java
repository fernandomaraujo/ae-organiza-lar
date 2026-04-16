package com.aelares.exception;

import com.aelares.dto.response.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {

    @Override
    public Response toResponse(UnauthorizedException exception) {
        ErrorResponse error = new ErrorResponse(
            exception.getMessage(),
            "Não autorizado",
            Response.Status.UNAUTHORIZED.getStatusCode(),
            System.currentTimeMillis()
        );

        return Response
            .status(Response.Status.UNAUTHORIZED)
            .entity(error)
            .build();
    }
}

