package sanosysalvos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sanosysalvos.service.PasswordService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordService - Pruebas Unitarias")
class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode((CharSequence) "password123")).thenReturn("$2a$hashed");
        doReturn(true).when(passwordEncoder).matches((CharSequence) "password123", "$2a$hashed");
        doReturn(false).when(passwordEncoder).matches((CharSequence) "wrongpass", "$2a$hashed");
    }

    @Test
    @DisplayName("encode: debe retornar contraseña cifrada")
    void encode_shouldReturnHashedPassword() {
        String result = passwordService.encode("password123");

        assertThat(result).isEqualTo("$2a$hashed");
        verify(passwordEncoder).encode((CharSequence) "password123");
    }

    @Test
    @DisplayName("encode: debe delegar al PasswordEncoder")
    void encode_shouldDelegateToEncoder() {
        passwordService.encode("password123");

        verify(passwordEncoder, times(1)).encode((CharSequence) "password123");
    }

    @Test
    @DisplayName("matches: debe retornar true cuando la contraseña es correcta")
    void matches_shouldReturnTrueForCorrectPassword() {
        boolean result = passwordService.matches("password123", "$2a$hashed");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("matches: debe retornar false cuando la contraseña es incorrecta")
    void matches_shouldReturnFalseForWrongPassword() {
        boolean result = passwordService.matches("wrongpass", "$2a$hashed");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("matches: debe delegar al PasswordEncoder")
    void matches_shouldDelegateToEncoder() {
        passwordService.matches("password123", "$2a$hashed");

        verify(passwordEncoder, times(1)).matches((CharSequence) "password123", "$2a$hashed");
    }
}

