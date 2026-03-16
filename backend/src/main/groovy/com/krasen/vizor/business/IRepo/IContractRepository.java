package com.krasen.vizor.business.IRepo;

import com.krasen.vizor.business.domain.Contract;

import java.util.List;
import java.util.Optional;

public interface IContractRepository {
    Contract save(Contract contract);

    Optional<Contract> findById(Long id);

    List<Contract> findAll();

    List<Contract> findAllIncludingRejected();

    List<Contract> findByCreatorId(Long creatorId);

    List<Contract> findByCampaignId(Long campaignId);

    void delete(Long id);

    List<Contract> findActiveForCreator(Long creatorId);

    List<Contract> findActiveForCampaign(Long campaignId);

    boolean checkDuplicates(Long creatorId, Long campaignId);

    List<Contract> findByUserIdIncludingRejected(Long userId);
}
