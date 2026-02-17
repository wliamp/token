package io.github.wliamp.kit.token.spring.reactive

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import io.github.wliamp.kit.token.core.EnvSecretLoader
import io.github.wliamp.kit.token.core.KeySetManager
import io.github.wliamp.kit.token.core.SecretLoader
import io.github.wliamp.kit.token.reactive.TokenUtil
import io.github.wliamp.kit.token.reactive.TokenUtilWrapper
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder

@AutoConfiguration
@EnableConfigurationProperties(ReactiveTokenProps::class)
class ReactiveTokenAutoConfig(
    private val props: ReactiveTokenProps
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
        props: ReactiveTokenProps,
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
