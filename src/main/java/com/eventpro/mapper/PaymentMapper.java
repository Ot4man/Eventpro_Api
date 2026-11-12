package com.eventpro.mapper;

import com.eventpro.dto.PaymentResponse;
import com.eventpro.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "reservationId", source = "reservation.id")
    PaymentResponse toResponse(Payment payment);
}

