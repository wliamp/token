package io.github.wliamp.tk

import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import java.time.Instant

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
    ): String {
        val now = Instant.now()
        val claimsBuilder = JwtClaimsSet.builder()
            .issuer(settings.issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiresInSeconds))
            .subject(subject)
            .claim("type", type.name)
        settings.defaultClaims.forEach { (k, v) -> claimsBuilder.claim(k, v) }
        extraClaims.forEach { (k, v) -> claimsBuilder.claim(k, v) }
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsBuilder.build())).tokenValue
    }

    override fun getClaims(token: String): Map<String, Any> =
        jwtDecoder.decode(token).claims

    override fun isExpired(token: String): Boolean =
        jwtDecoder.decode(token).expiresAt?.isBefore(Instant.now()) ?: true

    override fun verify(token: String): Boolean =
        try {
            jwtDecoder.decode(token)
            true
        } catch (_: Exception) {
            false
        }

    override fun getType(token: String): Type {
        val claims = getClaims(token)
        return Type.valueOf(claims["type"]?.toString() ?: Type.ACCESS.name)
    }

    override fun tokenInfo(token: String): Claim {
        val jwt = jwtDecoder.decode(token)
        val type = getType(token)
        return Claim(
            subject = jwt.claims["sub"]?.toString() ?: "",
            type = type,
            issuedAt = jwt.issuedAt ?: Instant.EPOCH,
            expiration = jwt.expiresAt ?: Instant.EPOCH,
            claims = jwt.claims
        )
    }

    override fun validateSubject(token: String, expected: String): Boolean =
        getClaims(token)["sub"] == expected

    override fun validateClaim(token: String, key: String, expected: String): Boolean =
        getClaims(token)[key] == expected

    override fun refresh(token: String, expiresInSeconds: Long): String {
        val oldClaims = getClaims(token)
        val subject = oldClaims["sub"]?.toString()
            ?: throw IllegalArgumentException("Missing subject")
        val typeStr = oldClaims["type"]?.toString()
            ?: throw IllegalArgumentException("Missing type")
        val type = Type.valueOf(typeStr)
        val preserved = oldClaims
            .filterKeys { it !in setOf("iat", "exp", "nbf", "sub", "type") }
        return issue(subject, type, expiresInSeconds, preserved)
    }
}
