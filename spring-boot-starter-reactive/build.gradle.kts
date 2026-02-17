plugins {
    id("org.jetbrains.kotlin.plugin.spring")
    `java-library`
}

dependencies {
    api(project(":core"))
    api(project(":reactive"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.2"))
    implementation("org.springframework.boot:spring-boot-autoconfigure")
}
