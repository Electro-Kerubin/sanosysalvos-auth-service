package sanosysalvos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sanosysalvos.controller.AuthController;
import sanosysalvos.dto.request.LoginRequest;
import sanosysalvos.dto.request.RegisterRequest;
import sanosysalvos.dto.response.LoginResponse;
import sanosysalvos.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Pruebas Unitarias")
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private AuthService authService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthController authController;

    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        loginResponse = LoginResponse.builder()
                .token("jwt-token")
                .type("Bearer")
                .email("juan@test.com")
                .nombreCompleto("Juan Pérez")
                .rol("USER")
                .build();
    }

    // ===================== REGISTER =====================

    @Test
    @DisplayName("POST /api/auth/register: debe retornar 201 con el token")
    void register_shouldReturn201WithToken() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setNombreCompleto("Juan Pérez");
        req.setEmail("juan@test.com");
        req.setContrasena("password123");
        req.setIdRol(1);

        when(authService.register(any(RegisterRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("juan@test.com"))
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    @DisplayName("POST /api/auth/register: debe llamar al servicio una sola vez")
    void register_shouldCallServiceOnce() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setNombreCompleto("Juan Pérez");
        req.setEmail("juan@test.com");
        req.setContrasena("password123");
        req.setIdRol(1);

        when(authService.register(any(RegisterRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    // ===================== LOGIN =====================

    @Test
    @DisplayName("POST /api/auth/login: debe retornar 200 con el token")
    void login_shouldReturn200WithToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("juan@test.com");
        req.setContrasena("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login: debe retornar 401 con credenciales incorrectas")
    void login_shouldReturn401OnBadCredentials() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("juan@test.com");
        req.setContrasena("wrong");

        doThrow(new BadCredentialsException("Credenciales inválidas"))
                .when(authenticationManager).authenticate(any());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ===================== VERIFY EMAIL =====================

    @Test
    @DisplayName("PATCH /api/auth/verify/{email}: debe retornar 204 al verificar email")
    void verificarEmail_shouldReturn204() throws Exception {
        doNothing().when(authService).verificarEmail("juan@test.com");

        mockMvc.perform(patch("/api/auth/verify/juan@test.com"))
                .andExpect(status().isNoContent());

        verify(authService).verificarEmail("juan@test.com");
    }

    @Test
    @DisplayName("PATCH /api/auth/verify/{email}: debe lanzar excepción si el email ya está verificado")
    void verificarEmail_shouldReturn4xxWhenAlreadyVerified() throws Exception {
        doThrow(new IllegalArgumentException("El email ya está verificado"))
                .when(authService).verificarEmail("juan@test.com");

        mockMvc.perform(patch("/api/auth/verify/juan@test.com"))
                .andExpect(status().is4xxClientError());
    }
}

