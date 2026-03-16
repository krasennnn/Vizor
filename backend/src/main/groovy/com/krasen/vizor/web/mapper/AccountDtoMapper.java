package com.krasen.vizor.web.mapper;

import com.krasen.vizor.business.domain.Account;
import com.krasen.vizor.web.DTOs.AccountDTOs.AccountResponse;
import com.krasen.vizor.web.DTOs.AccountDTOs.AccountSyncRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountDtoMapper {

    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isActive", expression = "java(request.isActive())")
    Account toDomain(AccountSyncRequest request);

    @Mapping(target = "creatorId", expression = "java(domain.getCreator() != null ? domain.getCreator().getId() : null)")
    @Mapping(target = "isActive", expression = "java(domain.isActive())")
    AccountResponse toResponse(Account domain);
}

