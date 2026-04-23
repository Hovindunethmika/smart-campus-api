package com.smartcampus.model;

/**
 * Represents a sensor deployed in a campus room.
 * Status values: "ACTIVE", "MAINTENANCE", "OFFLINE"
 */
public class Sensor {

    private String id;            // Unique identifier, e.g. "TEMP-001"
    private String type;          // Category: "Temperature", "CO2", "Occupancy", etc.
    private String status;        // ACTIVE | MAINTENANCE | OFFLINE
    private double currentValue;  // Most recent measurement recorded
    private String roomId;        // Foreign key linking to the parent Room

    public Sensor() {}

    public Sensor(String id, String type, String status,
                  double currentValue, String roomId) {
        this.id           = id;
        this.type         = type;
        this.status       = status;
        this.currentValue = currentValue;
        this.roomId       = roomId;
    }

    public String getId()                        { return id; }
    public void   setId(String id)               { this.id = id; }

    public String getType()                      { return type; }
    public void   setType(String type)           { this.type = type; }

    public String getStatus()                    { return status; }
    public void   setStatus(String status)       { this.status = status; }

    public double getCurrentValue()              { return currentValue; }
    public void   setCurrentValue(double v)      { this.currentValue = v; }

    public String getRoomId()                    { return roomId; }
    public void   setRoomId(String roomId)       { this.roomId = roomId; }
}
