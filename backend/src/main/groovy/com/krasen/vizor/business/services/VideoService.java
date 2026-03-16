package com.krasen.vizor.business.services;

import com.krasen.vizor.business.IRepo.IVideoRepository;
import com.krasen.vizor.business.IRepo.IContractRepository;
import com.krasen.vizor.business.IRepo.IAccountRepository;
import com.krasen.vizor.business.IRepo.IVideoAnalyticsRepository;
import com.krasen.vizor.business.domain.*;
import com.krasen.vizor.persistence.repos.VideoAnalyticsRepository;
import com.krasen.vizor.business.exception.VideoExceptions;
import com.krasen.vizor.business.exception.AuthExceptions;
import com.krasen.vizor.business.exception.ContractExceptions;
import com.krasen.vizor.business.exception.AccountExceptions;
import com.krasen.vizor.web.IServices.IVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VideoService implements IVideoService {
    private final IVideoRepository repo;
    private final IContractRepository contractRepo;
    private final IAccountRepository accountRepo;
    private final IVideoAnalyticsRepository videoAnalyticsRepo;
    private final VideoAnalyticsRepository videoAnalyticsRepository; // For save with videoId
    private final TikTokApiClient tiktokApiClient;

    @Override
    public Video get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> VideoExceptions.notFound(id));
    }

    @Override
    public List<Video> findByContractId(Long contractId, Long currentUserId) {
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();

        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> ContractExceptions.notFound(contractId));

        if (!contract.getCreator().getId().equals(currentUserId) && 
            !contract.getCampaign().getOwner().getId().equals(currentUserId)) {
            throw AuthExceptions.unauthorized();
        }

        return repo.findByContractId(contractId);
    }

    @Override
    public List<Video> findByAccountId(Long accountId, Long currentUserId) {
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> AccountExceptions.notFound(accountId));

        if (!account.getCreator().getId().equals(currentUserId)) {
            throw AuthExceptions.unauthorized();
        }

        return repo.findByAccountId(accountId);
    }

    @Override
    public List<Video> findByCampaignId(Long campaignId, Long currentUserId) {
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();

        return repo.findByCampaignId(campaignId);
    }

    @Transactional
    @Override
    public Video sync(Video input, Long currentUserId) {
        if (input == null) throw VideoExceptions.nullInput();
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();

        if (input.getPlatformVideoId() == null || input.getPlatformVideoId().trim().isEmpty()) {
            throw VideoExceptions.invalidPlatformVideoId();
        }

        if (input.getContract() == null || input.getContract().getId() == null) {
            throw VideoExceptions.nullInput();
        }

        if (input.getAccount() == null || input.getAccount().getId() == null) {
            throw VideoExceptions.nullInput();
        }

        Contract contract = contractRepo.findById(input.getContract().getId())
                .orElseThrow(() -> ContractExceptions.notFound(input.getContract().getId()));

        Account account = accountRepo.findById(input.getAccount().getId())
                .orElseThrow(() -> AccountExceptions.notFound(input.getAccount().getId()));

        // Verify authorization - only contract creator or campaign owner can sync videos
        if (!contract.getCreator().getId().equals(currentUserId) && 
            !contract.getCampaign().getOwner().getId().equals(currentUserId)) {
            throw AuthExceptions.unauthorized();
        }

        // Verify account belongs to contract creator
        if (!account.getCreator().getId().equals(contract.getCreator().getId())) {
            throw AuthExceptions.unauthorized();
        }

        input.setContract(contract);
        input.setAccount(account);

        // Check if video already exists (upsert pattern)
        Optional<Video> existing = repo.findByPlatformVideoId(input.getPlatformVideoId());
        if (existing.isPresent()) {
            Video existingVideo = existing.get();
            // Update existing video with new data from API
            existingVideo.setPlatformVideoLink(input.getPlatformVideoLink());
            existingVideo.setLocation(input.getLocation());
            existingVideo.setTitle(input.getTitle());
            existingVideo.setDescription(input.getDescription());
            existingVideo.setDuration(input.getDuration());
            existingVideo.setPostedAt(input.getPostedAt());
            existingVideo.setIsOnTime(input.getIsOnTime());
            // Contract and account shouldn't change, but ensure they're set correctly
            existingVideo.setContract(contract);
            existingVideo.setAccount(account);
            return repo.save(existingVideo);
        } else {
            // Create new video
            input.setId(null);
            input.setCreatedAt(null);
            input.setDeletedAt(null);
            return repo.save(input);
        }
    }

    @Transactional
    @Override
    public void delete(Long id, Long currentUserId) {
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();

        Video existing = get(id);

        Contract contract = contractRepo.findById(existing.getContract().getId())
                .orElseThrow(() -> ContractExceptions.notFound(existing.getContract().getId()));

        if (!contract.getCreator().getId().equals(currentUserId) && 
            !contract.getCampaign().getOwner().getId().equals(currentUserId)) {
            throw AuthExceptions.unauthorized();
        }

        repo.delete(existing.getId());
    }

    @Transactional
    public VideoAnalytics syncVideoAnalytics(Long videoId) {
        Video video = get(videoId);

        // Get the latest analytics snapshot
        List<VideoAnalytics> existingAnalytics = videoAnalyticsRepo.findByVideoId(videoId);
        VideoAnalytics latestAnalytics = null;
        if (!existingAnalytics.isEmpty()) {
            latestAnalytics = existingAnalytics.get(0);
        }

        // Fetch from TikTok API (or mock) - pass latest analytics so it can add growth
        Map<String, Object> metrics = tiktokApiClient.fetchVideoMetrics(
            video.getPlatformVideoId(),
            latestAnalytics
        );

        // Create analytics snapshot
        VideoAnalytics analytics = VideoAnalytics.builder()
            .video(video)
            .viewsCount(((Number) metrics.get("views")).longValue())
            .likesCount(((Number) metrics.get("likes")).longValue())
            .commentsCount(((Number) metrics.get("comments")).longValue())
            .sharesCount(((Number) metrics.get("shares")).longValue())
            .recordedAt(OffsetDateTime.now())
            .build();

        // Use repository method that accepts videoId to set managed entity reference
        return videoAnalyticsRepository.save(analytics, videoId);
    }

    public List<VideoAnalytics> getAnalyticsByVideoId(Long videoId) {
        return videoAnalyticsRepo.findByVideoId(videoId);
    }

    public List<VideoDailyAnalytics> getDailyAnalyticsByCampaignId(Long campaignId, Long currentUserId) {
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();

        // ORM handles filtering and sorting - get pre-sorted analytics
        List<VideoAnalytics> allAnalytics = videoAnalyticsRepo.findByCampaignIdOrdered(campaignId);
        return calculateDailyIncrementalAnalytics(allAnalytics);
    }

    public List<VideoDailyAnalytics> getDailyAnalyticsByContractId(Long contractId, Long currentUserId) {
        if (currentUserId == null) throw AuthExceptions.authenticationRequired();

        // Authorization check
        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> ContractExceptions.notFound(contractId));

        if (!contract.getCreator().getId().equals(currentUserId) && 
            !contract.getCampaign().getOwner().getId().equals(currentUserId)) {
            throw AuthExceptions.unauthorized();
        }

        // ORM handles filtering and sorting - get pre-sorted analytics
        List<VideoAnalytics> allAnalytics = videoAnalyticsRepo.findByContractIdOrdered(contractId);
        return calculateDailyIncrementalAnalytics(allAnalytics);
    }

    /**
     * Calculates daily incremental analytics from cumulative snapshots.
     * Groups by date (latest snapshot per video per day), calculates deltas from previous day,
     * and aggregates across all videos.
     */
    private List<VideoDailyAnalytics> calculateDailyIncrementalAnalytics(List<VideoAnalytics> allAnalytics) {
        if (allAnalytics.isEmpty()) {
            return Collections.emptyList();
        }

        // ORM returns data sorted by: video_id ASC, recorded_at DESC
        // This means for each video-date combo, the first entry is the latest snapshot
        // We only keep the first one we see for each unique video-date combination
        
        // Step 1: Get latest snapshot per video per date
        List<VideoAnalytics> latestPerVideoPerDate = new ArrayList<>();
        Long lastVideoId = null;
        LocalDate lastDate = null;
        
        for (VideoAnalytics snapshot : allAnalytics) {
            LocalDate date = snapshot.getRecordedAt().withOffsetSameInstant(ZoneOffset.UTC).toLocalDate();
            Long videoId = snapshot.getVideo().getId();
            
            // Skip if this is the same video-date combo as the previous entry
            // Previous entry is the latest because data is sorted DESC by time
            if (!videoId.equals(lastVideoId) || !date.equals(lastDate)) {
                latestPerVideoPerDate.add(snapshot);
                lastVideoId = videoId;
                lastDate = date;
            }
        }

        // Step 2: Sort by date (ORM sorted by video, we need by date)
        latestPerVideoPerDate.sort((a, b) -> {
            LocalDate dateA = a.getRecordedAt().withOffsetSameInstant(ZoneOffset.UTC).toLocalDate();
            LocalDate dateB = b.getRecordedAt().withOffsetSameInstant(ZoneOffset.UTC).toLocalDate();
            return dateA.compareTo(dateB);
        });

        // Step 3: Calculate deltas from analytics
        Map<Long, VideoAnalytics> previousSnapshotPerVideo = new HashMap<>();
        Map<String, VideoDailyAnalytics> dailyTotals = new HashMap<>();
        Set<Long> videosSeen = new HashSet<>(); // Track videos we've already counted as posts
        
        for (VideoAnalytics todaySnapshot : latestPerVideoPerDate) {
            Long videoId = todaySnapshot.getVideo().getId();
            LocalDate date = todaySnapshot.getRecordedAt().withOffsetSameInstant(ZoneOffset.UTC).toLocalDate();
            String dateKey = date.toString();
            
            // Get previous snapshot for this video
            VideoAnalytics previousSnapshot = previousSnapshotPerVideo.get(videoId);
            
            // Calculate deltas
            long viewsDelta = 0;
            long likesDelta = 0;
            long commentsDelta = 0;
            long sharesDelta = 0;
            
            long todayViews = todaySnapshot.getViewsCount() != null ? todaySnapshot.getViewsCount() : 0L;
            long todayLikes = todaySnapshot.getLikesCount() != null ? todaySnapshot.getLikesCount() : 0L;
            long todayComments = todaySnapshot.getCommentsCount() != null ? todaySnapshot.getCommentsCount() : 0L;
            long todayShares = todaySnapshot.getSharesCount() != null ? todaySnapshot.getSharesCount() : 0L;
            
            if (previousSnapshot == null) {
                // First day for this video - delta is the total value
                viewsDelta = todayViews;
                likesDelta = todayLikes;
                commentsDelta = todayComments;
                sharesDelta = todayShares;
            } else {
                // Calculate difference from previous day
                long prevViews = previousSnapshot.getViewsCount() != null ? previousSnapshot.getViewsCount() : 0L;
                long prevLikes = previousSnapshot.getLikesCount() != null ? previousSnapshot.getLikesCount() : 0L;
                long prevComments = previousSnapshot.getCommentsCount() != null ? previousSnapshot.getCommentsCount() : 0L;
                long prevShares = previousSnapshot.getSharesCount() != null ? previousSnapshot.getSharesCount() : 0L;
                
                viewsDelta = Math.max(0, todayViews - prevViews);
                likesDelta = Math.max(0, todayLikes - prevLikes);
                commentsDelta = Math.max(0, todayComments - prevComments);
                sharesDelta = Math.max(0, todayShares - prevShares);
            }
            
            // Add to daily totals
            VideoDailyAnalytics daily = dailyTotals.get(dateKey);
            if (daily == null) {
                daily = VideoDailyAnalytics.builder()
                        .date(date)
                        .views(0L)
                        .likes(0L)
                        .comments(0L)
                        .shares(0L)
                        .posts(0)
                        .build();
                dailyTotals.put(dateKey, daily);
            }
            
            daily.setViews(daily.getViews() + viewsDelta);
            daily.setLikes(daily.getLikes() + likesDelta);
            daily.setComments(daily.getComments() + commentsDelta);
            daily.setShares(daily.getShares() + sharesDelta);
            
            // Count post only on the FIRST day we see this video (incremental post count)
            if (!videosSeen.contains(videoId)) {
                daily.setPosts(daily.getPosts() + 1);
                videosSeen.add(videoId);
            }
            
            // Remember this snapshot for next iteration
            previousSnapshotPerVideo.put(videoId, todaySnapshot);
        }
        
        // Convert to list and sort by date
        return dailyTotals.values().stream()
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .toList();
    }
}
