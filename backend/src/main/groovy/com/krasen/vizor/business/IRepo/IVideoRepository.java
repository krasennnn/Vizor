package com.krasen.vizor.business.IRepo;

import com.krasen.vizor.business.domain.Video;

import java.util.List;
import java.util.Optional;

public interface IVideoRepository {
    Video save(Video video);

    Optional<Video> findById(Long id);

    Optional<Video> findByPlatformVideoId(String platformVideoId);

    List<Video> findByContractId(Long contractId);

    List<Video> findByAccountId(Long accountId);

    List<Video> findByCampaignId(Long campaignId);

    List<Video> findAll();

    void delete(Long id);
}

