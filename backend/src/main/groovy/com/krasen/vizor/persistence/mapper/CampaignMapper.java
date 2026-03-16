package com.krasen.vizor.persistence.mapper;

import com.krasen.vizor.business.domain.Campaign;
import com.krasen.vizor.persistence.entities.CampaignEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = UserMapper.class)
public interface CampaignMapper extends BaseMapper<Campaign, CampaignEntity>{
    // Domain -> Entity (DB fills id/createdAt)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "owner", source = "owner", qualifiedByName = "userToEntity")
    CampaignEntity toEntityForCreate(Campaign src);

    // Used for UPDATE
    @Named("campaignToEntity")
    @Mapping(target = "owner", source = "owner", qualifiedByName = "userToEntity")
    CampaignEntity toEntity(Campaign src);

    // Entity -> Domain (map everything, including id & createdAt)
    @Named("campaignToDomain")
    @Mapping(target = "owner", source = "owner", qualifiedByName = "userToDomain")
    Campaign toDomain(CampaignEntity src);
}