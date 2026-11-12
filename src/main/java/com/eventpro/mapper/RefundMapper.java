package com.eventpro.mapper;

import com.eventpro.dto.RefundResponse;
import com.eventpro.entity.Refund;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefundMapper {

    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "message", ignore = true)
    RefundResponse toResponse(Refund refund);

    default RefundResponse toResponseWithMessage(Refund refund, String message) {
        RefundResponse response = toResponse(refund);
        return new RefundResponse(
                response.id(),
                response.paymentId(),
                response.amount(),
                response.reason(),
                response.requestDate(),
                response.status(),
                message != null ? message : "Statut : " + response.status()
        );
    }
}

