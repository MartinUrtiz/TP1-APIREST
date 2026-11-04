package api_blank.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api_blank.usuario.UsuarioResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro y login (público, sin token)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrarse",
            description = "Crea un usuario con contraseña hasheada (BCrypt) y rol USER. "
                    + "Devuelve 409 si el email ya está registrado.")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody AuthRequestDTO request) {
        UsuarioResponseDTO creado = authService.registrar(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Valida email y contraseña y devuelve un JWT firmado "
                    + "(con el id y el rol del usuario adentro). Devuelve 401 si las credenciales son inválidas.")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }
}
