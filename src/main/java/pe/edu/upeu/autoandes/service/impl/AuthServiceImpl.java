package pe.edu.upeu.autoandes.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.config.JwtService;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;
import pe.edu.upeu.autoandes.security.UsuarioPrincipal;
import pe.edu.upeu.autoandes.service.AuthService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
    private static final String HASH_AUSENTE = "$2a$10$QLW5fBeuiWdRO4Zc2UoUse6/6X4cQLtzc2p9awUYDwjic3kFIhgXu";
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;

    @Override
    public LoginResponseDTO login(LoginRequestDTO r) {
        var encontrado = usuarios.findByUsername(r.username());
        String hash = encontrado.map(u -> u.getPassword()).orElse(HASH_AUSENTE);
        boolean coincide = passwordEncoder.matches(r.password(), hash);
        if (encontrado.isEmpty() || !coincide || !Boolean.TRUE.equals(encontrado.get().getActivo())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }
        var usuario = UsuarioPrincipal.de(encontrado.get());
        return new LoginResponseDTO(jwt.generar(usuario), "Bearer", jwt.expiracionSegundos(),
                usuario.nombreCompleto(), usuario.roles(), usuario.permisos());
    }

    @Override
    public UsuarioActualResponseDTO actual(String username) {
        var usuario = usuarios.findByUsername(username).filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .map(UsuarioPrincipal::de).orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));
        return new UsuarioActualResponseDTO(usuario.id(), usuario.username(), usuario.nombreCompleto(),
                usuario.roles(), usuario.permisos());
    }
}

