package com.smartcampus.exception;

/**
 * Thrown when POST /sensors/{id}/readings is attempted on a sensor
 * whose status is "MAINTENANCE".
 * Mapped to HTTP 403 Forbidden by SensorUnavailableMapper.
 */
public class SensorUnavailableException extends RuntimeException {

    private final String sensorId;
    private final String currentStatus;

    public SensorUnavailableException(String sensorId, String currentStatus) {
        super("Sensor '" + sensorId + "' is in " + currentStatus
              + " mode and cannot accept new readings.");
        this.sensorId      = sensorId;
        this.currentStatus = currentStatus;
    }

    public String getSensorId()      { return sensorId; }
    public String getCurrentStatus() { return currentStatus; }
}
