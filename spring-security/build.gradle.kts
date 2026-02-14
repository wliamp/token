plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("signing")
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.4"))
    api(project(":core"))
    api("org.springframework.security:spring-security-oauth2-jose")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
