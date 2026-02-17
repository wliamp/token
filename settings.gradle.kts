import java.lang.System.getenv

rootProject.name = "token"

val moduleDeps = mapOf(
    "core" to emptyList(),
    "spring-security" to listOf("core"),
    "reactive" to listOf("core"),
    "spring-boot-starter" to listOf("core", "spring-security"),
    "spring-boot-starter-reactive" to listOf("core", "reactive"),
)

val included = mutableSetOf<String>()

fun includeRecursive(module: String) =
    module
        .takeIf { included.add(it) }
        ?.also { include(it) }
        ?.let { moduleDeps[it].orEmpty().forEach(::includeRecursive) }

getenv("MODULE")?.takeIf { it.isNotBlank() }?.let {
    includeRecursive(it)
} ?: include(
    "core",
    "spring-security",
    "reactive",
    "spring-boot-starter",
    "spring-boot-starter-reactive",
)
