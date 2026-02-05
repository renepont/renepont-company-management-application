package com.renepont.renepontcma.service.mapper;

import com.renepont.renepontcma.domain.Category;
import com.renepont.renepontcma.domain.Company;
import com.renepont.renepontcma.domain.Transaction;
import com.renepont.renepontcma.service.dto.CategoryDTO;
import com.renepont.renepontcma.service.dto.CompanyDTO;
import com.renepont.renepontcma.service.dto.TransactionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Transaction} and its DTO {@link TransactionDTO}.
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper extends EntityMapper<TransactionDTO, Transaction> {
    @Mapping(target = "category", source = "category", qualifiedByName = "categoryName")
    @Mapping(target = "company", source = "company", qualifiedByName = "companyName")
    TransactionDTO toDto(Transaction s);

    @Named("categoryName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    CategoryDTO toDtoCategoryName(Category category);

    @Named("companyName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    CompanyDTO toDtoCompanyName(Company company);
}
