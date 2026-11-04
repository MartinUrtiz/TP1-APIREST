package api_blank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import api_blank.auth.JwtAuthenticationFilter;
import api_blank.handler.ErrorResponse;

// Spring Boot 4 autoconfigura Jackson 3 (tools.jackson): por eso el
// ObjectMapper de acá es de ese paquete y no de com.fasterxml (Jackson 2,
// que sólo está en el classpath por jjwt-jackson).
import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuración de seguridad de la aplicación.
 *
 * - Sin sesión (stateless): la identidad vive sólo en el JWT de cada request.
 * - Públicos: /api/auth/**, /api/productos/**, /health y la documentación.
 * - Todo lo demás (/api/favoritos/**, /api/listas/**) exige token válido.
 * - El filtro JWT se agrega ANTES de UsernamePasswordAuthenticationFilter
 *   (se instancia con `new` para que Spring no lo registre dos veces).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Públicos
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        // Documentación (Swagger)
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Errores estándar de Spring MVC
                        .requestMatchers("/error").permitAll()
                        // Todo lo demás requiere token válido
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this::responder401)
                        .accessDeniedHandler(this::responder403))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 401 Unauthorized: sin token o token inválido/vencido. */
    private void responder401(jakarta.servlet.http.HttpServletRequest request,
                              HttpServletResponse response,
                              org.springframework.security.core.AuthenticationException authException)
            throws java.io.IOException {
        responderError(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Se requiere un token JWT válido (header Authorization: Bearer <token>)");
    }

    /** 403 Forbidden: token válido pero sin permiso (no es dueño y no es ADMIN). */
    private void responder403(jakarta.servlet.http.HttpServletRequest request,
                              HttpServletResponse response,
                              org.springframework.security.access.AccessDeniedException accessDeniedException)
            throws java.io.IOException {
        responderError(response, HttpServletResponse.SC_FORBIDDEN,
                "No tenés permiso para acceder a este recurso");
    }

    private void responderError(HttpServletResponse response, int status, String mensaje)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(status, mensaje));
    }

    /** Codificador de contraseñas: BCrypt con sal incorporada. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
