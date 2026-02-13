import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("maven-publish")
    id("signing")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.4"))
    api(project(":token"))
    api(project(":token-reactive"))
    implementation("org.springframework.boot:spring-boot-autoconfigure")
}

publishing {
    publications {
        named<MavenPublication>("mavenJava") {
            artifactId = "spring-boot-starter-reactive-token"
            pom {
                name.set("Spring Boot Starter Reactive Token")
                description.set("Spring Boot auto-configuration for token utilities.")
            }
        }
    }
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
