package com.clinc_cms.management.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.clinc_cms.management.models.Status;

@Getter
@Setter
public class AppointmentDTO {
    private Long id;

    @NotNull(message = "Doctor ID cannot be null")
    private Long doctorId;

    @NotNull(message = "Patient ID cannot be null")
    private Long patientId;

    @NotNull(message = "Appointment time cannot be null")
    @Future(message = "Appointment time must be in the future")
    private LocalDateTime appointmentTime;

    @NotNull(message = "Status cannot be null")
    private Status status;
}
