package com.clinc_cms.management.models;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Name cannot be null")
    @Size(min = 3 , max = 100)
    private String name;

    @NotNull(message = "Specialization cannot be null")
    @Size(min = 3 , max = 100)
    private String speciality;
    
    @Email
    @NotNull(message = "Email cannot be null and must be a valid email address")
    private String email;

    @Size(min = 6)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    @NotNull(message = "Phone number cannot be null")
    private String phone;

    @ElementCollection
    private List<String> availableTimes;
}
