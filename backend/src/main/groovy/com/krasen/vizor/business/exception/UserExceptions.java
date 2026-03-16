package com.krasen.vizor.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserExceptions {
    // --- General ---
    public static ResponseStatusException nullInput() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "User data is missing or malformed.");
    }

    public static ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "User not found with ID: " + id);
    }

    public static ResponseStatusException notFound(String identifier) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "User not found with email or username: " + identifier);
    }

    // --- Registration/Validation ---
    public static ResponseStatusException emailAlreadyExists(String email) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "A user with email \"" + email + "\" already exists.");
    }

    public static ResponseStatusException usernameAlreadyExists(String username) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "A user with username \"" + username + "\" already exists.");
    }

    public static ResponseStatusException invalidRole() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "User must have at least one role (creator or owner).");
    }

    public static ResponseStatusException invalidPassword() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Password does not meet requirements.");
    }

    public static ResponseStatusException invalidEmail() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Email format is invalid.");
    }

    public static ResponseStatusException invalidUsername() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Username must be between 3 and 50 characters.");
    }

    // --- Authentication ---
    public static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Invalid email/username or password.");
    }

    public static ResponseStatusException accountDisabled() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN,
                "This account has been disabled.");
    }

    public static ResponseStatusException accountDeleted() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN,
                "This account has been deleted.");
    }

    // --- Update/Delete ---
    public static ResponseStatusException cannotDeleteSelf() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "You cannot delete your own account.");
    }

    public static ResponseStatusException cannotUpdateDeleted() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Cannot update a deleted user account.");
    }
}

