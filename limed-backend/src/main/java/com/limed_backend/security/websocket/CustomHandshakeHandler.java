package com.limed_backend.security.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected @NonNull Principal determineUser(@NonNull ServerHttpRequest request,
                                               @NonNull WebSocketHandler wsHandler,
                                               @NonNull Map<String, Object> attributes) {
        Object userIdObj = attributes.get("userId");
        if (userIdObj != null) {
            String userId = userIdObj.toString();
            return () -> userId;
        }
        return Objects.requireNonNull(super.determineUser(request, wsHandler, attributes));
    }
}