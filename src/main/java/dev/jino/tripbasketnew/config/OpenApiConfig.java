package dev.jino.tripbasketnew.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Trip Basket API",
        version = "v1",
        description = "Trip Basket 백엔드 API 문서"
    )
)
@SecurityScheme(
    name = "accessTokenCookie",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.COOKIE,
    paramName = "access_token",
    description = "JWT access token cookie"
)
public class OpenApiConfig {

}
