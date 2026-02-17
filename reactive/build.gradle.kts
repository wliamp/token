dependencies {
    api(project(":core"))
    api(platform("org.springframework.boot:spring-boot-dependencies:4.0.2"))
    api("io.projectreactor:reactor-core")
    api("org.springframework.security:spring-security-oauth2-jose")
}
