package com.eventpro.mapper;

import com.eventpro.dto.PromotionResponseDTO;
import com.eventpro.entity.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "eventId", source = "event.id")
    PromotionResponseDTO toDTO(Promotion promotion);
}
