import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("signing")
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.4"))
    api(project(":token"))
    api("io.projectreactor:reactor-core")
    api("org.springframework.security:spring-security-oauth2-jose")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        named<MavenPublication>("mavenJava") {
            artifactId = "token-reactive"
            pom {
                name.set("Token Reactive")
                description.set("Reactive adapters for token core.")
            }
        }
    }
}
