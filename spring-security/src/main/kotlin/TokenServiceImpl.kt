package io.github.wliamp.kit.token.spring.security

import io.github.wliamp.kit.token.core.Claim
import io.github.wliamp.kit.token.core.TokenService
import io.github.wliamp.kit.token.core.TokenSettings
import io.github.wliamp.kit.token.core.Type
import io.github.wliamp.kit.token.core.Type.*
import org.springframework.security.oauth2.jwt.JwtClaimsSet.*
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters.from
import java.time.Instant.*

class TokenServiceImpl(
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder,
    private val settings: TokenSettings
) : TokenService {
    override fun issue(
        subject: String,
        type: Type,
        expiresInSeconds: Long,
        extraClaims: Map<String, Any>
    ): String = run {
        val now = now()
        val claimsBuilder = builder()
            .issuer(settings.issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiresInSeconds))
            .subject(subject)
            .claim("type", type.name)
        settings.defaultClaims.forEach { (k, v) -> claimsBuilder.claim(k, v) }
        extraClaims.forEach { (k, v) -> claimsBuilder.claim(k, v) }
        jwtEncoder.encode(from(claimsBuilder.build())).tokenValue
    }

    override fun getClaims(token: String): Map<String, Any> =
        jwtDecoder.decode(token).claims

    override fun isExpired(token: String): Boolean =
        jwtDecoder.decode(token).expiresAt?.isBefore(now()) ?: true

    override fun verify(token: String): Boolean =
        try {
            jwtDecoder.decode(token)
            true
        } catch (_: Exception) {
            false
        }

    override fun getType(token: String): Type =
        valueOf(getClaims(token)["type"]?.toString() ?: ACCESS.name)

    override fun tokenInfo(token: String): Claim =
        jwtDecoder.decode(token).let {
            Claim(
                it.claims["sub"]?.toString() ?: "",
                getType(token),
                it.issuedAt ?: EPOCH,
                it.expiresAt ?: EPOCH,
                it.claims
            )
        }

    override fun validateSubject(token: String, expected: String): Boolean =
        getClaims(token)["sub"] == expected

    override fun validateClaim(token: String, key: String, expected: String): Boolean =
        getClaims(token)[key] == expected

    override fun refresh(token: String, expiresInSeconds: Long): String =
        run {
            val oldClaims = getClaims(token)
            val subject = oldClaims["sub"]?.toString()
                ?: throw IllegalArgumentException("Missing subject")
            val typeStr = oldClaims["type"]?.toString()
                ?: throw IllegalArgumentException("Missing type")
            val type = valueOf(typeStr)
            val preserved = oldClaims
                .filterKeys { it !in setOf("iat", "exp", "nbf", "sub", "type") }
            issue(subject, type, expiresInSeconds, preserved)
        }
}