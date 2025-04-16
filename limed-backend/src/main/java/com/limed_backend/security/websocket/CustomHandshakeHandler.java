package com.limed_backend.security.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        Object userIdObj = attributes.get("userId");
        if (userIdObj != null) {
            String userId = userIdObj.toString();
            return () -> userId;
        }
        return super.determineUser(request, wsHandler, attributes);
    }
}
