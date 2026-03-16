package com.krasen.vizor.web.IServices;

import com.krasen.vizor.business.domain.Video;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IVideoService {
    Video get(Long id);

    List<Video> findByContractId(Long contractId, Long currentUserId);

    List<Video> findByAccountId(Long accountId, Long currentUserId);

    List<Video> findByCampaignId(Long campaignId, Long currentUserId);

    @Transactional
    Video sync(Video input, Long currentUserId);

    @Transactional
    void delete(Long id, Long currentUserId);
}

