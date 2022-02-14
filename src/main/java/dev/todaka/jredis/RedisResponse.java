package dev.todaka.jredis;

import lombok.EqualsAndHashCode;
import lombok.Value;

public abstract class RedisResponse {
    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class StringResponse extends RedisResponse {
        String body;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class ErrorResponse extends RedisResponse {
        String body;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class LongResponse extends RedisResponse {
        long body;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class NullResponse extends RedisResponse {
    }
}
