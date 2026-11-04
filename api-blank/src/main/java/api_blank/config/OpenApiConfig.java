package api_blank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Documentación OpenAPI: expone el esquema Bearer (JWT) para que Swagger
 * permita probar los endpoints protegidos con un token.
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI apiBlankOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Blank - Favoritos y Listas")
                        .description("""
                                API de favoritos y listas de productos (TP3: seguridad con JWT).

                                - **Públicos:** `/api/auth/**`, `/api/productos/**`, `/health`
                                - **Requieren token:** `/api/favoritos/**`, `/api/listas/**`

                                Para probar los endpoints protegidos: iniciá sesión en
                                `POST /api/auth/login` y pegá el token devuelto en el campo
                                *Authorize* de Swagger (sin el prefijo `Bearer `).""")
                        .version("3.0.0"))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT obtenido en POST /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
