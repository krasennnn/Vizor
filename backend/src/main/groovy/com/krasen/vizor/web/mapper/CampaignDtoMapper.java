package com.krasen.vizor.web.mapper;

import com.krasen.vizor.business.domain.Campaign;
import com.krasen.vizor.web.DTOs.CampaignDTOs.CampaignCreateRequest;
import com.krasen.vizor.web.DTOs.CampaignDTOs.CampaignResponse;
import com.krasen.vizor.web.DTOs.CampaignDTOs.CampaignUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CampaignDtoMapper {

    @Mapping(target = "owner", ignore = true)
    Campaign toDomain(CampaignCreateRequest request);

    Campaign toDomain(CampaignUpdateRequest request);

    @Mapping(target = "ownerId", expression = "java(domain.getOwner() != null ? domain.getOwner().getId() : null)")
    @Mapping(target = "ownerUsername", expression = "java(domain.getOwner() != null ? domain.getOwner().getUsername() : null)")
    CampaignResponse toResponse(Campaign domain);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(CampaignUpdateRequest request, @MappingTarget Campaign target);
}
