package com.eventpro.mapper;

import com.eventpro.dto.CategoryResponse;
import com.eventpro.entity.TicketCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(TicketCategory category);
}

