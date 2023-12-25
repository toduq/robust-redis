plugins {
    kotlin("jvm") version "1.9.21"
}

group = "dev.todaka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // netty
    implementation("io.netty:netty-common:4.1.104.Final")
    implementation("io.netty:netty-handler:4.1.104.Final")
    implementation("io.netty:netty-transport:4.1.104.Final")

    // test
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1") {
        exclude(module = "hamcrest-core")
    }
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.0")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
    testImplementation("io.mockk:mockk:1.13.8")

    // test containers
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
