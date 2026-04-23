package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.*;

/**
 * Room management resource — /api/v1/rooms
 *
 * GET    /rooms            — list all rooms
 * POST   /rooms            — create a room  (201 + Location header)
 * GET    /rooms/{roomId}   — get one room
 * DELETE /rooms/{roomId}   — delete a room  (blocked with 409 if sensors present)
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @Context
    private UriInfo uriInfo;

    private final DataStore store = DataStore.get();

    // -----------------------------------------------------------------------
    // GET /rooms
    // -----------------------------------------------------------------------
    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(store.rooms.values())).build();
    }

    // -----------------------------------------------------------------------
    // POST /rooms  — returns 201 Created with a Location header
    // -----------------------------------------------------------------------
    @POST
    public Response createRoom(Room room) {
        if (room == null) {
            return Response.status(500)
                    .entity(error("Bad Request", "Request body must not be empty."))
                    .build();
        }
        if (room.getId() == null || room.getId().isBlank()) {
            room.setId(UUID.randomUUID().toString());
        }
        if (store.rooms.containsKey(room.getId())) {
            return Response.status(409)
                    .entity(error("Conflict", "A room with id '" + room.getId() + "' already exists."))
                    .build();
        }
        store.rooms.put(room.getId(), room);

        URI location = uriInfo.getAbsolutePathBuilder()
                               .path(room.getId())
                               .build();
        return Response.created(location).entity(room).build();
    }

    // -----------------------------------------------------------------------
    // GET /rooms/{roomId}
    // -----------------------------------------------------------------------
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.rooms.get(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity(error("Not Found", "No room with id '" + roomId + "'."))
                    .build();
        }
        return Response.ok(room).build();
    }

    // -----------------------------------------------------------------------
    // DELETE /rooms/{roomId}
    // Business rule: cannot delete a room that still has sensors assigned.
    // Throws RoomNotEmptyException → mapped to 409 Conflict.
    //
    // Idempotency: first call → 204, subsequent calls → 404.
    // Server state (room absent) is the same after both calls, so the
    // operation satisfies HTTP idempotency semantics.
    // -----------------------------------------------------------------------
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.rooms.get(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity(error("Not Found", "No room with id '" + roomId + "'."))
                    .build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        }
        store.rooms.remove(roomId);
        return Response.noContent().build(); // 204
    }

    // -----------------------------------------------------------------------
    private Map<String, Object> error(String err, String msg) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error",   err);
        body.put("message", msg);
        return body;
    }
}
