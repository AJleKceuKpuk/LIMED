package com.limed_backend.security.wesocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class HttpHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        // Извлекаем строку параметров из URI, например: "userId=1&foo=bar"
        String query = request.getURI().getQuery();

        if (query != null && query.contains("userId=")) {
            // На случай нескольких параметров разделенных & перебираем их
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("userId=")) {
                    String userId = param.substring("userId=".length());
                    // Сохраняем userId в атрибутах, чтобы позже его можно было использовать
                    attributes.put("userId", userId);
                    break;
                }
            }
        }
        // Продолжаем рукопожатие
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Здесь можно добавить действия после рукопожатия, если необходимо.
    }
}