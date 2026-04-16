package com.aelares.exception;

import com.aelares.dto.response.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        ErrorResponse error = new ErrorResponse(
            exception.getMessage(),
            "Recurso não encontrado",
            Response.Status.NOT_FOUND.getStatusCode(),
            System.currentTimeMillis()
        );

        return Response
            .status(Response.Status.NOT_FOUND)
            .entity(error)
            .build();
    }
}

