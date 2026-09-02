package com.clinc_cms.management.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
    @NotNull(message = "Email cannot be null")
    private String email;

    @NotNull(message = "Password cannot be null")
    private String password;
}
