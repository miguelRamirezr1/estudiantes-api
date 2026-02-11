package com.devops.estudiantes.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Model class representing a student in the system, this defines the structure of the student data.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estudiante {
    //student's id
    @NotNull(message = "El ID no puede ser nulo")
    @NotBlank(message = "El ID no puede estar vacío")
    private String id;
    //student's full name
    @NotNull(message = "El Nombre no puede ser nulo")
    @NotBlank(message = "El Nombre no puede estar vacío")
    private String nombre;
    //student's academic program
    @NotNull(message = "La carrera no puede ser nulo")
    @NotBlank(message = "La carrera no puede estar vacío")
    private String carrera;
}
