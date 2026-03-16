package com.krasen.vizor.persistence.mapper;

import com.krasen.vizor.business.domain.Account;
import com.krasen.vizor.persistence.entities.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = UserMapper.class)
public interface AccountMapper extends BaseMapper<Account, AccountEntity> {
    // Domain -> Entity (DB fills id/createdAt)
    @Named("accountToEntityForCreate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "creator", source = "creator", qualifiedByName = "userToEntity")
    @Mapping(target = "isActive", expression = "java(src.isActive())")
    AccountEntity toEntityForCreate(Account src);

    // Used for UPDATE
    @Named("accountToEntity")
    @Mapping(target = "creator", source = "creator", qualifiedByName = "userToEntity")
    @Mapping(target = "isActive", expression = "java(src.isActive())")
    AccountEntity toEntity(Account src);

    // Entity -> Domain (map everything, including id & createdAt)
    @Named("accountToDomain")
    @Mapping(target = "creator", source = "creator", qualifiedByName = "userToDomain")
    @Mapping(target = "isActive", expression = "java(src.isActive())")
    Account toDomain(AccountEntity src);
}

