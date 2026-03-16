package com.krasen.vizor.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AccountExceptions {
    public static ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found with ID: " + id);
    }

    public static ResponseStatusException creatorMismatch(Long creatorId) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to accounts owned by user: " + creatorId);
    }

    public static ResponseStatusException nullInput() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account data is missing or malformed.");
    }

    public static ResponseStatusException creatorMissingAccounts() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "You do not have any accounts created.");
    }

    public static ResponseStatusException invalidPlatformUsername() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Platform username cannot be empty.");
    }

    public static ResponseStatusException invalidPlatformUserId() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Platform user ID cannot be empty.");
    }
}

