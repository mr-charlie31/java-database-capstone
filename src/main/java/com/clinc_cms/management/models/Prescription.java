package com.clinc_cms.management.models;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Document(collection = "prescriptions")
public class Prescription {

    @Id
    private String id;

    @NotNull
    @Size(min = 3, max = 100)
    private String patientName;

    @NotNull
    @Size(min = 3, max = 100)
    private String medication;

    @NotNull
    private String appointmentId;

    @Size(max = 50)
    private String dosage;

    @Size(max = 200)
    private String doctorNotes;
}