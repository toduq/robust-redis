package dev.todaka.robustredis

import dev.todaka.robustredis.connection.RedisURI
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
abstract class AbstractContainerBaseTest {
    fun standaloneRedisUri() = RedisURI(
        redisContainer.host,
        redisContainer.getMappedPort(6379)
    )

    fun clusterRedisUri() = RedisURI(
        redisClusterContainer.host,
        redisClusterContainer.getMappedPort(7000)
    )

    companion object {
        /**
         * Redis container is shared among tests in a test class.
         * https://java.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers
         */
        @Container
        @JvmStatic
        protected val redisContainer: RedisContainer = RedisContainer(
            DockerImageName.parse("redis:6.2.7-alpine")
        ).withExposedPorts(6379)

        @Container
        @JvmStatic
        protected val redisClusterContainer: RedisContainer = RedisContainer(
            DockerImageName.parse("grokzen/redis-cluster:6.2.7")
        ).withEnv("IP", "0.0.0.0").withExposedPorts(7000, 7001, 7002, 7003, 7004, 7005)
    }
}

class RedisContainer(
    dockerImageName: DockerImageName,
) : GenericContainer<RedisContainer>(dockerImageName)
