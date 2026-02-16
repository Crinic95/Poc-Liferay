package it.dedagroup.microservice.proxy;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.Set;

@Configuration
public class JwtDecoderConfig {

    @Bean
    JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri
    ) {
        var typVerifier = new DefaultJOSEObjectTypeVerifier<SecurityContext>(
                Set.of(
                        JOSEObjectType.JWT,
                        new JOSEObjectType("at+jwt")
                )
        );

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwtProcessorCustomizer(processor -> {
                    ((DefaultJWTProcessor<SecurityContext>) processor).setJWSTypeVerifier(typVerifier);
                })
                .build();

        decoder.setJwtValidator(new JwtTimestampValidator());

        return decoder;
    }
}