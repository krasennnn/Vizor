package com.krasen.vizor.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class VideoExceptions {
    public static ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found with ID: " + id);
    }

    public static ResponseStatusException nullInput() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Video data is missing or malformed.");
    }

    public static ResponseStatusException invalidPlatformVideoId() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Platform video ID cannot be empty.");
    }
}

