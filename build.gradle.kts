plugins {
    kotlin("jvm") version "1.9.21"
    application
}

group = "dev.todaka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.netty:netty-common:4.1.104.Final")
    implementation("io.netty:netty-handler:4.1.104.Final")
    implementation("io.netty:netty-transport:4.1.104.Final")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("dev.todaka.robustredis.MainKt")
}
