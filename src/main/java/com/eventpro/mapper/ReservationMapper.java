package com.eventpro.mapper;

import com.eventpro.dto.ReservationResponse;
import com.eventpro.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "clientId", source = "client.id")
    ReservationResponse toResponse(Reservation reservation);
}

