package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sensor resource — /api/v1/sensors
 *
 * POST /sensors                      — register a sensor (validates roomId exists)
 * GET  /sensors                      — list all (optional ?type= filter)
 * GET  /sensors/{sensorId}           — get one sensor
 * PUT  /sensors/{sensorId}/status    — update sensor status
 *
 * Sub-resource locator (no HTTP method annotation):
 * /sensors/{sensorId}/readings       — delegates to SensorReadingResource
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @Context
    private UriInfo uriInfo;

    private final DataStore store = DataStore.get();

    // -----------------------------------------------------------------------
    // POST /sensors
    // Validates that the referenced roomId exists before persisting.
    // Throws LinkedResourceNotFoundException → 422 if room is missing.
    // -----------------------------------------------------------------------
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null) {
            return Response.status(400)
                    .entity(error("Bad Request", "Request body must not be empty."))
                    .build();
        }
        // Foreign-key validation: roomId must reference an existing room
        if (sensor.getRoomId() == null || !store.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(sensor.getRoomId());
        }
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            sensor.setId(UUID.randomUUID().toString());
        }
        if (store.sensors.containsKey(sensor.getId())) {
            return Response.status(409)
                    .entity(error("Conflict", "Sensor '" + sensor.getId() + "' already exists."))
                    .build();
        }
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        store.sensors.put(sensor.getId(), sensor);

        // Link sensor ID into the parent room
        store.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        // Initialise an empty thread-safe reading list for this sensor
        store.readings.put(sensor.getId(),
                Collections.synchronizedList(new ArrayList<>()));

        URI location = uriInfo.getAbsolutePathBuilder()
                               .path(sensor.getId())
                               .build();
        return Response.created(location).entity(sensor).build();
    }

    // -----------------------------------------------------------------------
    // GET /sensors?type=CO2
    // @QueryParam is used (not a path segment) because:
    //   - the filter is optional; path segments cannot be optional
    //   - multiple filters compose: ?type=CO2&status=ACTIVE
    //   - /sensors always identifies the collection; the query string narrows it
    // -----------------------------------------------------------------------
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(store.sensors.values());
        if (type != null && !type.isBlank()) {
            result = result.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return Response.ok(result).build();
    }

    // -----------------------------------------------------------------------
    // GET /sensors/{sensorId}
    // -----------------------------------------------------------------------
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(error("Not Found", "No sensor with id '" + sensorId + "'."))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // -----------------------------------------------------------------------
    // PUT /sensors/{sensorId}/status  — change ACTIVE | MAINTENANCE | OFFLINE
    // -----------------------------------------------------------------------
    @PUT
    @Path("/{sensorId}/status")
    public Response updateStatus(@PathParam("sensorId") String sensorId,
                                 Map<String, String> body) {
        Sensor sensor = store.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(error("Not Found", "No sensor with id '" + sensorId + "'."))
                    .build();
        }
        String newStatus = body == null ? null : body.get("status");
        if (newStatus == null ||
            (!newStatus.equals("ACTIVE") &&
             !newStatus.equals("MAINTENANCE") &&
             !newStatus.equals("OFFLINE"))) {
            return Response.status(400)
                    .entity(error("Bad Request", "Status must be ACTIVE, MAINTENANCE, or OFFLINE."))
                    .build();
        }
        sensor.setStatus(newStatus);
        return Response.ok(sensor).build();
    }

    // -----------------------------------------------------------------------
    // Sub-resource locator — NO @GET/@POST annotation.
    // JAX-RS calls this method to obtain the object that handles
    // requests under /sensors/{sensorId}/readings.
    // -----------------------------------------------------------------------
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(
            @PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    // -----------------------------------------------------------------------
    private Map<String, Object> error(String err, String msg) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error",   err);
        body.put("message", msg);
        return body;
    }
}
