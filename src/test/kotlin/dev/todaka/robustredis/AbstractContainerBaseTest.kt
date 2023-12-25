package dev.todaka.robustredis

import dev.todaka.robustredis.connection.RedisURI
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
abstract class AbstractContainerBaseTest {
    fun redisUri() = RedisURI(
        redisContainer.host,
        redisContainer.getMappedPort(6379)
    )

    companion object {
        /**
         * Redis container is shared among tests in a test class.
         * https://java.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers
         */
        @Container
        @JvmStatic
        private val redisContainer: RedisContainer = RedisContainer(
            DockerImageName.parse("redis:6.2.7-alpine")
        ).withExposedPorts(6379)
    }
}

class RedisContainer(
    dockerImageName: DockerImageName,
) : GenericContainer<RedisContainer>(dockerImageName)
