//package com.example.chatserver.chat.config;
//
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//@Configuration
//@EnableWebSocket
//public class WebSocketConfig implements WebSocketConfigurer {
//
//    private final SimpleWebsocketHandler simpleWebsocketHandler;
//
//    public WebSocketConfig(SimpleWebsocketHandler simpleWebsocketHandler) {
//        this.simpleWebsocketHandler = simpleWebsocketHandler;
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        // /conntect url로 websocket연결 요청이 들어오면, 핸들러 클래스가 처리
//        registry.addHandler(simpleWebsocketHandler, "/connect")
//                //securityConfig에서의 cors예외는 http요청에 대한 예외
//                //따라서 websocket프로토콜에 대한 요청에 대해서느 별도의 cors설정 필요
//                .setAllowedOrigins("http://localhost:3000");
//    }
//}
