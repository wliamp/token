package io.github.wliamp.kit.token.core

import java.time.Instant

data class Claim(
    val subject: String,
    val type: Type,
    val issuedAt: Instant,
    val expiration: Instant,
    val claims: Map<String, Any>
)

enum class Type {
    ACCESS,
    REFRESH,
    SERVICE
}
