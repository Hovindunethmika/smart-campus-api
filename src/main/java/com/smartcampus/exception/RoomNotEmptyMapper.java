package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps RoomNotEmptyException → HTTP 409 Conflict
 */
@Provider
public class RoomNotEmptyMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",  409);
        body.put("error",   "Conflict");
        body.put("message", ex.getMessage());
        body.put("roomId",  ex.getRoomId());
        body.put("hint",    "Remove or reassign the " + ex.getSensorCount()
                            + " sensor(s) before deleting this room.");
        return Response.status(409)
                       .entity(body)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
