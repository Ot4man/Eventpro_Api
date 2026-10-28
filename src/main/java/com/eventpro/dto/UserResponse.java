
package com.eventpro.dto;

public record UserResponse(
        Long id,
        String name,
        String email,
        String userType,
        String phone,
        String organisationName
) {
}
