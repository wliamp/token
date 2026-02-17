plugins {
    id("org.jetbrains.kotlin.plugin.spring")
    `java-library`
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.2"))
    api(project(":core"))
    api(project(":spring-security"))
    implementation("org.springframework.boot:spring-boot-autoconfigure")
}
