package io.github.wliamp.tk

data class TokenSettings(
    val issuer: String = "",
    val expireSeconds: Long = 3600,
    val defaultClaims: Map<String, Any> = emptyMap()
)
