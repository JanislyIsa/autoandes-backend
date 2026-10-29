package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.*;

public record LoginRequestDTO(@NotBlank @Size(max = 30) String username, @NotBlank @Size(max = 100) String password) {}

