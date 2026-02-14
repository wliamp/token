package io.github.wliamp.tk

interface TokenService {
    fun issue(
        subject: String,
        type: Type = Type.ACCESS,
        expiresInSeconds: Long,
        extraClaims: Map<String, Any> = emptyMap()
    ): String

    fun getClaims(token: String): Map<String, Any>

    fun isExpired(token: String): Boolean

    fun verify(token: String): Boolean

    fun getType(token: String): Type

    fun tokenInfo(token: String): Claim

    fun validateSubject(token: String, expected: String): Boolean

    fun validateClaim(token: String, key: String, expected: String): Boolean

    fun refresh(token: String, expiresInSeconds: Long): String
}
