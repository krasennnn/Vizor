package com.krasen.vizor.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ContractExceptions {
    // --- General ---
    public static ResponseStatusException nullInput() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Contract data is missing or malformed.");
    }

    public static ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Contract not found with ID: " + id);
    }

    public static ResponseStatusException missingRequiredFields(){
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Contract data is missing required fields.");
    }

    // --- Contract-specific validation ---
    public static ResponseStatusException duplicateContract(Long creatorId, Long campaignId) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "A contract already exists for creator " + creatorId +
                        " and campaign " + campaignId + ".");
    }

    public static ResponseStatusException campaignInactive(Long id) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Campaign with ID " + id + " is inactive or has already ended.");
    }

    public static ResponseStatusException campaignOwnershipConflict(Long id) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN,
                "You cannot create a contract for a campaign you do not own (ID: " + id + ").");
    }

    public static ResponseStatusException alreadySigned(Long id) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "This contract (ID: " + id + ") has already been signed and cannot be modified.");
    }

    public static ResponseStatusException cannotDeleteActive() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Active or in-progress contracts cannot be deleted.");
    }

    public static ResponseStatusException cannotDeleteSigned() {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "You cannot delete a contract that has already been signed by both parties.");
    }

    public static ResponseStatusException invalidDates() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Contract start or end date is invalid.");
    }

    public static ResponseStatusException alreadyApproved(Long id) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "Contract with ID " + id + " has already been approved.");
    }

    public static ResponseStatusException alreadyRejected(Long id) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "Contract with ID " + id + " has already been rejected.");
    }

    public static ResponseStatusException alreadyCompleted(Long id) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "Contract with ID " + id + " has already been completed.");
    }

    public static ResponseStatusException cannotApprove(Long id) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Contract with ID " + id + " cannot be approved (may already be approved, rejected, or completed).");
    }

    public static ResponseStatusException cannotComplete(Long id) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Contract with ID " + id + " cannot be completed (must be approved and started first).");
    }

    public static ResponseStatusException cannotApplyToOwnCampaign(Long campaignId) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "You cannot apply to your own campaign (ID: " + campaignId + ").");
    }

    public static ResponseStatusException creatorNotFound(Long creatorId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Creator not found with ID: " + creatorId);
    }

    public static ResponseStatusException accessDenied(Long contractId) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN,
                "You do not have permission to access contract with ID: " + contractId);
    }
}
