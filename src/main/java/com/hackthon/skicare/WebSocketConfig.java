
package com.hackthon.skicare;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
// Active le traitement des messages STOMP à travers les WebSockets
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // 1. Définir le préfixe pour le Broker (diffusion du serveur vers les clients)
    config.enableSimpleBroker("/topic");

    // 2. Définir le préfixe pour les requêtes de l'application (client vers serveur)
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Définit l'URL pour le handshake de la connexion WebSocket.
    registry.addEndpoint("/ws")
        // Autorise toutes les origines (*) pour les requêtes CORS et SockJS.
        // C'est ESSENTIEL pour permettre la connexion à partir d'un fichier local (origine 'null').
        .setAllowedOriginPatterns("*")
        // Ajoute SockJS pour assurer la compatibilité avec les navigateurs plus anciens
        .withSockJS();
  }
}
