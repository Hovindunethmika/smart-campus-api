package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "safety net" — catches any Throwable not handled by a more
 * specific mapper and returns a clean HTTP 500 with no stack trace.
 *
 * Security rationale for hiding stack traces:
 *   1. Internal file paths reveal server directory structure.
 *   2. Library names/versions enable targeted CVE searches.
 *   3. Class and method names expose business logic to attackers.
 *   4. Error conditions hint at exploitable input patterns.
 *
 * The full trace is logged server-side only for diagnostic purposes.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG =
            Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable throwable) {
        // Full trace logged server-side — never sent to the client
        LOG.log(Level.SEVERE, "Unhandled exception: " + throwable.getMessage(),
                throwable);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",  500);
        body.put("error",   "Internal Server Error");
        body.put("message", "An unexpected error occurred. "
                            + "Please contact the API administrator.");

        return Response.status(500)
                       .entity(body)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
