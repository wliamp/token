package io.github.wliamp.kit.token.spring

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import io.github.wliamp.kit.token.core.EnvSecretLoader
import io.github.wliamp.kit.token.core.KeySetManager
import io.github.wliamp.kit.token.core.SecretLoader
import io.github.wliamp.kit.token.spring.security.TokenServiceImpl
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

@AutoConfiguration
@EnableConfigurationProperties(TokenProps::class)
class TokenAutoConfig(
    private val props: TokenProps
) {
    @Bean
    fun secretLoader(): SecretLoader = EnvSecretLoader(props.envVar)

    @Bean
    fun jwkSource(keySetManager: KeySetManager): JWKSource<SecurityContext> =
        keySetManager.signingJwkSource()

    @Bean
    fun jwtEncoder(jwkSource: JWKSource<SecurityContext>): JwtEncoder =
        NimbusJwtEncoder(jwkSource)

    @Bean
    fun jwtDecoder(keySetManager: KeySetManager): JwtDecoder =
        NimbusJwtDecoder.withPublicKey(
            keySetManager.currentKeySet().active().toRSAPublicKey()
        ).build()

    @Bean
    fun keySetManager(
        loader: SecretLoader,
        props: TokenProps,
        objectMapper: ObjectMapper
    ): KeySetManager = KeySetManager(
        loader,
        props.reloadIntervalSeconds,
        objectMapper
    )

    @Bean
    @ConditionalOnMissingBean
    fun tokenService(
        jwtEncoder: JwtEncoder,
        jwtDecoder: JwtDecoder
    ): TokenServiceImpl =
        TokenServiceImpl(jwtEncoder, jwtDecoder, props.toSettings())
}
