package io.github.wliamp.kit.token.reactive

import io.github.wliamp.kit.token.core.Claim
import io.github.wliamp.kit.token.core.TokenSettings
import io.github.wliamp.kit.token.core.Type
import io.github.wliamp.kit.token.core.Type.*
import org.springframework.security.oauth2.jwt.JwtClaimsSet.*
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters.from
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.*
import reactor.core.scheduler.Schedulers.*
import java.time.Instant.*

class TokenUtil(
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: ReactiveJwtDecoder,
    private val settings: TokenSettings,
) {
    @JvmOverloads
    fun issue(
        subject: String,
        type: Type = ACCESS,
        expiresInSeconds: Long = settings.expireSeconds,
        extraClaims: Map<String, Any> = emptyMap()
    ): Mono<String> =
        fromCallable {
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
        }.subscribeOn(boundedElastic())

    /** Decode token */
    fun getClaims(token: String): Mono<Map<String, Any>> =
        jwtDecoder.decode(token).map { it.claims }

    /** Check if token expired */
    fun isExpired(token: String): Mono<Boolean> =
        jwtDecoder.decode(token)
            .map { it.expiresAt?.isBefore(now()) ?: true }

    /** Verify token */
    fun verify(token: String): Mono<Boolean> =
        jwtDecoder.decode(token)
            .map { true }
            .onErrorReturn(false)

    /** Get token type */
    fun getType(token: String): Mono<Type> =
        getClaims(token).map {
            valueOf(it["type"]?.toString() ?: ACCESS.name)
        }

    /** Retrieve summarized information about the token */
    fun tokenInfo(token: String): Mono<Claim> =
        jwtDecoder.decode(token).flatMap { jwt ->
            getType(token).map {
                Claim(
                    jwt.claims["sub"]?.toString() ?: "",
                    it,
                    jwt.issuedAt ?: EPOCH,
                    jwt.expiresAt ?: EPOCH,
                    jwt.claims
                )
            }
        }

    /** Validate that the token subject matches the expected value */
    fun validateSubject(token: String, expected: String): Mono<Boolean> =
        getClaims(token).map { it["sub"] == expected }

    /** Validate a specific claim in the token */
    fun validateClaim(token: String, key: String, expected: String): Mono<Boolean> =
        getClaims(token).map { it[key] == expected }

    /** Refresh the token: retain existing claims and type, generate new iat/exp */
    fun refresh(token: String, expiresInSeconds: Long): Mono<String> =
        getClaims(token).flatMap {
            val subject = it["sub"]?.toString()
                ?: return@flatMap error<String>(IllegalArgumentException("Missing subject"))
            val typeStr = it["type"]?.toString()
                ?: return@flatMap error<String>(IllegalArgumentException("Missing type"))
            val type = valueOf(typeStr)
            val preserved = it.filterKeys { it !in setOf("iat", "exp", "nbf", "sub", "type") }
            issue(subject, type, expiresInSeconds, preserved)
        }
}

class TokenUtilWrapper(private val tokenUtil: TokenUtil) {
    fun issue(subject: String): Mono<String> {
        return tokenUtil.issue(subject)
    }

    fun issue(subject: String, type: Type): Mono<String> {
        return tokenUtil.issue(subject, type)
    }

    fun issue(subject: String, type: Type, exp: Long): Mono<String> {
        return tokenUtil.issue(subject, type, exp)
    }

    fun issue(subject: String, type: Type, exp: Long, extraClaims: Map<String, Any>): Mono<String> {
        return tokenUtil.issue(subject, type, exp, extraClaims)
    }
}
