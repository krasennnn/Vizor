package com.krasen.vizor.web.mapper;

import com.krasen.vizor.business.domain.VideoAnalytics;
import com.krasen.vizor.business.domain.VideoDailyAnalytics;
import com.krasen.vizor.web.DTOs.VideoDTOs.VideoAnalyticsResponse;
import com.krasen.vizor.web.DTOs.VideoDTOs.VideoDailyAnalyticsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VideoAnalyticsDtoMapper {
    @Mapping(target = "videoId", expression = "java(domain.getVideo() != null ? domain.getVideo().getId() : null)")
    VideoAnalyticsResponse toResponse(VideoAnalytics domain);

    @Mapping(target = "date", expression = "java(domain.getDate().toString())")
    VideoDailyAnalyticsResponse toDailyResponse(VideoDailyAnalytics domain);

    List<VideoDailyAnalyticsResponse> toDailyResponseList(List<VideoDailyAnalytics> domains);
}


