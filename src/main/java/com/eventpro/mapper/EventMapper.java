package com.eventpro.mapper;

import com.eventpro.dto.EventResponse;
import com.eventpro.dto.TicketCategoryResponse;
import com.eventpro.entity.Event;
import com.eventpro.entity.TicketCategoriesPerEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "organizerId", source = "organizer.id")
    EventResponse toResponse(Event event);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "name", source = "category.name")
    TicketCategoryResponse toCategoryResponse(
            TicketCategoriesPerEvent category
    );
}
