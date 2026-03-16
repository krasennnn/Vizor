package com.krasen.vizor.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AuthExceptions {
    public static ResponseStatusException authenticationRequired() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    public static ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    }
}

