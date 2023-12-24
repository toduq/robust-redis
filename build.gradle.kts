plugins {
    kotlin("jvm") version "1.9.21"
    application
}

group = "dev.todaka"
version = "1.0-SNAPSHOT;we"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}
