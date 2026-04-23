package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.*;

/**
 * Sub-resource for sensor readings — /api/v1/sensors/{sensorId}/readings
 *
 * Instantiated by the sub-resource locator in SensorResource.
 * Receives sensorId at construction rather than via repeated @PathParam,
 * keeping responsibility focused on reading management only.
 *
 * GET  /  — fetch full reading history for this sensor
 * POST /  — append a new reading
 *           Side effect: updates parent Sensor's currentValue
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String    sensorId;
    private final DataStore store = DataStore.get();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // -----------------------------------------------------------------------
    // GET /sensors/{sensorId}/readings
    // -----------------------------------------------------------------------
    @GET
    public Response getReadings() {
        Sensor sensor = store.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(error("Not Found", "No sensor with id '" + sensorId + "'."))
                    .build();
        }
        List<SensorReading> history =
                store.readings.getOrDefault(sensorId, Collections.emptyList());
        return Response.ok(new ArrayList<>(history)).build();
    }

    // -----------------------------------------------------------------------
    // POST /sensors/{sensorId}/readings
    //
    // Business rule: MAINTENANCE sensors cannot accept readings → 403 Forbidden.
    //
    // Side effect: updates the parent Sensor's currentValue so that
    // GET /sensors/{sensorId} always reflects the latest measurement.
    // -----------------------------------------------------------------------
    @POST
    public Response addReading(SensorReading reading,
                               @Context UriInfo uriInfo) {
        Sensor sensor = store.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(error("Not Found", "No sensor with id '" + sensorId + "'."))
                    .build();
        }
        // 403 if sensor is under maintenance
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }
        if (reading == null) {
            return Response.status(400)
                    .entity(error("Bad Request", "Reading body must not be empty."))
                    .build();
        }

        // Server-side fields
        reading.setId(UUID.randomUUID().toString());
        reading.setTimestamp(System.currentTimeMillis());

        // Persist
        store.readings
             .computeIfAbsent(sensorId,
                     k -> Collections.synchronizedList(new ArrayList<>()))
             .add(reading);

        // --- SIDE EFFECT ---
        // Keep the parent sensor's currentValue in sync with the latest reading
        sensor.setCurrentValue(reading.getValue());

        URI location = uriInfo.getAbsolutePathBuilder()
                               .path(reading.getId())
                               .build();
        return Response.created(location).entity(reading).build();
    }

    // -----------------------------------------------------------------------
    private Map<String, Object> error(String err, String msg) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error",   err);
        body.put("message", msg);
        return body;
    }
}
