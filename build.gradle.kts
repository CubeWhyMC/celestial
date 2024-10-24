plugins {
    id("com.github.johnrengelman.shadow") version "8.+"
    id ("com.gorylenko.gradle-git-properties") version "2.4.1"
    kotlin("jvm")
    id("com.google.protobuf") version "0.9.4"
    kotlin("plugin.serialization") version "2.0.21"
}

group = "org.cubewhy.celestial"
version = "2.7.3-SNAPSHOT"

println("Java: " + System.getProperty("java.version") + " JVM: " + System.getProperty("java.vm.version") + "(" + System.getProperty("java.vendor") + ") Arch: " + System.getProperty("os.arch"))
println("Celestial Launcher -> https://lunarclient.top/")

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}


repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.typesafe.com/typesafe/maven-releases/")
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-log4j12:2.0.16")
    implementation("org.apache.logging.log4j:log4j-api:2.24.1")
    implementation("org.apache.logging.log4j:log4j-core:2.24.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.formdev:flatlaf:3.5.2")
    implementation("commons-io:commons-io:2.17.0")
    implementation("cn.hutool:hutool-crypto:5.8.32")
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
    implementation("com.google.protobuf:protobuf-kotlin:4.28.3")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation("org.ow2.asm:asm:9.7.1")
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

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.28.2"
    }
}

tasks.jar {
    dependsOn("shadowJar")

    manifest {
        attributes(
            "Main-Class" to "org.cubewhy.celestial.CelestialKt",
        )
        attributes(
            "Charset" to "UTF-8"
        )
    }
}


kotlin {
    jvmToolchain(17)
}
