package com.krasen.vizor.business.services;

import com.krasen.vizor.business.IRepo.ICampaignRepository;
import com.krasen.vizor.business.IRepo.IContractRepository;
import com.krasen.vizor.business.IRepo.IUserRepository;
import com.krasen.vizor.business.exception.AuthExceptions;
import com.krasen.vizor.business.exception.CampaignExceptions;
import com.krasen.vizor.business.exception.ContractExceptions;
import com.krasen.vizor.business.domain.Campaign;
import com.krasen.vizor.business.domain.Contract;
import com.krasen.vizor.business.domain.User;
import com.krasen.vizor.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractService {
    private final IContractRepository repo;
    private final ICampaignRepository campaignRepo;
    private final IUserRepository userRepo;
    private final SecurityUtils securityUtils;

    @Transactional
    public Contract create(Contract input, Long currentUserId, boolean isOwner) {

        if (input == null || input.getCampaign() == null || input.getCampaign().getId() == null) {
            throw ContractExceptions.nullInput();
        }

        if (currentUserId == null) {
            throw AuthExceptions.authenticationRequired();
        }

        // Validate campaign and ensure it's loaded
        Campaign campaign = campaignRepo.findById(input.getCampaign().getId())
                .orElseThrow(() -> CampaignExceptions.notFound(input.getCampaign().getId()));
        
        // Set the full campaign object
        input.setCampaign(campaign);

        User creator;

        if (isOwner) {
            // owner sends invite → validate owner owns the campaign
            if (!campaign.getOwner().getId().equals(currentUserId)) {
                throw AuthExceptions.unauthorized();
            }

            // creatorId must be provided in request
            if (input.getCreator() == null || input.getCreator().getId() == null)
                throw ContractExceptions.missingRequiredFields();

            creator = userRepo.findById(input.getCreator().getId())
                    .orElseThrow(() -> ContractExceptions.creatorNotFound(input.getCreator().getId()));

            // Prevent owner from inviting themselves
            if (creator.getId().equals(currentUserId)) {
                throw ContractExceptions.cannotApplyToOwnCampaign(campaign.getId());
            }
        } else {
            // creator sends proposal → use logged-in user id
            creator = userRepo.findById(currentUserId)
                    .orElseThrow(() -> AuthExceptions.authenticationRequired());

            // Prevent creator from applying to their own campaign
            if (campaign.getOwner().getId().equals(creator.getId())) {
                throw ContractExceptions.cannotApplyToOwnCampaign(campaign.getId());
            }
        }

        // Set creator on contract
        input.setCreator(creator);

        // check duplicates with the effective creator id
        if (repo.checkDuplicates(creator.getId(), campaign.getId()))
            throw ContractExceptions.duplicateContract(creator.getId(), campaign.getId());

        // Default state for new proposals/invites
        input.setApprovedByOwner(false);
        input.setStartAt(null);
        input.setDeadlineAt(null);
        input.setCompletedAt(null);
        input.setDeletedAt(null);

        return repo.save(input);
    }

    public Contract get(Long id) {
        if (id == null) {
            throw ContractExceptions.nullInput();
        }
        return repo.findById(id)
                .orElseThrow(() -> ContractExceptions.notFound(id));
    }

    public Contract getByIdWithAuthorization(Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw AuthExceptions.authenticationRequired();
        }
        
        Contract contract = get(id);
        
        // Verify user is authorized to view this contract (either creator or campaign owner)
        if (!contract.getCreator().getId().equals(currentUserId) && 
            !contract.getCampaign().getOwner().getId().equals(currentUserId)) {
            throw ContractExceptions.accessDenied(id);
        }
        
        return contract;
    }

    public List<Contract> getAllForCurrentUser() {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw AuthExceptions.authenticationRequired();
        }
        return getByUserIdIncludingRejected(currentUserId);
    }

    public List<Contract> getAll() {
        return repo.findAll();
    }

    public List<Contract> getAllIncludingRejected() {
        return repo.findAllIncludingRejected();
    }

    public List<Contract> getByUserIdIncludingRejected(Long userId) {
        if (userId == null) {
            throw AuthExceptions.authenticationRequired();
        }
        return repo.findByUserIdIncludingRejected(userId);
    }

    public List<Contract> getByCreator(Long creatorId) {
        if (creatorId == null) {
            throw ContractExceptions.nullInput();
        }
        return repo.findByCreatorId(creatorId);
    }

    public List<Contract> getByCampaign(Long campaignId) {
        if (campaignId == null) {
            throw ContractExceptions.nullInput();
        }
        // Validate campaign exists
        campaignRepo.findById(campaignId)
                .orElseThrow(() -> CampaignExceptions.notFound(campaignId));

        return repo.findByCampaignId(campaignId);
    }

    @Transactional
    public Contract update(Contract input) {
        if (input == null || input.getId() == null) {
            throw ContractExceptions.nullInput();
        }

        Contract existing = get(input.getId()); // throws if not found

        // Only allow updates to non-approved contracts (proposals/invites that haven't been accepted)
        if (existing.isApprovedByOwner()) {
            throw ContractExceptions.alreadySigned(existing.getId());
        }

        // Apply partial updates (only update provided fields)
        if (input.getRetainerCents() != null) {
            existing.setRetainerCents(input.getRetainerCents());
        }
        if (input.getExpectedPosts() > 0) {
            existing.setExpectedPosts(input.getExpectedPosts());
        }
        // Note: deadlineAt, startAt, approvedByOwner, completedAt are not updatable via ContractUpdateRequest
        // These use separate lifecycle methods: approve(), reject(), complete()

        return repo.save(existing);
    }

    @Transactional
    public Contract approve(Long id) {
        if (id == null) {
            throw ContractExceptions.nullInput();
        }

        Contract existing = get(id); // throws if not found

        // Validate contract can be approved
        if (existing.isApprovedByOwner()) {
            throw ContractExceptions.alreadyApproved(id);
        }

        if (existing.getCompletedAt() != null) {
            throw ContractExceptions.cannotApprove(id);
        }

        // Approve contract and set start time
        OffsetDateTime now = OffsetDateTime.now();
        existing.setApprovedByOwner(true);
        existing.setStartAt(now);
        
        // Set deadline based on expected posts
        if (existing.getExpectedPosts() > 0) {
            existing.setDeadlineAt(now.plusDays(existing.getExpectedPosts()));
        }

        return repo.save(existing);
    }

    @Transactional
    public Contract reject(Long id) {
        if (id == null) {
            throw ContractExceptions.nullInput();
        }

        Contract existing = get(id); // throws if not found

        // Validate contract can be rejected
        if (existing.isApprovedByOwner()) {
            throw ContractExceptions.alreadyApproved(id);
        }

        if (existing.getCompletedAt() != null) {
            throw ContractExceptions.cannotApprove(id);
        }

        // Reject = soft delete
        existing.setDeletedAt(OffsetDateTime.now());
        return repo.save(existing);
    }

    @Transactional
    public Contract complete(Long id) {
        if (id == null) {
            throw ContractExceptions.nullInput();
        }

        Contract existing = get(id);

        // Validate contract can be completed
        if (!existing.isApprovedByOwner() || existing.getStartAt() == null) {
            throw ContractExceptions.cannotComplete(id);
        }

        if (existing.getCompletedAt() != null) {
            throw ContractExceptions.alreadyCompleted(id);
        }

        // Mark as completed
        existing.setCompletedAt(OffsetDateTime.now());
        return repo.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw ContractExceptions.nullInput();
        }

        Contract existing = get(id); // throws if not found

        // Check if contract is active (started but not completed)
        if (existing.getStartAt() != null && existing.getCompletedAt() == null) {
            throw ContractExceptions.cannotDeleteActive();
        }

        // Check if contract is signed (approved by owner)
        if (existing.isApprovedByOwner()) {
            throw ContractExceptions.cannotDeleteSigned();
        }

        repo.delete(existing.getId());
    }
}
