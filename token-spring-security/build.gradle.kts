import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("signing")
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.4"))
    api(project(":token"))
    api("org.springframework.security:spring-security-oauth2-jose")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        named<MavenPublication>("mavenJava") {
            artifactId = "token-spring-security"
            pom {
                name.set("Token Spring Security")
                description.set("Spring Security imperative adapter for token core.")
            }
        }
    }
}
