plugins {
    id("com.github.johnrengelman.shadow") version "8.+"
    id ("com.gorylenko.gradle-git-properties") version "2.4.1"
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.23"
}

group = "org.cubewhy.celestial"
version = "2.3-SNAPSHOT"

println("Java: " + System.getProperty("java.version") + " JVM: " + System.getProperty("java.vm.version") + "(" + System.getProperty("java.vendor") + ") Arch: " + System.getProperty("os.arch"))
println("Celestial Launcher -> https://www.lunarclient.top/")

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}


repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.typesafe.com/typesafe/maven-releases/")
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.11")
    implementation("org.slf4j:slf4j-log4j12:2.0.12")
    implementation("org.apache.logging.log4j:log4j-api:2.23.0")
    implementation("org.apache.logging.log4j:log4j-core:2.22.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.formdev:flatlaf:3.4")
    implementation("commons-io:commons-io:2.15.1")
    implementation("cn.hutool:hutool-crypto:5.8.26")
    implementation("org.java-websocket:Java-WebSocket:1.5.6")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("org.ow2.asm:asm:9.4")
}

tasks.shadowJar {
    archiveClassifier.set("fatjar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    exclude("native-binaries/**")

    exclude("LICENSE.txt")

    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")

    exclude("org/junit/**")
}

tasks.jar {
    dependsOn("shadowJar")

    manifest {
        attributes(
            "Main-Class" to "org.cubewhy.celestial.CelestialKt",
            "Agent-Class" to "org.cubewhy.celestial.AgentKt",
            "Premain-Class" to "org.cubewhy.celestial.AgentKt"
        )
        attributes(
            "Charset" to "UTF-8"
        )
    }
}


kotlin {
    jvmToolchain(17)
}
