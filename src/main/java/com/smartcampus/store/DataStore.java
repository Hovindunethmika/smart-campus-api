package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store.
 *
 * Because JAX-RS creates a new resource instance per request, shared data
 * cannot live on resource classes. This singleton is the single source of
 * truth for the lifetime of the application.
 *
 * ConcurrentHashMap provides thread-safe reads and writes without explicit
 * synchronisation, preventing race conditions under concurrent requests.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    public static DataStore get() { return INSTANCE; }

    /** All rooms, keyed by room ID */
    public final ConcurrentHashMap<String, Room>   rooms   = new ConcurrentHashMap<>();

    /** All sensors, keyed by sensor ID */
    public final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();

    /** Historical readings per sensor, keyed by sensor ID */
    public final ConcurrentHashMap<String, List<SensorReading>> readings
            = new ConcurrentHashMap<>();

    /** Pre-loaded demo data so Postman tests work immediately on first run */
    private DataStore() {
        // Seed rooms
        Room r1 = new Room("LIB-301",  "Library Quiet Study",  40);
        Room r2 = new Room("LAB-101",  "Computer Science Lab",  30);
        Room r3 = new Room("HALL-A",   "Main Lecture Hall",    200);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        // Seed sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE",      22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE",     415.0, "LAB-101");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE",  0.0, "HALL-A");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        // Link sensor IDs into rooms
        r1.getSensorIds().add(s1.getId());
        r2.getSensorIds().add(s2.getId());
        r3.getSensorIds().add(s3.getId());

        // Initialise empty reading lists (thread-safe)
        readings.put(s1.getId(), Collections.synchronizedList(new ArrayList<>()));
        readings.put(s2.getId(), Collections.synchronizedList(new ArrayList<>()));
        readings.put(s3.getId(), Collections.synchronizedList(new ArrayList<>()));
    }
}
