package com.smartcampus.config;

import com.smartcampus.exception.GlobalExceptionMapper;
import com.smartcampus.exception.LinkedResourceNotFoundMapper;
import com.smartcampus.exception.RoomNotEmptyMapper;
import com.smartcampus.exception.SensorUnavailableMapper;
import com.smartcampus.filter.LoggingFilter;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import org.glassfish.jersey.jackson.JacksonFeature;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application configuration.
 *
 * @ApplicationPath declares the base URI segment for all REST resources.
 * Combined with the servlet mapping in web.xml (/api/v1/*), every request
 * to /api/v1/... is routed through Jersey into the registered classes below.
 *
 * Lifecycle note: by default JAX-RS creates a NEW resource instance per
 * request (request-scoped). Shared state is therefore stored in the
 * DataStore singleton (ConcurrentHashMap) — not in resource instance fields.
 */
@ApplicationPath("/")
public class AppConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // REST Resources
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);

        // Exception Mappers
        classes.add(RoomNotEmptyMapper.class);
        classes.add(LinkedResourceNotFoundMapper.class);
        classes.add(SensorUnavailableMapper.class);
        classes.add(GlobalExceptionMapper.class);

        // Request / Response Filters
        classes.add(LoggingFilter.class);

        return classes;
    }
}
