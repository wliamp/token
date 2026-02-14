package io.github.wliamp.tk

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.map

fun interface SecretLoader {
    fun loadPrivateJwksJson(): String
}

class EnvSecretLoader(private val envVar: String) : SecretLoader {
    override fun loadPrivateJwksJson(): String =
        System.getenv(envVar)
            ?: System.getProperty(envVar)
            ?: error("Env var $envVar not found")
}

data class PrivateKeySet(
    val currentKid: String,
    val graceKids: Set<String>,
    val privateKeys: List<RSAKey>
) {
    fun active(): RSAKey = privateKeys.first { it.keyID == currentKid }
    fun verificationPublicKeys(): List<RSAKey> =
        privateKeys.filter { it.keyID == currentKid || it.keyID in graceKids }
            .map { it.toPublicJWK().toRSAKey() }
}

class KeySetManager(
    private val loader: SecretLoader,
    private val reloadIntervalSeconds: Long,
    private val objectMapper: ObjectMapper
) {
    private val cache = AtomicReference<PrivateKeySet?>(null)
    private val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "token-reloader").apply { isDaemon = true }
    }

    init {
        reload()
        if (reloadIntervalSeconds > 0) {
            scheduler.scheduleAtFixedRate(
                { reload() },
                reloadIntervalSeconds,
                reloadIntervalSeconds,
                TimeUnit.SECONDS
            )
        }
    }

    fun reload() {
        val json = loader.loadPrivateJwksJson()
        val root = objectMapper.readTree(json)
        val currentKid = root["currentKid"]?.asText()
            ?: error("Missing currentKid")
        val graceKids = root["graceKids"]?.map { it.asText() }?.toSet() ?: emptySet()
        val keysNode = root["keys"] ?: error("Missing keys")
        val privateKeys = keysNode.map { k ->
            val map: Map<String, Any> =
                objectMapper.convertValue(k, object : TypeReference<Map<String, Any>>() {})
            val jwk = JWK.parse(map)
            jwk as? RSAKey ?: error("Key is not RSA")
        }
        cache.set(PrivateKeySet(currentKid, graceKids, privateKeys))
    }

    fun currentKeySet(): PrivateKeySet =
        cache.get() ?: error("Keys not loaded")

    fun signingJwkSource(): JWKSource<SecurityContext> = JWKSource { selector, _ ->
        val ks = currentKeySet()
        val jwkSet = JWKSet(ks.privateKeys)
        val matched = selector.select(jwkSet)
        if (matched.isEmpty()) listOf(ks.active()) else matched
    }
}
