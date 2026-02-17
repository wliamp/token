package io.github.wliamp.kit.token.spring

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.RSAKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.security.KeyPairGenerator
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
        System.clearProperty("STARTER_TOKEN_PRIVATE_JWKS_JSON")
        System.clearProperty("MY_ENV")
    }

    @Test
    fun `should create beans with default env var`() {
        System.setProperty(
            "STARTER_TOKEN_PRIVATE_JWKS_JSON",
            """
            {
              "currentKid": "k1",
              "graceKids": [],
              "keys": [${RSAKeyGenerator.generate()}]
            }
            """.trimIndent()
        )

        baseContextRunner()
            .withPropertyValues(
                "token.backend=ENV",
                "token.env-var=STARTER_TOKEN_PRIVATE_JWKS_JSON"
            )
            .run {
                assertNotNull(it.getBean(TokenProps::class.java))
                assertNotNull(it.getBean(SecretLoader::class.java))
                assertNotNull(it.getBean(KeySetManager::class.java))
                assertNotNull(it.getBean(JwtEncoder::class.java))
                assertNotNull(it.getBean(JwtDecoder::class.java))
                assertNotNull(it.getBean(TokenService::class.java))
            }
    }

    @Test
    fun `should create beans with custom env var`() {
        System.setProperty(
            "MY_ENV",
            """
            {
              "currentKid": "k1",
              "graceKids": [],
              "keys": [${RSAKeyGenerator.generate()}]
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
                assertNotNull(it.getBean(TokenProps::class.java))
                assertNotNull(it.getBean(SecretLoader::class.java))
                assertNotNull(it.getBean(KeySetManager::class.java))
                assertNotNull(it.getBean(JwtEncoder::class.java))
                assertNotNull(it.getBean(JwtDecoder::class.java))
                assertNotNull(it.getBean(TokenService::class.java))
            }
    }
}

object RSAKeyGenerator {
    fun generate(): String {
        val gen = KeyPairGenerator.getInstance("RSA")
        gen.initialize(2048)
        val keyPair = gen.generateKeyPair()

        val rsaKey = RSAKey.Builder(keyPair.public as RSAPublicKey)
            .privateKey(keyPair.private as RSAPrivateKey)
            .keyID("k1")
            .build()

        return rsaKey.toJSONString()
    }
}
