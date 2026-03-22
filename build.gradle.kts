plugins {
    id("java")
    id("war")
}


group = "org.nk"
version = "v1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.mysql:mysql-connector-j:9.6.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.1")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")

}

tasks.test {
    useJUnitPlatform()
}