package com.krasen.vizor.business.services;

import com.krasen.vizor.business.IRepo.ICampaignRepository;
import com.krasen.vizor.business.IRepo.IContractRepository;
import com.krasen.vizor.business.IRepo.IUserRepository;
import com.krasen.vizor.business.domain.Contract;
import com.krasen.vizor.business.exception.AuthExceptions;
import com.krasen.vizor.business.exception.CampaignExceptions;
import com.krasen.vizor.business.domain.Campaign;
import com.krasen.vizor.business.domain.User;
import com.krasen.vizor.web.IServices.ICampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CampaignService implements ICampaignService {
    private final ICampaignRepository repo;
    private final IContractRepository contractRepo;
    private final IUserRepository userRepo;

    @Transactional
    @Override
    public Campaign create(Campaign input, Long currentUserId) {
        if (input == null) throw CampaignExceptions.nullInput();
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();
        
        // Load owner user
        User owner = userRepo.findById(currentUserId)
                .orElseThrow(() -> AuthExceptions.authenticationRequired());
        
        if (input.getOwner() == null || !input.getOwner().getId().equals(currentUserId)) {
            input.setOwner(owner);
        }
        
        if (!input.getOwner().getId().equals(currentUserId)) throw AuthExceptions.unauthorized();

        if (input.getStartAt() != null && input.getEndAt() != null &&
                input.getEndAt().toLocalDate().isBefore(input.getStartAt().toLocalDate())) {
            throw CampaignExceptions.invalidDates(); // check if start date is before end date
        }

        if (input.getStartAt() != null &&
                input.getStartAt().toLocalDate().isBefore(LocalDate.now(ZoneOffset.UTC))) {
            throw CampaignExceptions.pastDate(); // check if start date is in the past
        }

        if (input.getEndAt() != null &&
                input.getEndAt().toLocalDate().isBefore(LocalDate.now(ZoneOffset.UTC))) {
            throw CampaignExceptions.pastDate(); // check if end date is in the past
        }

        // Check if campaign name already exists (excluding deleted campaigns)
        boolean nameExists = repo.findAll().stream()
                .anyMatch(c -> Objects.equals(c.getName(), input.getName()));
        if (nameExists) {
            throw CampaignExceptions.duplicateName(input.getName());
        }

        input.setId(null);
        input.setCreatedAt(null);
        return repo.save(input);
    }

    @Override
    public Campaign get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> CampaignExceptions.notFound(id));
    }

    @Override
    public List<Campaign> getAll() {
        return repo.findAll();
    }

    @Override
    public List<Campaign> getByOwner(Long ownerId, Long currentUserId) {
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();
        if (!ownerId.equals(currentUserId)) throw AuthExceptions.unauthorized();

        List<Campaign> campaigns = repo.findByOwnerId(ownerId);

        if (campaigns.stream().count() < 1){
            throw CampaignExceptions.ownerMissingCampaigns();
        }

        return campaigns;
    }

    @Override
    public List<Campaign> getByCreator(Long creatorId, Long currentUserId) {
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();
        if (!creatorId.equals(currentUserId)) throw AuthExceptions.unauthorized();

        // Get all accepted contracts for this creator (including completed ones)
        // This includes all contracts that are approved, regardless of completion status
        List<Contract> contracts = contractRepo.findByCreatorId(creatorId)
                .stream()
                .filter(c -> c.isApprovedByOwner() && c.getDeletedAt() == null)
                .toList();
        
        // Extract unique campaigns from contracts
        return contracts.stream()
                .map(Contract::getCampaign)
                .distinct()
                .toList();
    }

    @Transactional
    @Override
    public Campaign update(Campaign input, Long currentUserId) {
        if (input == null) throw CampaignExceptions.nullInput();
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();

        Campaign existing = get(input.getId());
        
        // Verify ownership
        if (!existing.getOwner().getId().equals(currentUserId)) {
            throw AuthExceptions.unauthorized();
        }

        if (input.getEndAt() != null && input.getStartAt() != null &&
                input.getEndAt().toLocalDate().isBefore(input.getStartAt().toLocalDate())) {
            throw CampaignExceptions.invalidDates();
        }

        if (input.getStartAt() != null &&
                input.getStartAt().toLocalDate().isBefore(LocalDate.now())) {
            throw CampaignExceptions.alreadyStarted();
        }

        if (input.getEndAt() != null &&
                input.getEndAt().toLocalDate().isBefore(LocalDate.now())) {
            throw CampaignExceptions.alreadyEnded();
        }

        // Check if campaign name already exists (excluding current campaign and deleted campaigns)
        if (input.getName() != null) {
            boolean nameExists = repo.findAll().stream()
                    .anyMatch(c -> {
                        // Skip if name doesn't match
                        if (!Objects.equals(c.getName(), input.getName())) {
                            return false;
                        }
                        // If campaign has null ID, it's a different campaign (input has ID), so it's a duplicate
                        if (c.getId() == null) {
                            return true;
                        }
                        // If campaign has ID, check it's different from the one being updated
                        return !c.getId().equals(input.getId());
                    });
            if (nameExists) {
                throw CampaignExceptions.duplicateName(input.getName());
            }
        }

        if (input.getName() != null) existing.setName(input.getName());
        if (input.getEndAt() != null) existing.setEndAt(input.getEndAt());
        if (input.getStartAt() != null) existing.setStartAt(input.getStartAt());

        return repo.save(existing);
    }

    @Transactional
    @Override
    public void delete(Long id, Long currentUserId) {
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();
        
        Campaign existing = get(id);
        
        // Verify ownership
        if (!existing.getOwner().getId().equals(currentUserId)) {
            throw AuthExceptions.unauthorized();
        }

        OffsetDateTime now = OffsetDateTime.now();

        if (existing.getStartAt() != null && existing.getEndAt() != null &&
                (now.isAfter(existing.getStartAt()) && now.isBefore(existing.getEndAt()))) {
            throw CampaignExceptions.cannotDeleteActive();
        }

         if (contractRepo.findActiveForCampaign(id).stream().count() > 0) {
             throw CampaignExceptions.cannotDeleteWithContracts();
         }

        repo.delete(existing.getId());
    }
}
