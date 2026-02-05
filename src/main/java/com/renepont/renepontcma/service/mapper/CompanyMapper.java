package com.renepont.renepontcma.service.mapper;

import com.renepont.renepontcma.domain.Company;
import com.renepont.renepontcma.domain.UserProfile;
import com.renepont.renepontcma.service.dto.CompanyDTO;
import com.renepont.renepontcma.service.dto.UserProfileDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Company} and its DTO {@link CompanyDTO}.
 */
@Mapper(componentModel = "spring")
public interface CompanyMapper extends EntityMapper<CompanyDTO, Company> {
    @Mapping(target = "userProfiles", source = "userProfiles", qualifiedByName = "userProfileIdSet")
    CompanyDTO toDto(Company s);

    @Mapping(target = "removeUserProfile", ignore = true)
    Company toEntity(CompanyDTO companyDTO);

    @Named("userProfileId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserProfileDTO toDtoUserProfileId(UserProfile userProfile);

    @Named("userProfileIdSet")
    default Set<UserProfileDTO> toDtoUserProfileIdSet(Set<UserProfile> userProfile) {
        return userProfile.stream().map(this::toDtoUserProfileId).collect(Collectors.toSet());
    }
}
