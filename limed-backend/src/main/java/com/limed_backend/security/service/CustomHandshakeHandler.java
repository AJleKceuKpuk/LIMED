package com.limed_backend.security.service;

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
        // Если в атрибутах (помещённых нашим интерцептором) присутствует userId, используем его
        Object userIdObj = attributes.get("userId");
        if (userIdObj != null) {
            String userId = userIdObj.toString();
            // Возвращаем Principal, где getName() == userId. Это потом будет доступно через headerAccessor.getUser()
            return () -> userId;
        }
        return super.determineUser(request, wsHandler, attributes);
    }
}
