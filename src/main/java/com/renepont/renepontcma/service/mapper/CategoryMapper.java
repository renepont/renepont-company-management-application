package com.renepont.renepontcma.service.mapper;

import com.renepont.renepontcma.domain.Category;
import com.renepont.renepontcma.service.dto.CategoryDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Category} and its DTO {@link CategoryDTO}.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper extends EntityMapper<CategoryDTO, Category> {}
