import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("signing")
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.4"))
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.nimbusds:nimbus-jose-jwt:10.0.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        named<MavenPublication>("mavenJava") {
            artifactId = "token"
            pom {
                name.set("Token Core")
                description.set("Core token model and settings.")
            }
        }
    }
}
