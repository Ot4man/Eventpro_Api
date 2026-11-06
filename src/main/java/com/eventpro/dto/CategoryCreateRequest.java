package com.eventpro.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequest(
        @NotBlank(message = "Le nom de la catégorie est obligatoire")
        String name
) {
}

