package com.krasen.vizor.persistence.mapper;

import com.krasen.vizor.business.domain.User;
import com.krasen.vizor.persistence.entities.RoleEntity;
import com.krasen.vizor.persistence.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = MapperConfig.class)
public interface UserMapper extends BaseMapper<User, UserEntity> {
    // Domain -> Entity (DB fills id/createdAt)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    UserEntity toEntityForCreate(User src);

    // Used for UPDATE
    @Named("userToEntity")
    @Mapping(target = "roles", ignore = true)
    UserEntity toEntity(User src);

    // Entity -> Domain (map everything, including id & createdAt)
    @Named("userToDomain")
    @Mapping(target = "roles", expression = "java(mapRolesToDomain(src.getRoles()))")
    User toDomain(UserEntity src);

    default Set<String> mapRolesToDomain(Set<RoleEntity> roleEntities) {
        if (roleEntities == null) {
            return new HashSet<>();
        }
        return roleEntities.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());
    }
}

