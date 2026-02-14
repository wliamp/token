import java.lang.System.getenv

rootProject.name = "token"

getenv("MODULE")?.takeIf { it.isNotBlank() }?.let {
    include(it)
} ?: include(
    "core",
    "reactive",
    "spring-security",
    "reactive-spring-boot-starter",
    "imperative-spring-boot-starter"
)
