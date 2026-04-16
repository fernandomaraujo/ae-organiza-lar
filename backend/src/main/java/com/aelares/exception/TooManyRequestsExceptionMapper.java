package com.aelares.exception;

import com.aelares.dto.response.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TooManyRequestsExceptionMapper implements ExceptionMapper<TooManyRequestsException> {

    @Override
    public Response toResponse(TooManyRequestsException exception) {
        ErrorResponse error = new ErrorResponse(
            exception.getMessage(),
            "Muitas tentativas",
            Response.Status.TOO_MANY_REQUESTS.getStatusCode(),
            System.currentTimeMillis()
        );

        return Response
            .status(Response.Status.TOO_MANY_REQUESTS)
            .entity(error)
            .build();
    }
}

