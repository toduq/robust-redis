package dev.todaka.jredis;

import lombok.Value;

public abstract class RedisResponse {
    @Value
    static class StringResponse extends RedisResponse {
        String body;
    }

    @Value
    static class ErrorResponse extends RedisResponse {
        String body;
    }

    @Value
    static class LongResponse extends RedisResponse {
        long body;
    }

    @Value
    static class NullResponse extends RedisResponse {
    }

}
