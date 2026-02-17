package io.github.wliamp.kit.token.core

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWK.*
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import java.lang.System.*
import java.util.concurrent.Executors.*
import java.util.concurrent.TimeUnit.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.map

fun interface SecretLoader {
    fun loadPrivateJwksJson(): String
}

class EnvSecretLoader(private val envVar: String) : SecretLoader {
    override fun loadPrivateJwksJson(): String =
        getenv(envVar)
            ?: getProperty(envVar)
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
    private val scheduler = newSingleThreadScheduledExecutor {
        Thread(it, "token-reloader").apply { isDaemon = true }
    }

    init {
        reload()
        reloadIntervalSeconds
            .takeIf { it > 0 }
            ?.let {
                scheduler.scheduleAtFixedRate(
                    { reload() },
                    it,
                    it,
                    SECONDS
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
        val privateKeys = keysNode.map {
            val map: Map<String, Any> =
                objectMapper.convertValue(it, object : TypeReference<Map<String, Any>>() {})
            parse(map) as? RSAKey ?: error("Key is not RSA")
        }
        cache.set(PrivateKeySet(currentKid, graceKids, privateKeys))
    }

    fun currentKeySet(): PrivateKeySet =
        cache.get() ?: error("Keys not loaded")

    fun signingJwkSource(): JWKSource<SecurityContext> = JWKSource { selector, _ ->
        val ks = currentKeySet()
        selector.select(JWKSet(ks.privateKeys)).ifEmpty { listOf(ks.active()) }
    }
}
