package sanosysalvos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sanosysalvos.config.JwtConfig;
import sanosysalvos.model.Rol;
import sanosysalvos.model.Status;
import sanosysalvos.model.Usuario;
import sanosysalvos.service.JwtService;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService - Pruebas Unitarias")
class JwtServiceTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtService jwtService;

    private Usuario usuario;

    // Clave secreta mínima de 32 caracteres para HMAC-SHA256
    private static final String SECRET = "clave-secreta-super-segura-12345678";

    @BeforeEach
    void setUp() {
        Rol rol = Rol.builder().idRol(1).descripcion("USER").build();
        Status activo = Status.builder().idStatus(1).descripcion("ACTIVO").build();

        usuario = Usuario.builder()
                .idUsuario(1)
                .email("juan@test.com")
                .nombreCompleto("Juan Pérez")
                .contrasena("hashed")
                .rol(rol)
                .status(activo)
                .roles(Set.of(rol))
                .build();

        when(jwtConfig.getSecret()).thenReturn(SECRET);
        when(jwtConfig.getExpiration()).thenReturn(86_400_000L); // 24h
    }

    @Test
    @DisplayName("generateToken: debe generar un token no nulo y no vacío")
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtService.generateToken(usuario);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("generateToken: el token debe contener el email del usuario como subject")
    void generateToken_shouldContainEmailAsSubject() {
        String token = jwtService.generateToken(usuario);
        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("juan@test.com");
    }

    @Test
    @DisplayName("isTokenValid: debe retornar true para un token recién generado")
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = jwtService.generateToken(usuario);

        boolean isValid = jwtService.isTokenValid(token, usuario);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: debe retornar false para un token con usuario diferente")
    void isTokenValid_shouldReturnFalseForDifferentUser() {
        String token = jwtService.generateToken(usuario);

        Usuario otroUsuario = Usuario.builder()
                .email("otro@test.com")
                .contrasena("hashed")
                .roles(Set.of())
                .build();

        boolean isValid = jwtService.isTokenValid(token, otroUsuario);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("isTokenValid: debe retornar false para un token expirado")
    void isTokenValid_shouldReturnFalseForExpiredToken() {
        // Expira en 1 milisegundo
        when(jwtConfig.getExpiration()).thenReturn(1L);

        String token = jwtService.generateToken(usuario);

        // Esperar que expire
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        boolean isValid = jwtService.isTokenValid(token, usuario);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("extractUsername: debe extraer correctamente el email del token")
    void extractUsername_shouldExtractEmailFromToken() {
        String token = jwtService.generateToken(usuario);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo(usuario.getEmail());
    }

    @Test
    @DisplayName("generateToken: tokens distintos para el mismo usuario deben tener el mismo subject")
    void generateToken_shouldProduceDifferentTokensForSameUser() {
        String token1 = jwtService.generateToken(usuario);
        String token2 = jwtService.generateToken(usuario);

        // Los tokens tienen tiempos distintos, por lo que pueden diferir levemente
        assertThat(jwtService.extractUsername(token1)).isEqualTo(jwtService.extractUsername(token2));
    }
}

