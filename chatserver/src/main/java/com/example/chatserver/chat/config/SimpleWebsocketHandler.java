//package com.example.chatserver.chat.config;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.*;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
////connect로 웹소켓 연결 요청이 들어왔을때, 이를 처리할 클래스
//@Component
//public class SimpleWebsocketHandler extends TextWebSocketHandler {
//
//    //연결된 세션관리: thread safe한 Set자료구조
//    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
//
//    //연결되면, 사용자 등록
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        sessions.add(session);
//        System.out.println("Connected : " + session.getId());
//    }
//    @Override
//    //사용자 메세지 전송
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload(); //payload에 메세지가 담겨있음
//        System.out.println("recived Message : " + payload);
//        for(WebSocketSession s : sessions){
//            if(s.isOpen()){
//                s.sendMessage(new TextMessage(payload));
//            }
//        }
//    }
//    @Override
//    //연결 끊기면, 사용자 제거
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        sessions.remove(session);
//        System.out.println("Disconnected!");
//    }
//}