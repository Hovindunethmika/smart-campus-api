package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps LinkedResourceNotFoundException → HTTP 422 Unprocessable Entity
 */
@Provider
public class LinkedResourceNotFoundMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",    422);
        body.put("error",     "Unprocessable Entity");
        body.put("message",   ex.getMessage());
        body.put("missingId", ex.getMissingId());
        body.put("hint",      "POST to /api/v1/rooms first to create the room.");
        return Response.status(422)
                       .entity(body)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
