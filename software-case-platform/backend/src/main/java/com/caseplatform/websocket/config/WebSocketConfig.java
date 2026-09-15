package com.caseplatform.websocket.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración central del broker de mensajería WebSocket con soporte STOMP y SockJS.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket estándar
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        // Endpoint WebSocket con soporte fallback SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Habilita broker en memoria para tópicos públicos y colas privadas
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefijo para mensajes dirigidos a controladores @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");

        // Prefijo para mensajes punto a punto de usuario
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Aplica interceptor de autenticación JWT
        registration.interceptors(webSocketAuthInterceptor);
    }
}
