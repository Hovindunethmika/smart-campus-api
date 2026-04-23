package com.smartcampus.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * Logging filter — logs every incoming request and outgoing response.
 *
 * Implementing both ContainerRequestFilter and ContainerResponseFilter
 * in a single class is the idiomatic JAX-RS approach for request/response
 * observability without duplicating Logger calls across every resource method.
 *
 * Benefits over manual logging per method:
 *   - DRY: one class covers the entire API automatically.
 *   - Consistent: new endpoints are logged with zero extra effort.
 *   - Separation of concerns: resources stay focused on business logic.
 *   - Also fires on error paths handled by exception mappers.
 */
@Provider
public class LoggingFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG =
            Logger.getLogger(LoggingFilter.class.getName());

    /** Fires before the request reaches the resource method. */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        LOG.info(String.format("[REQUEST]  %s  %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()));
    }

    /** Fires after the resource method (or exception mapper) returns. */
    @Override
    public void filter(ContainerRequestContext  requestContext,
                       ContainerResponseContext responseContext) {
        LOG.info(String.format("[RESPONSE] %d  %s",
                responseContext.getStatus(),
                requestContext.getUriInfo().getRequestUri()));
    }
}
