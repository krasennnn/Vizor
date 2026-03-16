package com.krasen.vizor.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CampaignExceptions {
    public static ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found with ID: " + id);
    }

    public static ResponseStatusException ownerMismatch(Long ownerId) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to campaigns owned by user: " + ownerId);
    }

    public static ResponseStatusException invalidDates() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date.");
    }

    public static ResponseStatusException duplicateName(String name) {
        return new ResponseStatusException(HttpStatus.CONFLICT, "A campaign with name \"" + name + "\" already exists.");
    }

    public static ResponseStatusException cannotDeleteActive() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Active campaigns cannot be deleted.");
    }

    public static ResponseStatusException alreadyStarted() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot change start date for a campaign that has already started.");
    }

    public static ResponseStatusException alreadyEnded() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "This campaign has already ended and cannot be modified.");
    }

    public static ResponseStatusException cannotDeleteWithContracts() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete a campaign that already has active contracts.");
    }

    public static ResponseStatusException nullInput() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign data is missing or malformed.");
    }

    public static ResponseStatusException ownerMissingCampaigns() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "You do not have any campaigns created.");
    }

    public static ResponseStatusException pastDate(){
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start or end dates can't be before the current date.");
    }
}
