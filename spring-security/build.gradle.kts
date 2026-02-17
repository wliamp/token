plugins {
    id("org.jetbrains.kotlin.plugin.spring")
    `java-library`
}

dependencies {
    api(project(":core"))
    api(platform("org.springframework.boot:spring-boot-dependencies:4.0.2"))
    api("org.springframework.security:spring-security-oauth2-jose")
}
