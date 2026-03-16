package com.krasen.vizor.web.mapper;

import com.krasen.vizor.business.domain.Campaign;
import com.krasen.vizor.business.domain.Contract;
import com.krasen.vizor.business.domain.User;
import com.krasen.vizor.web.DTOs.ContractDTOs.ContractCreateRequest;
import com.krasen.vizor.web.DTOs.ContractDTOs.ContractResponse;
import com.krasen.vizor.web.DTOs.ContractDTOs.ContractUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = CampaignDtoMapper.class)
public interface ContractDtoMapper {

        @Mapping(target = "campaign", expression = "java(createCampaignFromId(request.campaignId()))")
        @Mapping(target = "creator", expression = "java(request.creatorId() != null ? createUserFromId(request.creatorId()) : null)")
        @Mapping(target = "id", ignore = true)
        @Mapping(target = "createdAt", ignore = true)
        @Mapping(target = "approvedByOwner", ignore = true)
        @Mapping(target = "startAt", ignore = true)
        @Mapping(target = "deadlineAt", ignore = true)
        @Mapping(target = "completedAt", ignore = true)
        @Mapping(target = "deletedAt", ignore = true)
        Contract toDomain(ContractCreateRequest request);

        Contract toDomain(ContractUpdateRequest request);

        @Mapping(target = "campaign", source = "campaign")
        @Mapping(target = "deletedAt", source = "deletedAt")
        @Mapping(target = "creatorId", expression = "java(domain.getCreator() != null ? domain.getCreator().getId() : null)")
        @Mapping(target = "creatorUsername", expression = "java(domain.getCreator() != null ? domain.getCreator().getUsername() : null)")
        ContractResponse toResponse(Contract domain);

        @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
        void patch(ContractUpdateRequest request, @MappingTarget Contract target);

        default Campaign createCampaignFromId(Long campaignId) {
            if (campaignId == null) {
                return null;
            }
            return Campaign.builder().id(campaignId).build();
        }

        default User createUserFromId(Long userId) {
            if (userId == null) {
                return null;
            }
            return User.builder().id(userId).build();
        }
}
