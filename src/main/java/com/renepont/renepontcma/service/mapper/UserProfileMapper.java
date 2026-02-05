package com.renepont.renepontcma.service.mapper;

import com.renepont.renepontcma.domain.Company;
import com.renepont.renepontcma.domain.User;
import com.renepont.renepontcma.domain.UserProfile;
import com.renepont.renepontcma.service.dto.CompanyDTO;
import com.renepont.renepontcma.service.dto.UserDTO;
import com.renepont.renepontcma.service.dto.UserProfileDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link UserProfile} and its DTO {@link UserProfileDTO}.
 */
@Mapper(componentModel = "spring")
public interface UserProfileMapper extends EntityMapper<UserProfileDTO, UserProfile> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    @Mapping(target = "companies", source = "companies", qualifiedByName = "companyIdSet")
    UserProfileDTO toDto(UserProfile s);

    @Mapping(target = "companies", ignore = true)
    @Mapping(target = "removeCompany", ignore = true)
    UserProfile toEntity(UserProfileDTO userProfileDTO);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("companyId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CompanyDTO toDtoCompanyId(Company company);

    @Named("companyIdSet")
    default Set<CompanyDTO> toDtoCompanyIdSet(Set<Company> company) {
        return company.stream().map(this::toDtoCompanyId).collect(Collectors.toSet());
    }
}
