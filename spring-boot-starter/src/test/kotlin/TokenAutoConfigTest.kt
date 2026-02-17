package io.github.wliamp.kit.token.spring

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.RSAKey.*
import io.github.wliamp.kit.token.core.KeySetManager
import io.github.wliamp.kit.token.core.SecretLoader
import io.github.wliamp.kit.token.core.TokenService
import io.github.wliamp.kit.token.spring.RSAKeyGenerator.generate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.lang.System.*
import java.security.KeyPairGenerator.*
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import kotlin.test.assertNotNull

class TokenAutoConfigTest {
    private fun baseContextRunner() = ApplicationContextRunner()
        .withUserConfiguration(TokenAutoConfig::class.java)
        .withInitializer {
            it.beanFactory.registerSingleton("objectMapper", ObjectMapper())
        }

    @BeforeEach
    fun clearEnv() {
        clearProperty("STARTER_TOKEN_PRIVATE_JWKS_JSON")
        clearProperty("MY_ENV")
    }

    @Test
    fun `should create beans with default env var`() {
        setProperty(
            "STARTER_TOKEN_PRIVATE_JWKS_JSON",
            """
            {
              "currentKid": "k1",
              "graceKids": [],
              "keys": [${generate()}]
            }
            """.trimIndent()
        )

        baseContextRunner()
            .withPropertyValues(
                "token.backend=ENV",
                "token.env-var=STARTER_TOKEN_PRIVATE_JWKS_JSON"
            )
            .run {
                assertNotNull(it.getBean<TokenProps>())
                assertNotNull(it.getBean<SecretLoader>())
                assertNotNull(it.getBean<KeySetManager>())
                assertNotNull(it.getBean<JwtEncoder>())
                assertNotNull(it.getBean<JwtDecoder>())
                assertNotNull(it.getBean<TokenService>())
            }
    }

    @Test
    fun `should create beans with custom env var`() {
        setProperty(
            "MY_ENV",
            """
            {
              "currentKid": "k1",
              "graceKids": [],
              "keys": [${generate()}]
            }
            """.trimIndent()
        )

        baseContextRunner()
            .withPropertyValues(
                "token.backend=ENV",
                "token.env-var=MY_ENV",
                "token.issuer=http://custom-issuer"
            )
            .run {
                assertNotNull(it.getBean<TokenProps>())
                assertNotNull(it.getBean<SecretLoader>())
                assertNotNull(it.getBean<KeySetManager>())
                assertNotNull(it.getBean<JwtEncoder>())
                assertNotNull(it.getBean<JwtDecoder>())
                assertNotNull(it.getBean<TokenService>())
            }
    }
}

object RSAKeyGenerator {
    fun generate(): String {
        val gen = getInstance("RSA")
        gen.initialize(2048)
        val keyPair = gen.generateKeyPair()
        val rsaKey = Builder(keyPair.public as RSAPublicKey)
            .privateKey(keyPair.private as RSAPrivateKey)
            .keyID("k1")
            .build()
        return rsaKey.toJSONString()
    }
}
