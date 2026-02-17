plugins {
    id("org.jetbrains.kotlin.plugin.spring")
    `java-library`
}

dependencies {
    api(project(":core"))
    api(project(":reactive"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.2"))
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    testImplementation("org.springframework.boot:spring-boot-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
    testImplementation("org.assertj:assertj-core")
}
