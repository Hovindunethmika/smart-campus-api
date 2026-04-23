package com.smartcampus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical room on campus.
 */
public class Room {

    private String id;           // Unique identifier, e.g. "LIB-301"
    private String name;         // Human-readable name, e.g. "Library Quiet Study"
    private int capacity;        // Maximum occupancy for safety regulations
    private List<String> sensorIds = new ArrayList<>(); // IDs of sensors in this room

    public Room() {}

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public String getId()                       { return id; }
    public void   setId(String id)              { this.id = id; }

    public String getName()                     { return name; }
    public void   setName(String name)          { this.name = name; }

    public int    getCapacity()                 { return capacity; }
    public void   setCapacity(int capacity)     { this.capacity = capacity; }

    public List<String> getSensorIds()          { return sensorIds; }
    public void setSensorIds(List<String> ids)  { this.sensorIds = ids; }
}
