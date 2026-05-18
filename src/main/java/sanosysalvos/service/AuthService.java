package sanosysalvos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sanosysalvos.dto.request.LoginRequest;
import sanosysalvos.dto.request.RegisterRequest;
import sanosysalvos.dto.response.LoginResponse;
import sanosysalvos.model.Rol;
import sanosysalvos.model.Status;
import org.springframework.security.authentication.BadCredentialsException;
import sanosysalvos.model.Usuario;
import sanosysalvos.repository.RolRepository;
import sanosysalvos.repository.StatusRepository;
import sanosysalvos.repository.UserRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final StatusRepository statusRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    // --- UserDetailsService (requerido por Spring Security) ---

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    // --- Registro ---

    @SuppressWarnings("null")
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Rol rolUser = rolRepository.findByDescripcion("USUARIO")
                .orElseThrow(() -> new IllegalStateException("Rol USUARIO no encontrado en BD"));

        Status statusActivo = statusRepository.findByDescripcion("ACTIVO")
                .orElseThrow(() -> new IllegalStateException("Status ACTIVO no encontrado en BD"));

        Usuario usuario = Usuario.builder()
                .nombreCompleto(request.getNombreCompleto())
                .email(request.getEmail())
                .contrasena(passwordService.encode(request.getContrasena()))
                .rol(rolUser)
                .status(statusActivo)
                .roles(Set.of(rolUser))
                .emailVerificado(false)
                .build();

        Usuario savedUser = userRepository.save(usuario);
        String token = jwtService.generateToken(savedUser);

        return buildResponse(token, savedUser);
    }

    // --- Login ---

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = (Usuario) loadUserByUsername(request.getEmail());

        if (!passwordService.matches(request.getContrasena(), usuario.getContrasena())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        // Actualiza last_login_at en BD
        usuario.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(usuario);

        String token = jwtService.generateToken(usuario);

        return buildResponse(token, usuario);
    }

    // --- Verificación de email ---

    public void verificarEmail(String email) {
        Usuario usuario = (Usuario) loadUserByUsername(email);
        if (usuario.isEmailVerificado()) {
            throw new IllegalArgumentException("El email ya está verificado");
        }
        usuario.setEmailVerificado(true);
        userRepository.save(usuario);
    }

    // --- Helper ---

    private LoginResponse buildResponse(String token, Usuario usuario) {
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol() != null ? usuario.getRol().getDescripcion() : "USER")
                .build();
    }
}
