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
tasks.register<Copy>("deploy") {
    dependsOn(tasks.named("war"))

    val tomcatWebapps = "/Users/naveen-nts0470/devhub/bookyourshow/server/webapps"

    val warTask = tasks.named<War>("war")

    doFirst {
        val warFile = warTask.get().archiveFile.get().asFile
        val warName = warFile.name
        val explodedDir = warName.removeSuffix(".war")

        delete("$tomcatWebapps/$warName")
        delete("$tomcatWebapps/$explodedDir")

        println("Old deployment removed: $warName")
    }

    from(warTask.map { it.archiveFile })
    into(tomcatWebapps)

    doLast {
        println("New WAR deployed successfully ")
    }
}
/*

tasks.test {
    useJUnitPlatform()
}
*/

tasks.named<War>("war") {
    archiveFileName.set("api.war")
}

