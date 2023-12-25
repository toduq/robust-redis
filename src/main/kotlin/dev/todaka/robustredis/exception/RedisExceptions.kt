package dev.todaka.robustredis.exception

/**
 * 初期化時の例外
 */
open class RedisInitializationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class RedisInvalidClusterException(message: String, cause: Throwable? = null) :
    RedisInitializationException(message, cause)

/**
 * Network timeoutなどの仕方がない例外
 */
open class RedisNetworkException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class RedisAlreadyClosedException(message: String, cause: Throwable? = null) : RedisNetworkException(message, cause)

class RedisConnectionClosedException(message: String, cause: Throwable? = null) : RedisNetworkException(message, cause)

/**
 * Protocol error系の致命的な例外
 *
 * 発生した時点でおかしいので、このsub-classのexceptionが発生した時、NodeConnectionは切断される
 */
open class RedisFatalException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class RedisProtocolException(message: String, cause: Throwable? = null) : RedisFatalException(message, cause)

/**
 * Commandの実行に失敗した場合の例外
 */
open class RedisCommandException(message: String) : RuntimeException(message)
