package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery endpoint — GET /api/v1
 *
 * Returns API metadata and hypermedia links to primary resource collections.
 * This embodies the HATEOAS principle: clients can discover the full API
 * starting from this single entry point rather than relying on static docs.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name",        "Smart Campus Sensor & Room Management API");
        response.put("version",     "1.0.0");
        response.put("status",      "running");
        response.put("contact",     "admin@smartcampus.ac.uk");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");

        // Hypermedia links — HATEOAS
        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms",   "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("resources", links);

        return Response.ok(response).build();
    }
}
