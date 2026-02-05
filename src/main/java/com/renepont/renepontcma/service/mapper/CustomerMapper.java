package com.renepont.renepontcma.service.mapper;

import com.renepont.renepontcma.domain.Company;
import com.renepont.renepontcma.domain.Customer;
import com.renepont.renepontcma.service.dto.CompanyDTO;
import com.renepont.renepontcma.service.dto.CustomerDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Customer} and its DTO {@link CustomerDTO}.
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper extends EntityMapper<CustomerDTO, Customer> {
    @Mapping(target = "company", source = "company", qualifiedByName = "companyName")
    CustomerDTO toDto(Customer s);

    @Named("companyName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    CompanyDTO toDtoCompanyName(Company company);
}
