package com.krasen.vizor.web.mapper;

import com.krasen.vizor.business.domain.Contract;
import com.krasen.vizor.business.domain.Account;
import com.krasen.vizor.business.domain.Video;
import com.krasen.vizor.web.DTOs.VideoDTOs.VideoResponse;
import com.krasen.vizor.web.DTOs.VideoDTOs.VideoSyncRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VideoDtoMapper {

    @Mapping(target = "contract", expression = "java(createContractFromId(request.contractId()))")
    @Mapping(target = "account", expression = "java(createAccountFromId(request.accountId()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Video toDomain(VideoSyncRequest request);

    @Mapping(target = "contractId", expression = "java(domain.getContract() != null ? domain.getContract().getId() : null)")
    @Mapping(target = "accountId", expression = "java(domain.getAccount() != null ? domain.getAccount().getId() : null)")
    VideoResponse toResponse(Video domain);

    default Contract createContractFromId(Long id) {
        if (id == null) return null;
        return Contract.builder().id(id).build();
    }

    default Account createAccountFromId(Long id) {
        if (id == null) return null;
        return Account.builder().id(id).build();
    }
}

