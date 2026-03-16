package com.krasen.vizor.business.IRepo;

import com.krasen.vizor.business.domain.Campaign;

import java.util.List;
import java.util.Optional;

public interface ICampaignRepository {
    Campaign save(Campaign campaign);

    Optional<Campaign> findById(Long id);

    List<Campaign> findAll();

    List<Campaign> findByOwnerId(Long ownerId);

    void delete(Long id);
}
