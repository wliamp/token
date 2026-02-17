package io.github.wliamp.kit.token.core

data class TokenSettings(
    val issuer: String = "",
    val expireSeconds: Long = 3600,
    val defaultClaims: Map<String, Any> = emptyMap()
)
