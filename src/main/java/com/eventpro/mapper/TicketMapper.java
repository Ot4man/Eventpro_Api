package com.eventpro.mapper;

import com.eventpro.dto.TicketResponse;
import com.eventpro.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "reservationId", source = "reservationLine.reservation.id")
    @Mapping(target = "eventTitle", source = "reservationLine.category.event.title")
    @Mapping(target = "categoryName", source = "reservationLine.category.category.name")
    @Mapping(target = "price", source = "reservationLine.fixedUnitPrice")
    @Mapping(target = "clientName", source = "reservationLine.reservation.client.name")
    TicketResponse toResponse(Ticket ticket);
}

