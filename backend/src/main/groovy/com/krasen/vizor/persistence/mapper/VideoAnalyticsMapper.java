package com.krasen.vizor.persistence.mapper;

import com.krasen.vizor.business.domain.VideoAnalytics;
import com.krasen.vizor.persistence.entities.VideoAnalyticsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = {VideoMapper.class})
public interface VideoAnalyticsMapper extends BaseMapper<VideoAnalytics, VideoAnalyticsEntity> {
    // Domain -> Entity (DB fills id/createdAt)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "video", source = "video", qualifiedByName = "videoToEntity")
    VideoAnalyticsEntity toEntityForCreate(VideoAnalytics src);

    // Used for UPDATE
    @Named("videoAnalyticsToEntity")
    @Mapping(target = "video", source = "video", qualifiedByName = "videoToEntity")
    VideoAnalyticsEntity toEntity(VideoAnalytics src);

    // Entity -> Domain (map everything, including id & createdAt)
    @Named("videoAnalyticsToDomain")
    @Mapping(target = "video", source = "video", qualifiedByName = "videoToDomain")
    VideoAnalytics toDomain(VideoAnalyticsEntity src);
}

