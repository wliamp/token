package io.github.wliamp.kit.token.reactive

import io.github.wliamp.kit.token.core.Claim
import io.github.wliamp.kit.token.core.TokenService
import io.github.wliamp.kit.token.core.Type
import io.github.wliamp.kit.token.core.Type.*
import java.time.Instant.*

class ReactiveTokenService(
    private val tokenUtil: TokenUtil
) : TokenService {
    override fun issue(
        subject: String,
        type: Type,
        expiresInSeconds: Long,
        extraClaims: Map<String, Any>
    ): String =
        tokenUtil.issue(subject, type, expiresInSeconds, extraClaims).block()
            ?: error("TokenUtil.issue returned null")

    override fun getClaims(token: String): Map<String, Any> =
        tokenUtil.getClaims(token).block() ?: emptyMap()

    override fun isExpired(token: String): Boolean =
        tokenUtil.isExpired(token).block() ?: true

    override fun verify(token: String): Boolean =
        tokenUtil.verify(token).block() ?: false

    override fun getType(token: String): Type =
        tokenUtil.getType(token).block() ?: ACCESS

    override fun tokenInfo(token: String): Claim =
        tokenUtil.tokenInfo(token).block()
            ?: Claim("", ACCESS, EPOCH, EPOCH, emptyMap())

    override fun validateSubject(token: String, expected: String): Boolean =
        tokenUtil.validateSubject(token, expected).block() ?: false

    override fun validateClaim(token: String, key: String, expected: String): Boolean =
        tokenUtil.validateClaim(token, key, expected).block() ?: false

    override fun refresh(token: String, expiresInSeconds: Long): String =
        tokenUtil.refresh(token, expiresInSeconds).block()
            ?: error("TokenUtil.refresh returned null")
}
