package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps SensorUnavailableException → HTTP 403 Forbidden
 */
@Provider
public class SensorUnavailableMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",        403);
        body.put("error",         "Forbidden");
        body.put("message",       ex.getMessage());
        body.put("sensorId",      ex.getSensorId());
        body.put("currentStatus", ex.getCurrentStatus());
        body.put("hint",          "Set status to ACTIVE via PUT /api/v1/sensors/"
                                  + ex.getSensorId() + "/status");
        return Response.status(403)
                       .entity(body)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
