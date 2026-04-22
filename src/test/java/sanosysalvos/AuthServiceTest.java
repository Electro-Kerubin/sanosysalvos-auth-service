package sanosysalvos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import sanosysalvos.dto.request.LoginRequest;
import sanosysalvos.dto.request.RegisterRequest;
import sanosysalvos.dto.response.LoginResponse;
import sanosysalvos.model.Rol;
import sanosysalvos.model.Status;
import sanosysalvos.model.Usuario;
import sanosysalvos.repository.RolRepository;
import sanosysalvos.repository.StatusRepository;
import sanosysalvos.repository.UserRepository;
import sanosysalvos.service.AuthService;
import sanosysalvos.service.JwtService;
import sanosysalvos.service.PasswordService;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Pruebas Unitarias")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RolRepository rolRepository;
    @Mock private StatusRepository statusRepository;
    @Mock private PasswordService passwordService;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    private Rol rolUser;
    private Status activo;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        rolUser  = Rol.builder().idRol(1).descripcion("USER").build();
        activo   = Status.builder().idStatus(1).descripcion("ACTIVO").build();
        usuario  = Usuario.builder()
                .idUsuario(1)
                .email("juan@test.com")
                .nombreCompleto("Juan Pérez")
                .contrasena("hashed_password")
                .rol(rolUser)
                .status(activo)
                .roles(Set.of(rolUser))
                .emailVerificado(false)
                .build();
    }

    // ===================== REGISTER =====================

    @Test
    @DisplayName("register: debe retornar token cuando el email es nuevo")
    void register_shouldReturnTokenWhenEmailIsNew() {
        RegisterRequest req = new RegisterRequest();
        req.setNombreCompleto("Juan Pérez");
        req.setEmail("juan@test.com");
        req.setContrasena("password123");
        req.setIdRol(1);

        doReturn(false).when(userRepository).existsByEmail(req.getEmail());
        when(rolRepository.findById(1)).thenReturn(Optional.of(rolUser));
        when(statusRepository.findByDescripcion("ACTIVO")).thenReturn(Optional.of(activo));
        when(passwordService.encode(req.getContrasena())).thenReturn("hashed_password");
        when(userRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("jwt-token");

        LoginResponse response = authService.register(req);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("juan@test.com");
        assertThat(response.getRol()).isEqualTo("USER");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getNombreCompleto()).isEqualTo("Juan Pérez");
        verify(userRepository).save(any(Usuario.class));
        verify(passwordService).encode("password123");
    }

    @Test
    @DisplayName("register: debe lanzar excepción cuando el email ya existe")
    void register_shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("duplicado@test.com");
        req.setContrasena("password123");
        req.setNombreCompleto("Test User");
        req.setIdRol(1);

        doReturn(true).when(userRepository).existsByEmail(req.getEmail());

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está registrado");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: debe lanzar excepción cuando el rol no existe")
    void register_shouldThrowWhenRolNotFound() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("nuevo@test.com");
        req.setContrasena("password123");
        req.setNombreCompleto("Nuevo");
        req.setIdRol(99);

        doReturn(false).when(userRepository).existsByEmail(req.getEmail());
        when(rolRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rol no encontrado");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: debe lanzar excepción cuando el status ACTIVO no existe en BD")
    void register_shouldThrowWhenStatusActivoNotFound() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("nuevo@test.com");
        req.setContrasena("password123");
        req.setNombreCompleto("Nuevo");
        req.setIdRol(1);

        doReturn(false).when(userRepository).existsByEmail(req.getEmail());
        when(rolRepository.findById(1)).thenReturn(Optional.of(rolUser));
        when(statusRepository.findByDescripcion("ACTIVO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ACTIVO");

        verify(userRepository, never()).save(any());
    }

    // ===================== LOGIN =====================

    @Test
    @DisplayName("login: debe retornar token con credenciales válidas")
    void login_shouldReturnTokenOnValidCredentials() {
        LoginRequest req = new LoginRequest();
        req.setEmail("juan@test.com");
        req.setContrasena("password123");

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(usuario));
        when(userRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken(usuario)).thenReturn("jwt-token");

        LoginResponse response = authService.login(req);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getEmail()).isEqualTo("juan@test.com");
        assertThat(response.getRol()).isEqualTo("USER");
        verify(userRepository).save(any(Usuario.class)); // actualiza last_login_at
    }

    @Test
    @DisplayName("login: debe lanzar excepción cuando el usuario no existe")
    void login_shouldThrowWhenUserNotFound() {
        LoginRequest req = new LoginRequest();
        req.setEmail("noexiste@test.com");
        req.setContrasena("pass");

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ===================== VERIFICAR EMAIL =====================

    @Test
    @DisplayName("verificarEmail: debe marcar el email como verificado")
    void verificarEmail_shouldSetEmailVerificadoTrue() {
        usuario.setEmailVerificado(false);

        when(userRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(userRepository.save(any(Usuario.class))).thenReturn(usuario);

        authService.verificarEmail(usuario.getEmail());

        assertThat(usuario.isEmailVerificado()).isTrue();
        verify(userRepository).save(usuario);
    }

    @Test
    @DisplayName("verificarEmail: debe lanzar excepción si el email ya está verificado")
    void verificarEmail_shouldThrowWhenAlreadyVerified() {
        usuario.setEmailVerificado(true);

        when(userRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.verificarEmail(usuario.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está verificado");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("verificarEmail: debe lanzar excepción si el usuario no existe")
    void verificarEmail_shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("fantasma@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verificarEmail("fantasma@test.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ===================== loadUserByUsername =====================

    @Test
    @DisplayName("loadUserByUsername: debe retornar el usuario cuando existe")
    void loadUserByUsername_shouldReturnUserWhenExists() {
        when(userRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        var result = authService.loadUserByUsername(usuario.getEmail());

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("juan@test.com");
    }

    @Test
    @DisplayName("loadUserByUsername: debe lanzar UsernameNotFoundException cuando no existe")
    void loadUserByUsername_shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loadUserByUsername("noexiste@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("noexiste@test.com");
    }
}

