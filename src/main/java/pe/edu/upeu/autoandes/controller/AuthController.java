package pe.edu.upeu.autoandes.controller;

import java.security.Principal;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService servicio;
    @PostMapping("/login")
    @SecurityRequirements
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO r) { return servicio.login(r); }
    @GetMapping("/me")
    public UsuarioActualResponseDTO actual(Principal usuario) { return servicio.actual(usuario.getName()); }
}

