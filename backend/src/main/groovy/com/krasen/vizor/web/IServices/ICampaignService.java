package com.krasen.vizor.web.IServices;

import com.krasen.vizor.business.domain.Campaign;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ICampaignService {
    @Transactional
    Campaign create(Campaign input, Long currentUserId);

    Campaign get(Long id);

    List<Campaign> getAll();

    List<Campaign> getByOwner(Long ownerId, Long currentUserId);

    List<Campaign> getByCreator(Long creatorId, Long currentUserId);

    @Transactional
    Campaign update(Campaign input, Long currentUserId);

    @Transactional
    void delete(Long id, Long currentUserId);
}
