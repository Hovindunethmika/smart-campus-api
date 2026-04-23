package com.smartcampus.exception;

/**
 * Thrown when POST /sensors is called with a roomId that does not exist.
 * Mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundMapper.
 *
 * 422 is used instead of 404 because the request URL (/sensors) is valid —
 * the problem is a broken reference inside the JSON payload body.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    private final String missingId;

    public LinkedResourceNotFoundException(String missingId) {
        super("Referenced roomId '" + missingId
              + "' does not exist. Create the room first.");
        this.missingId = missingId;
    }

    public String getMissingId() { return missingId; }
}
