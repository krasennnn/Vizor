package com.krasen.vizor.persistence.mapper;

import com.krasen.vizor.business.domain.Video;
import com.krasen.vizor.persistence.entities.VideoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = {ContractMapper.class, AccountMapper.class})
public interface VideoMapper extends BaseMapper<Video, VideoEntity> {
    // Domain -> Entity (DB fills id/createdAt)
    @Named("videoToEntityForCreate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "contract", source = "contract", qualifiedByName = "contractToEntity")
    @Mapping(target = "account", source = "account", qualifiedByName = "accountToEntity")
    VideoEntity toEntityForCreate(Video src);

    // Used for UPDATE
    @Named("videoToEntity")
    @Mapping(target = "contract", source = "contract", qualifiedByName = "contractToEntity")
    @Mapping(target = "account", source = "account", qualifiedByName = "accountToEntity")
    VideoEntity toEntity(Video src);

    // Entity -> Domain (map everything, including id & createdAt)
    @Named("videoToDomain")
    @Mapping(target = "contract", source = "contract", qualifiedByName = "contractToDomain")
    @Mapping(target = "account", source = "account", qualifiedByName = "accountToDomain")
    Video toDomain(VideoEntity src);
}

