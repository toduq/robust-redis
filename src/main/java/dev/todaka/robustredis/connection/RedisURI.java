package dev.todaka.robustredis.connection;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class RedisURI {
    String host;
    int port;
    String password;

    public RedisURI(String host, int port) {
        this(host, port, null);
    }
}
