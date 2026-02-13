package io.github.wliamp.tk

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder

@AutoConfiguration
@EnableConfigurationProperties(Properties::class)
class AutoConfig(
    private val props: Properties
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
    fun reactiveJwtDecoder(keySetManager: KeySetManager): ReactiveJwtDecoder =
        NimbusReactiveJwtDecoder.withPublicKey(
            keySetManager.currentKeySet().active().toRSAPublicKey()
        ).build()

    @Bean
    fun keySetManager(
        loader: SecretLoader,
        props: Properties,
        objectMapper: ObjectMapper
    ): KeySetManager = KeySetManager(
        loader,
        props.reloadIntervalSeconds,
        objectMapper
    )

    @Bean
    @ConditionalOnMissingBean
    fun tokenUtil(
        jwtEncoder: JwtEncoder, jwtDecoder: ReactiveJwtDecoder
    ): TokenUtil = TokenUtil(
        jwtEncoder, jwtDecoder, props.toSettings()
    )

    @Bean
    @ConditionalOnMissingBean
    fun tokenUtilWrapper(tokenUtil: TokenUtil): TokenUtilWrapper =
        TokenUtilWrapper(tokenUtil)
}
