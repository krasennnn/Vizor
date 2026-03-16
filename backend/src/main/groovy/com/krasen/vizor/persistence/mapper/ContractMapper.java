package com.krasen.vizor.persistence.mapper;

import com.krasen.vizor.business.domain.Contract;
import com.krasen.vizor.persistence.entities.ContractEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = {CampaignMapper.class, UserMapper.class})
public interface ContractMapper extends BaseMapper<Contract, ContractEntity> {
    @Named("contractToEntityForCreate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "campaign", source = "campaign", qualifiedByName = "campaignToEntity")
    @Mapping(target = "creator", source = "creator", qualifiedByName = "userToEntity")
    ContractEntity toEntityForCreate(Contract src);

    // Used for UPDATE
    @Named("contractToEntity")
    @Mapping(target = "campaign", source = "campaign", qualifiedByName = "campaignToEntity")
    @Mapping(target = "creator", source = "creator", qualifiedByName = "userToEntity")
    ContractEntity toEntity(Contract src);

    // Entity -> Domain (map everything, including id & createdAt)
    @Named("contractToDomain")
    @Mapping(target = "campaign", source = "campaign", qualifiedByName = "campaignToDomain")
    @Mapping(target = "creator", source = "creator", qualifiedByName = "userToDomain")
    Contract toDomain(ContractEntity src);
}
