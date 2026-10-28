package com.eventpro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(@NotBlank(message = "name is mandatory")
                                String name,
                                @NotBlank(message = "mail is obligatory")
                                @Email(message = "Email adress not valid")
                                String email,
                                @NotBlank(message = "Type of user is obligatory")
                                String userType,
                                @NotBlank(message = "Password is mandatory")
                                @Size(min = 8, message = "Password must contain at least 8 characters")
                                String password,
                                @Size(max = 20, message = "phone number must be less than 20 ")
                                String phone,

                                @Size(max = 150, message = "organization name must be less than 150 character")
                                String organisationName)
{


}
